package br.com.fiap.mercado.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoRespostaDTO {
    private String numeroPedido;
    private String status;            // APROVADO ou RECUSADO
    private String mensagem;
    private String codigoAutorizacao;
    private BigDecimal valor;
    private String emailCliente;
    private String dataHora;
}
