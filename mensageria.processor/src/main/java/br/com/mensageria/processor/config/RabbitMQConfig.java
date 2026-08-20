package br.com.mensageria.processor.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;

import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class RabbitMQConfig {
    private final static String EXCHANGE_NAME = "mensageria-payment-exchange";
    private static final String QUEUE_NAME = "payment.validated";
    private static final String ROUTING_KEY = "payment.validated";

    private static final String QUEUE_NOTIFICATION_NAME = "payment.notification";
    private static final String ROUTING_KEY_NOTIFICATION = "payment.notification";
    @Bean
    public Exchange exchange() {
        return ExchangeBuilder
                .topicExchange(EXCHANGE_NAME)
                .build();
    }
    @Bean
    public Queue queue() {
        return QueueBuilder
                .durable(QUEUE_NAME)
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

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder
                .durable(QUEUE_NOTIFICATION_NAME)
                .build();
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(exchange())
                .with(ROUTING_KEY_NOTIFICATION)
                .noargs();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory, MessageConverter jacksonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonMessageConverter);

        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                        .maxRetries(3)
                        .backOffOptions(
                                1000,
                                1.0,
                                10000
                        )
                        .recoverer(new RejectAndDontRequeueRecoverer())
                        .build()
        );
        return factory;
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter converter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter);

        return rabbitTemplate;
    }

}
