package br.com.fiap.mercado.api;

import br.com.fiap.mercado.dto.CompraRequestDTO;
import br.com.fiap.mercado.dto.CompraResponseDTO;
import br.com.fiap.mercado.facade.PagamentoFacade;
import br.com.fiap.mercado.model.Pedido;
import br.com.fiap.mercado.repository.PedidoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoApi {

    @Autowired private PagamentoFacade pagamentoFacade;
    @Autowired private PedidoRepository pedidoRepository;

    /** Finaliza uma compra e envia o pagamento para o Banco Tranquilo (exemplo de JSON no README). */
    @PostMapping
    public ResponseEntity<?> processar(@RequestBody @Valid CompraRequestDTO request) {
        try {
            CompraResponseDTO resposta = pagamentoFacade.solicitarPagamento(request);
            return ResponseEntity.accepted().body(resposta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    /** Consulta o status do pedido depois da resposta do Banco. */
    @GetMapping("/{numeroPedido}")
    public ResponseEntity<?> consultar(@PathVariable String numeroPedido) {
        return pedidoRepository.findByNumeroPedido(numeroPedido)
                .<ResponseEntity<?>>map(p -> ResponseEntity.ok(resumo(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    private Map<String, Object> resumo(Pedido p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("numeroPedido", p.getNumeroPedido());
        m.put("status", p.getStatus());
        m.put("cliente", p.getNomeCliente());
        m.put("email", p.getEmailCliente());
        m.put("itens", p.getResumoItens());
        m.put("valorTotal", p.getValorTotal());
        m.put("mensagemBanco", p.getMensagemBanco());
        m.put("codigoAutorizacao", p.getCodigoAutorizacao());
        m.put("dataCriacao", p.getDataCriacao());
        return m;
    }
}
