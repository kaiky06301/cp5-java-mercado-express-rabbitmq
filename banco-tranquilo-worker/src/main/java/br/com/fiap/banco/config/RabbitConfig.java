package br.com.fiap.banco.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declara as exchanges, filas e bindings no RabbitMQ (mesmos nomes usados em aula).
 * Se ja existirem no Broker (criadas pelo Dashboard), nada muda.
 */
@Configuration
public class RabbitConfig {

    // Requisicao de pagamento: Mercado Express -> Banco Tranquilo
    @Bean DirectExchange pagamentoRequestExchange() { return new DirectExchange("pagamento-request-exchange"); }
    @Bean Queue pagamentoRequestQueue() { return new Queue("pagamento-request-queue", true); }
    @Bean Binding pagamentoRequestBinding() {
        return BindingBuilder.bind(pagamentoRequestQueue()).to(pagamentoRequestExchange()).with("pagamento-request-rout-key");
    }

    // Resposta de SUCESSO: Banco Tranquilo -> Mercado Express
    @Bean DirectExchange pagamentoSucessoExchange() { return new DirectExchange("pagamento-response-sucesso-exchange"); }
    @Bean Queue pagamentoSucessoQueue() { return new Queue("pagamento-response-sucesso-queue", true); }
    @Bean Binding pagamentoSucessoBinding() {
        return BindingBuilder.bind(pagamentoSucessoQueue()).to(pagamentoSucessoExchange()).with("pagamento-response-sucesso-rout-key");
    }

    // Resposta de ERRO: Banco Tranquilo -> Mercado Express
    @Bean DirectExchange pagamentoErroExchange() { return new DirectExchange("pagamento-response-erro-exchange"); }
    @Bean Queue pagamentoErroQueue() { return new Queue("pagamento-response-erro-queue", true); }
    @Bean Binding pagamentoErroBinding() {
        return BindingBuilder.bind(pagamentoErroQueue()).to(pagamentoErroExchange()).with("pagamento-response-erro-rout-key");
    }
}
