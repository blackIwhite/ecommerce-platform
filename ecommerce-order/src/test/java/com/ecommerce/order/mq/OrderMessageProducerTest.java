package com.ecommerce.order.mq;

import com.ecommerce.common.mq.constant.MqConstants;
import com.ecommerce.common.mq.message.OrderMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderMessageProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderMessageProducer producer;

    private OrderMessage sampleMessage() {
        return OrderMessage.builder()
                .orderId(1L).userId(100L)
                .skuIds(List.of(1001L)).quantities(List.of(2))
                .totalAmount(new BigDecimal("1998")).orderStatus(0)
                .timestamp(LocalDateTime.now()).build();
    }

    @Test
    void sendOrderCreated_shouldSendToOrderExchange() {
        producer.sendOrderCreated(sampleMessage());

        verify(rabbitTemplate).convertAndSend(
                eq(MqConstants.ORDER_EXCHANGE),
                eq(MqConstants.ORDER_CREATE_KEY),
                any(OrderMessage.class));
    }

    @Test
    void sendDelayAutoCancel_shouldSendWithDelay() {
        producer.sendDelayAutoCancel(sampleMessage());

        verify(rabbitTemplate).convertAndSend(
                eq(MqConstants.ORDER_EXCHANGE),
                eq(MqConstants.ORDER_CLOSE_KEY),
                any(OrderMessage.class),
                any(org.springframework.amqp.core.MessagePostProcessor.class));
    }

    @Test
    void sendOrderPaySuccess_shouldSendToPaySuccessKey() {
        producer.sendOrderPaySuccess(sampleMessage());

        verify(rabbitTemplate).convertAndSend(
                eq(MqConstants.ORDER_EXCHANGE),
                eq(MqConstants.ORDER_PAY_SUCCESS_KEY),
                any(OrderMessage.class));
    }

    @Test
    void doSend_exception_shouldNotPropagate() {
        doThrow(new AmqpException("broker down") {})
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(OrderMessage.class));

        assertDoesNotThrow(() -> producer.sendOrderCreated(sampleMessage()));
    }
}
