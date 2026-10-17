package br.com.fiap.mercado.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PagamentoDTO {
    private String numeroPedido;
    private BigDecimal valor;
    private String produto;
    private String nomeCliente;
    private String emailCliente;
}
