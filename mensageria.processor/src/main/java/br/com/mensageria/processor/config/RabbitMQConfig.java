package br.com.mensageria.processor.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    private final static String EXCHANGE_NAME = "mensageria-payment-exchange";
    private static final String FILA_NAME = "payment.validated";
    private static final String ROUTING_KEY = "payment.validated";

    @Bean
    public Exchange exchange() {
        return ExchangeBuilder
                .topicExchange(EXCHANGE_NAME)
                .build();
    }
    @Bean
    public Queue queue() {
        return QueueBuilder
                .durable(FILA_NAME)
                .build();
    }
    @Bean
    public Binding binding() {
        return BindingBuilder
                .bind(queue())
                .to(exchange())
                .with(ROUTING_KEY)
                .noargs();
    }
}
