package br.com.mensageria.processor.infra.consumer;

import br.com.mensageria.processor.application.dto.PaymentReceiveDTO;
import br.com.mensageria.processor.application.dto.PaymentValidatedDTO;
import br.com.mensageria.processor.application.ProcessorService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentProcessorConsumer {
    @Autowired
    private ProcessorService service;

    @RabbitListener(queues = "payment.validated")
    public void process(String dto){
        service.process(dto);
    }

    @RabbitListener(queues = "payment.notification")
    public Object verify(String dto){
        return service.verify(dto);
    }
}
