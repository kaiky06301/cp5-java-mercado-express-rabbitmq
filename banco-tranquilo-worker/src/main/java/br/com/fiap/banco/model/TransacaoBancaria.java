package br.com.fiap.banco.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "TDS_Banco_Tranquilo_Transacoes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransacaoBancaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numeroPedido;
    private String emailCliente;

    @Column(precision = 12, scale = 2)
    private BigDecimal valor;

    private String status;
    private String motivo;
    private String codigoAutorizacao;
    private LocalDateTime dataHora;
}
