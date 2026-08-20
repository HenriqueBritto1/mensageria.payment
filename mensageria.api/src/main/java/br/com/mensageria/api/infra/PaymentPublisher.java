package br.com.mensageria.api.infra;

import br.com.mensageria.api.application.dto.PaymentReceiveDTO;
import br.com.mensageria.api.application.dto.PaymentValidatedDTO;
import br.com.mensageria.commons.exceptions.PaymentError;
import br.com.mensageria.commons.exceptions.PaymentNotFound;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
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
        try {
            Object response = rabbitTemplate.convertSendAndReceive(EXCHANGE_NAME, ROUTING_KEY_NOTIFICATION, json);
            return mapper.readValue((String) response, PaymentReceiveDTO.class);
        }catch (Exception e){
            throw new PaymentError("Erro ao buscar Pagamento");
        }
    }
}
