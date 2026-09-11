package br.com.mensageria.processor.infra.config;

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

@Configuration
public class RabbitMQConfig {
    private final static String EXCHANGE_NAME = "mensageria-payment-exchange";
    private static final String QUEUE_NAME = "payment.validated";
    private static final String ROUTING_KEY = "payment.validated";

    private static final String QUEUE_NOTIFICATION_NAME = "payment.notification";
    private static final String ROUTING_KEY_NOTIFICATION = "payment.notification";

    private static final String QUEUE_CANCEL_NAME = "payment.cancel";
    private static final String ROUTING_KEY_CANCEL = "payment.cancel";

    private static final String DEAD_LETTER_EXCHANGE_NAME = "mensageria-payment-dlx";
    private static final String DEAD_LETTER_PAY_QUEUE_NAME = "payment.validated.dlq";
    private static final String DEAD_LETTER_ROUTING_KEY = "payment.validated.dlq";

    private static final String DEAD_LETTER_ROUTING_KEY_NOTIFICATION = "payment.notification.dlq";
    private static final String DEAD_LETTER_NOTIFICATION_QUEUE_NAME = "payment.notification.dlq";

    private static final String DEAD_LETTER_CANCEL_QUEUE_NAME = "payment.cancel.dlq";
    private static final String DEAD_LETTER_ROUTING_KEY_CANCEL = "payment.cancel.dlq";

    @Bean
    public Exchange exchange() {
        return ExchangeBuilder
                .topicExchange(EXCHANGE_NAME)
                .build();
    }
    @Bean
    public Exchange deadLetterExchange() {
        return ExchangeBuilder
                .topicExchange(DEAD_LETTER_EXCHANGE_NAME)
                .build();
    }

    @Bean
    public Queue queue() {
        return QueueBuilder
                .durable(QUEUE_NAME)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE_NAME)
                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY)
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
                .deadLetterExchange(DEAD_LETTER_EXCHANGE_NAME)
                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY_NOTIFICATION)
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
    public Queue cancelQueue() {
        return QueueBuilder
                .durable(QUEUE_CANCEL_NAME)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE_NAME)
                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY_CANCEL)
                .build();
    }

    @Bean
    public Binding cancelBinding() {
        return BindingBuilder
                .bind(cancelQueue())
                .to(exchange())
                .with(ROUTING_KEY_CANCEL)
                .noargs();
    }


    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory, MessageConverter jacksonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonMessageConverter);

        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                        .maxRetries(3)
                        .backOffOptions(1000, 1.0, 10000)
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

    @Bean
    public Queue deadLetterPayQueue() {
        return QueueBuilder
                .durable(DEAD_LETTER_PAY_QUEUE_NAME)
                .build();
    }

    @Bean
    public Binding deadLetterpayBinding() {
        return BindingBuilder
                .bind(deadLetterPayQueue())
                .to(deadLetterExchange())
                .with(DEAD_LETTER_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Queue deadLetterVerifyQueue(){
        return QueueBuilder
                .durable(DEAD_LETTER_NOTIFICATION_QUEUE_NAME)
                .build();
    }

    @Bean
    public Binding deadLetterVerifyBinding() {
        return BindingBuilder
                .bind(deadLetterVerifyQueue())
                .to(deadLetterExchange())
                .with(DEAD_LETTER_ROUTING_KEY_NOTIFICATION)
                .noargs();
    }

    @Bean
    public Queue deadLetterCancelQueue() {
        return QueueBuilder
                .durable(DEAD_LETTER_CANCEL_QUEUE_NAME)
                .build();
    }

    @Bean
    public Binding deadLetterCancelBinding() {
        return BindingBuilder
                .bind(deadLetterCancelQueue())
                .to(deadLetterExchange())
                .with(DEAD_LETTER_ROUTING_KEY_CANCEL)
                .noargs();
    }
}
