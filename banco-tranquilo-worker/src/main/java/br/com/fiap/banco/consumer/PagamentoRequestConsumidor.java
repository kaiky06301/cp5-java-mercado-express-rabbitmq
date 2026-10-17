package br.com.fiap.banco.consumer;

import br.com.fiap.banco.dto.PagamentoDTO;
import br.com.fiap.banco.dto.PagamentoRespostaDTO;
import br.com.fiap.banco.model.ContaBancaria;
import br.com.fiap.banco.model.TransacaoBancaria;
import br.com.fiap.banco.producer.PagamentoErroProdutor;
import br.com.fiap.banco.producer.PagamentoSucessoProdutor;
import br.com.fiap.banco.repository.ContaBancariaRepository;
import br.com.fiap.banco.repository.TransacaoBancariaRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * API do Banco Tranquilo: recebe a requisicao de pagamento do Mercado Express,
 * valida a conta e o saldo do cliente e responde na fila de SUCESSO ou de ERRO.
 */
@Component
public class PagamentoRequestConsumidor {

    @Autowired private PagamentoErroProdutor erroProdutor;
    @Autowired private PagamentoSucessoProdutor sucessoProdutor;
    @Autowired private ContaBancariaRepository contaRepository;
    @Autowired private TransacaoBancariaRepository transacaoRepository;
    @Autowired private JsonMapper jsonMapper;

    @Transactional
    @RabbitListener(queues = {"pagamento-request-queue"})
    public void receberMensagem(@Payload Message<?> message) {
        String payload = String.valueOf(message.getPayload());
        System.out.println("==== REQUISICAO RECEBIDA ==== " + payload);

        PagamentoDTO pagamento;
        try {
            pagamento = jsonMapper.readValue(payload, PagamentoDTO.class);
        } catch (Exception e) {
            erroProdutor.gerarResposta(json(new PagamentoRespostaDTO(null, "RECUSADO",
                    "Mensagem invalida: " + e.getMessage(), null, null, null, LocalDateTime.now().toString())));
            return;
        }

        String motivoRecusa = null;
        Optional<ContaBancaria> conta = pagamento.getEmailCliente() == null
                ? Optional.empty()
                : contaRepository.findByEmailIgnoreCase(pagamento.getEmailCliente());

        if (pagamento.getValor() == null || pagamento.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            motivoRecusa = "Valor da compra invalido";
        } else if (conta.isEmpty()) {
            motivoRecusa = "Conta nao encontrada no Banco Tranquilo para " + pagamento.getEmailCliente();
        } else if (conta.get().getSaldo().compareTo(pagamento.getValor()) < 0) {
            motivoRecusa = "Saldo insuficiente (saldo R$ " + conta.get().getSaldo() + ")";
        }

        TransacaoBancaria transacao = TransacaoBancaria.builder()
                .numeroPedido(pagamento.getNumeroPedido())
                .emailCliente(pagamento.getEmailCliente())
                .valor(pagamento.getValor())
                .dataHora(LocalDateTime.now())
                .build();

        if (motivoRecusa == null) {
            ContaBancaria c = conta.get();
            c.setSaldo(c.getSaldo().subtract(pagamento.getValor()));
            contaRepository.save(c);

            String codigo = "BT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            transacao.setStatus("APROVADO");
            transacao.setCodigoAutorizacao(codigo);
            transacao.setMotivo("Pagamento aprovado");
            transacaoRepository.save(transacao);

            sucessoProdutor.gerarResposta(json(new PagamentoRespostaDTO(pagamento.getNumeroPedido(), "APROVADO",
                    "Pagamento aprovado pelo Banco Tranquilo", codigo, pagamento.getValor(),
                    pagamento.getEmailCliente(), LocalDateTime.now().toString())));
            System.out.println("==== PAGAMENTO APROVADO ==== " + pagamento.getNumeroPedido() + " " + codigo);
        } else {
            transacao.setStatus("RECUSADO");
            transacao.setMotivo(motivoRecusa);
            transacaoRepository.save(transacao);

            erroProdutor.gerarResposta(json(new PagamentoRespostaDTO(pagamento.getNumeroPedido(), "RECUSADO",
                    motivoRecusa, null, pagamento.getValor(), pagamento.getEmailCliente(), LocalDateTime.now().toString())));
            System.err.println("==== PAGAMENTO RECUSADO ==== " + pagamento.getNumeroPedido() + " - " + motivoRecusa);
        }
    }

    private String json(Object obj) {
        return jsonMapper.writeValueAsString(obj);
    }
}
