package br.com.fiap.mercado.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Controle de envio de e-mails para os clientes. */
@Entity
@Table(name = "TDS_Mercado_Emails_Enviados")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmailEnviado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numeroPedido;
    private String destinatario;
    private String assunto;

    @Column(columnDefinition = "TEXT")
    private String corpo;

    private String status;   // ENVIADO ou FALHA
    private String erro;
    private LocalDateTime dataEnvio;
}
