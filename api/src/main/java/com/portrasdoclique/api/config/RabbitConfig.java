package com.portrasdoclique.api.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "filme.events";
    public static final String QUEUE_BUSCADO = "filme.buscado";
    public static final String ROUTING_KEY_BUSCADO = "filme.buscado";

    @Bean
    public TopicExchange filmeExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue filmeBuscadoQueue() {
        return QueueBuilder.durable(QUEUE_BUSCADO).build();
    }

    @Bean
    public Binding filmeBuscadoBinding() {
        return BindingBuilder
                .bind(filmeBuscadoQueue())
                .to(filmeExchange())
                .with(ROUTING_KEY_BUSCADO);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}