package com.ecommerce.common.mq.config;

import com.ecommerce.common.mq.constant.MqConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ---- Order exchange (delayed) ----

    @Bean
    public CustomExchange orderExchange() {
        return new CustomExchange(MqConstants.ORDER_EXCHANGE, "x-delayed-message", true, false,
                Map.of("x-delayed-type", "direct"));
    }

    // ---- Order queues ----

    @Bean
    public Queue orderCreateQueue() {
        return QueueBuilder.durable(MqConstants.ORDER_CREATE_QUEUE).build();
    }

    @Bean
    public Queue orderCancelQueue() {
        return QueueBuilder.durable(MqConstants.ORDER_CANCEL_QUEUE).build();
    }

    @Bean
    public Queue orderPaySuccessQueue() {
        return QueueBuilder.durable(MqConstants.ORDER_PAY_SUCCESS_QUEUE).build();
    }

    @Bean
    public Queue orderCloseQueue() {
        return QueueBuilder.durable(MqConstants.ORDER_CLOSE_QUEUE).build();
    }

    // ---- Order bindings ----

    @Bean
    public Binding orderCreateBinding() {
        return BindingBuilder.bind(orderCreateQueue()).to(orderExchange()).with(MqConstants.ORDER_CREATE_KEY).noargs();
    }

    @Bean
    public Binding orderCancelBinding() {
        return BindingBuilder.bind(orderCancelQueue()).to(orderExchange()).with(MqConstants.ORDER_CANCEL_KEY).noargs();
    }

    @Bean
    public Binding orderPaySuccessBinding() {
        return BindingBuilder.bind(orderPaySuccessQueue()).to(orderExchange()).with(MqConstants.ORDER_PAY_SUCCESS_KEY).noargs();
    }

    @Bean
    public Binding orderCloseBinding() {
        return BindingBuilder.bind(orderCloseQueue()).to(orderExchange()).with(MqConstants.ORDER_CLOSE_KEY).noargs();
    }
}
