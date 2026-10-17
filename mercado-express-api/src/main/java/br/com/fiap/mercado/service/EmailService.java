package br.com.fiap.mercado.service;

import br.com.fiap.mercado.model.EmailEnviado;
import br.com.fiap.mercado.model.ItemPedido;
import br.com.fiap.mercado.model.Pedido;
import br.com.fiap.mercado.repository.EmailEnviadoRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Envia o e-mail de confirmacao de pagamento e registra o envio no BD (controle de e-mails). */
@Service
public class EmailService {

    @Autowired private JavaMailSender mailSender;
    @Autowired private EmailEnviadoRepository emailRepository;

    @Value("${mercado.email-remetente}")
    private String remetente;

    public void enviarConfirmacaoPagamento(Pedido pedido) {
        String assunto = "Pagamento confirmado - Pedido " + pedido.getNumeroPedido();
        String corpo = montarCorpo(pedido);

        EmailEnviado registro = EmailEnviado.builder()
                .numeroPedido(pedido.getNumeroPedido())
                .destinatario(pedido.getEmailCliente())
                .assunto(assunto)
                .corpo(corpo)
                .dataEnvio(LocalDateTime.now())
                .build();
        try {
            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");
            helper.setFrom(remetente);
            helper.setTo(pedido.getEmailCliente());
            helper.setSubject(assunto);
            helper.setText(corpo, true);
            mailSender.send(mensagem);
            registro.setStatus("ENVIADO");
            System.out.println("==== E-MAIL ENVIADO ==== " + pedido.getEmailCliente());
        } catch (Exception e) {
            registro.setStatus("FALHA");
            registro.setErro(e.getMessage());
            System.err.println("==== FALHA NO E-MAIL ==== " + e.getMessage());
        }
        emailRepository.save(registro);
    }

    private String montarCorpo(Pedido pedido) {
        StringBuilder linhas = new StringBuilder();
        for (ItemPedido i : pedido.getItens()) {
            linhas.append("<tr><td style='padding:6px 10px'>").append(i.getProduto().getEmoji()).append(' ')
                  .append(i.getProduto().getNome()).append("</td><td style='padding:6px 10px;text-align:center'>")
                  .append(i.getQuantidade()).append("</td><td style='padding:6px 10px;text-align:right'>R$ ")
                  .append(String.format("%.2f", i.getSubtotal())).append("</td></tr>");
        }
        return """
            <div style="font-family:Arial,sans-serif;max-width:560px;margin:auto;border:1px solid #e5e7eb;border-radius:12px;overflow:hidden">
              <div style="background:#16a34a;color:#fff;padding:18px 22px">
                <h2 style="margin:0">Mercado Express</h2>
                <p style="margin:4px 0 0">Pagamento confirmado pelo Banco Tranquilo</p>
              </div>
              <div style="padding:20px 22px;color:#111827">
                <p>Ola, <b>%s</b>!</p>
                <p>Recebemos a confirmacao do pagamento do seu pedido <b>%s</b>.</p>
                <table style="width:100%%;border-collapse:collapse;background:#f9fafb;border-radius:8px">
                  <tr style="background:#e5e7eb"><th style="padding:6px 10px;text-align:left">Produto</th><th>Qtd</th><th style="padding:6px 10px;text-align:right">Subtotal</th></tr>
                  %s
                  <tr><td colspan="2" style="padding:8px 10px;text-align:right"><b>Total</b></td><td style="padding:8px 10px;text-align:right"><b>R$ %.2f</b></td></tr>
                </table>
                <p>Codigo de autorizacao: <b>%s</b><br>Data: %s</p>
                <p>Obrigado por comprar no Mercado Express!</p>
              </div>
            </div>
            """.formatted(pedido.getNomeCliente(), pedido.getNumeroPedido(), linhas, pedido.getValorTotal(),
                pedido.getCodigoAutorizacao(),
                pedido.getDataAtualizacao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }
}
