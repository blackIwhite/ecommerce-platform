package com.ecommerce.common.mq.config;

import com.ecommerce.common.mq.constant.MqConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ---- Order exchange ----

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(MqConstants.ORDER_EXCHANGE, true, false);
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

    @Bean
    public Queue orderCloseDelayQueue() {
        return QueueBuilder.durable(MqConstants.ORDER_CLOSE_DELAY_QUEUE)
                .withArgument("x-message-ttl", MqConstants.DELAY_ORDER_CANCEL_MS)
                .withArgument("x-dead-letter-exchange", MqConstants.ORDER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", MqConstants.ORDER_CLOSE_KEY)
                .build();
    }

    // ---- Order bindings ----

    @Bean
    public Binding orderCreateBinding() {
        return BindingBuilder.bind(orderCreateQueue()).to(orderExchange()).with(MqConstants.ORDER_CREATE_KEY);
    }

    @Bean
    public Binding orderCancelBinding() {
        return BindingBuilder.bind(orderCancelQueue()).to(orderExchange()).with(MqConstants.ORDER_CANCEL_KEY);
    }

    @Bean
    public Binding orderPaySuccessBinding() {
        return BindingBuilder.bind(orderPaySuccessQueue()).to(orderExchange()).with(MqConstants.ORDER_PAY_SUCCESS_KEY);
    }

    @Bean
    public Binding orderCloseBinding() {
        return BindingBuilder.bind(orderCloseQueue()).to(orderExchange()).with(MqConstants.ORDER_CLOSE_KEY);
    }

    // ---- Marketing exchange ----

    @Bean
    public DirectExchange marketingExchange() {
        return new DirectExchange(MqConstants.MARKETING_EXCHANGE, true, false);
    }

    // ---- Marketing queues ----

    @Bean
    public Queue pointsEarnQueue() {
        return QueueBuilder.durable(MqConstants.POINTS_EARN_QUEUE).build();
    }

    // ---- Marketing bindings ----

    @Bean
    public Binding pointsEarnBinding() {
        return BindingBuilder.bind(pointsEarnQueue()).to(marketingExchange()).with(MqConstants.POINTS_EARN_KEY);
    }
}
