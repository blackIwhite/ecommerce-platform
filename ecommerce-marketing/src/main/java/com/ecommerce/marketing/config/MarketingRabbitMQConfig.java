package com.ecommerce.marketing.config;

import com.ecommerce.common.mq.constant.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MarketingRabbitMQConfig {

    private static final String ORDER_PAY_SUCCESS_FOR_POINTS = "marketing.queue.order-pay-success";

    @Bean
    public Queue orderPaySuccessForPointsQueue() {
        return QueueBuilder.durable(ORDER_PAY_SUCCESS_FOR_POINTS).build();
    }

    @Bean
    public Binding orderPaySuccessForPointsBinding(DirectExchange orderExchange) {
        return BindingBuilder.bind(orderPaySuccessForPointsQueue())
                .to(orderExchange)
                .with(MqConstants.ORDER_PAY_SUCCESS_KEY);
    }
}
