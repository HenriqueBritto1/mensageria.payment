package br.com.mensageria.api.infra;

import br.com.mensageria.commons.dto.PaymentReceiveDTO;
import br.com.mensageria.commons.exceptions.PaymentError;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;


@Component
public class PaymentPublisher {
    private final static String ROUTING_KEY= "payment.validated";
    private static final String EXCHANGE_NAME = "mensageria-payment-exchange";
    private static final String ROUTING_KEY_NOTIFICATION = "payment.notification";
    private static final String ROUTING_KEY_CANCEL="payment.cancel";
    private static final String ROUTING_KEY_REFUND="payment.refund";

    private ObjectMapper mapper = new ObjectMapper();
    private final RabbitTemplate rabbitTemplate;

    public PaymentPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(Object json){
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

    public PaymentReceiveDTO publishAndReceivePayment(String json){
        try {
            Object response = rabbitTemplate.convertSendAndReceive(EXCHANGE_NAME, ROUTING_KEY, json);
            return mapper.readValue((String) response, PaymentReceiveDTO.class);
        }catch (Exception e){
            throw new PaymentError("Erro ao enviar Pagamento");
        }
    }

    public PaymentReceiveDTO publishAndReceiveCancel(String json){
        try {
            Object response = rabbitTemplate.convertSendAndReceive(EXCHANGE_NAME, ROUTING_KEY_CANCEL, json);
            System.out.println(response.toString());
            return mapper.readValue((String) response, PaymentReceiveDTO.class);
        }catch (Exception e){
            throw new PaymentError("Erro ao cancelar Pagamento");
        }
    }

    public PaymentReceiveDTO publishAndReceiveRefund(String json){
        try {
            Object response = rabbitTemplate.convertSendAndReceive(EXCHANGE_NAME, ROUTING_KEY_REFUND, json);
            return mapper.readValue((String) response, PaymentReceiveDTO.class);
        }catch (Exception e){
            throw new PaymentError("Erro ao reembolsar Pagamento");
        }
    }
}
