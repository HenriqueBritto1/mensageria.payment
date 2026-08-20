package br.com.mensageria.api.infra;

import br.com.mensageria.api.application.dto.PaymentReceiveDTO;
import br.com.mensageria.api.application.dto.PaymentValidatedDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.ObjectMapper;


@Component
public class PaymentPublisher {
    private final static String ROUTING_KEY= "payment.validated";
    private static final String EXCHANGE_NAME = "mensageria-payment-exchange";
    private static final String ROUTING_KEY_NOTIFICATION = "payment.notification";

    private ObjectMapper mapper = new ObjectMapper();
    private final RabbitTemplate rabbitTemplate;

    public PaymentPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(PaymentValidatedDTO json){
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, json);
    }

    public PaymentReceiveDTO publishAndReceive(String json){
        Object response = rabbitTemplate.convertSendAndReceive(EXCHANGE_NAME, ROUTING_KEY_NOTIFICATION, json);
        var dto = mapper.readValue((String) response, PaymentReceiveDTO.class);
        if(response == null){
            throw new RuntimeException("Erro ao consultar pagamento");
        }
        return dto;
    }
}
