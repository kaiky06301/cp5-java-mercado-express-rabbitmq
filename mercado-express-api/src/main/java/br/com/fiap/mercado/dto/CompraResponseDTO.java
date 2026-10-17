package br.com.fiap.mercado.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CompraResponseDTO {
    private String numeroPedido;
    private String status;
    private BigDecimal valorTotal;
    private String mensagem;
}
