package br.com.fiap.mercado.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TDS_Mercado_Pedidos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_pedido", nullable = false, unique = true)
    private String numeroPedido;

    private String nomeCliente;
    private String emailCliente;

    @Column(precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Column(precision = 12, scale = 2)
    private BigDecimal custoTotal;

    @Enumerated(EnumType.STRING)
    private StatusPedido status;

    private String mensagemBanco;
    private String codigoAutorizacao;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;

    @Builder.Default
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ItemPedido> itens = new ArrayList<>();

    public BigDecimal getLucro() {
        if (valorTotal == null || custoTotal == null) return BigDecimal.ZERO;
        return valorTotal.subtract(custoTotal);
    }

    public String getResumoItens() {
        return itens.stream()
                .map(i -> i.getQuantidade() + "x " + i.getProduto().getNome())
                .reduce((a, b) -> a + ", " + b).orElse("");
    }
}
