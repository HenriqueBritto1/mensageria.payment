package br.com.mensageria.processor.infra.consumer;

import br.com.mensageria.processor.application.ProcessorService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentProcessorConsumer {
    @Autowired
    private ProcessorService service;

    @RabbitListener(queues = "payment.validated")
    public Object process(String dto){
        return service.process(dto);
    }

    @RabbitListener(queues = "payment.notification")
    public Object verify(String dto){
        return service.verify(dto);
    }

    @RabbitListener(queues = "payment.cancel")
    public Object cancel(String dto){
        return service.cancel(dto);
    }

    @RabbitListener(queues = "payment.refund")
    public Object change(String dto){
        return service.refund(dto);
    }
}
