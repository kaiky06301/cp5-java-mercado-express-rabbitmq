package br.com.fiap.mercado.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** Requisicao de compra (carrinho) enviada pela tela ou pelo Postman. */
@Data
public class CompraRequestDTO {

    @NotBlank
    private String nomeCliente;

    @NotBlank @Email
    private String emailCliente;

    @NotEmpty @Valid
    private List<Item> itens = new ArrayList<>();

    @Data
    public static class Item {
        @NotNull
        private Long produtoId;
        @NotNull @Min(1)
        private Integer quantidade;
    }
}
