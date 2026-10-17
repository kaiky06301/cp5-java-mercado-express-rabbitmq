package br.com.fiap.mercado.producer;

import br.com.fiap.mercado.dto.PagamentoDTO;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class PagamentoRequestProducer {

    @Autowired private AmqpTemplate amqpTemplate;
    @Autowired private JsonMapper jsonMapper;

    public void integrar(PagamentoDTO pagamento) {
        amqpTemplate.convertAndSend(
            "pagamento-request-exchange",
            "pagamento-request-rout-key",
            jsonMapper.writeValueAsString(pagamento)
        );
    }
}
