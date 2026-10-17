package br.com.fiap.mercado.facade;

import br.com.fiap.mercado.dto.CompraRequestDTO;
import br.com.fiap.mercado.dto.CompraResponseDTO;
import br.com.fiap.mercado.dto.PagamentoDTO;
import br.com.fiap.mercado.dto.PagamentoRespostaDTO;
import br.com.fiap.mercado.model.ItemPedido;
import br.com.fiap.mercado.model.Pedido;
import br.com.fiap.mercado.model.Produto;
import br.com.fiap.mercado.model.StatusPedido;
import br.com.fiap.mercado.producer.PagamentoRequestProducer;
import br.com.fiap.mercado.repository.PedidoRepository;
import br.com.fiap.mercado.repository.ProdutoRepository;
import br.com.fiap.mercado.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PagamentoFacade {

    @Autowired private PagamentoRequestProducer producer;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private EmailService emailService;
    @Autowired private JsonMapper jsonMapper;

    /** Registra o pedido no BD e envia a requisicao de pagamento para o Banco Tranquilo via RabbitMQ. */
    @Transactional
    public CompraResponseDTO solicitarPagamento(CompraRequestDTO request) {
        Pedido pedido = Pedido.builder()
                .numeroPedido(gerarNumeroPedido())
                .nomeCliente(request.getNomeCliente())
                .emailCliente(request.getEmailCliente())
                .status(StatusPedido.AGUARDANDO_PAGAMENTO)
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal custo = BigDecimal.ZERO;
        for (CompraRequestDTO.Item item : request.getItens()) {
            Produto produto = produtoRepository.findById(item.getProdutoId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto " + item.getProdutoId() + " nao encontrado"));
            if (produto.getEstoque() < item.getQuantidade()) {
                throw new IllegalArgumentException("Estoque insuficiente para " + produto.getNome()
                        + " (disponivel: " + produto.getEstoque() + ")");
            }
            pedido.getItens().add(ItemPedido.builder()
                    .pedido(pedido)
                    .produto(produto)
                    .quantidade(item.getQuantidade())
                    .precoUnitario(produto.getPrecoVenda())
                    .custoUnitario(produto.getPrecoCusto())
                    .build());
            total = total.add(produto.getPrecoVenda().multiply(BigDecimal.valueOf(item.getQuantidade())));
            custo = custo.add(produto.getPrecoCusto().multiply(BigDecimal.valueOf(item.getQuantidade())));
        }
        pedido.setValorTotal(total);
        pedido.setCustoTotal(custo);
        pedidoRepository.save(pedido);

        PagamentoDTO pagamento = new PagamentoDTO();
        pagamento.setNumeroPedido(pedido.getNumeroPedido());
        pagamento.setValor(total);
        pagamento.setProduto(pedido.getResumoItens());
        pagamento.setNomeCliente(pedido.getNomeCliente());
        pagamento.setEmailCliente(pedido.getEmailCliente());
        producer.integrar(pagamento);

        return new CompraResponseDTO(pedido.getNumeroPedido(), pedido.getStatus().name(), total,
                "Pagamento aguardando confirmacao do Banco Tranquilo...");
    }

    /** Resposta de SUCESSO do Banco: marca como PAGO, baixa o estoque e envia o e-mail de confirmacao. */
    @Transactional
    public void sucessoPagamento(String payload) {
        System.out.println("==== RESPOSTA SUCESSO =====" + payload);
        PagamentoRespostaDTO resposta = jsonMapper.readValue(payload, PagamentoRespostaDTO.class);
        pedidoRepository.findByNumeroPedido(resposta.getNumeroPedido()).ifPresent(pedido -> {
            if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) return;
            pedido.setStatus(StatusPedido.PAGO);
            pedido.setMensagemBanco(resposta.getMensagem());
            pedido.setCodigoAutorizacao(resposta.getCodigoAutorizacao());
            pedido.setDataAtualizacao(LocalDateTime.now());
            for (ItemPedido item : pedido.getItens()) {
                Produto p = item.getProduto();
                p.setEstoque(Math.max(0, p.getEstoque() - item.getQuantidade()));
                produtoRepository.save(p);
            }
            pedidoRepository.save(pedido);
            emailService.enviarConfirmacaoPagamento(pedido);
        });
    }

    /** Resposta de ERRO do Banco: marca o pedido como RECUSADO com o motivo. */
    @Transactional
    public void erroPagamento(String payload) {
        System.err.println("==== RESPOSTA ERRO =====" + payload);
        PagamentoRespostaDTO resposta = jsonMapper.readValue(payload, PagamentoRespostaDTO.class);
        if (resposta.getNumeroPedido() == null) return;
        pedidoRepository.findByNumeroPedido(resposta.getNumeroPedido()).ifPresent(pedido -> {
            if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) return;
            pedido.setStatus(StatusPedido.RECUSADO);
            pedido.setMensagemBanco(resposta.getMensagem());
            pedido.setDataAtualizacao(LocalDateTime.now());
            pedidoRepository.save(pedido);
        });
    }

    private String gerarNumeroPedido() {
        return "ME-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + ThreadLocalRandom.current().nextInt(100, 1000);
    }
}
