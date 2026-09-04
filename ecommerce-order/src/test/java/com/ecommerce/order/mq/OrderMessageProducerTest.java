package com.ecommerce.order.mq;

import com.ecommerce.common.mq.constant.MqConstants;
import com.ecommerce.common.mq.message.OrderMessage;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderMessageProducerTest {

    @Mock
    private RocketMQTemplate rocketMQTemplate;

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
    void sendOrderCreated_shouldSendToCreateTopic() {
        producer.sendOrderCreated(sampleMessage());

        ArgumentCaptor<Message<OrderMessage>> captor = ArgumentCaptor.forClass(Message.class);
        verify(rocketMQTemplate).syncSend(eq(MqConstants.ORDER_CREATE_TOPIC), captor.capture());
        assertEquals("1", captor.getValue().getHeaders().get("KEYS"));
    }

    @Test
    void sendDelayAutoCancel_shouldUseDelayLevel() {
        producer.sendDelayAutoCancel(sampleMessage());

        verify(rocketMQTemplate).syncSend(
                eq(MqConstants.ORDER_CLOSE_TOPIC),
                any(Message.class),
                eq(3000L),
                eq(MqConstants.DELAY_LEVEL_ORDER_CANCEL));
    }

    @Test
    void sendOrderPaySuccess_shouldSendToPayTopic() {
        producer.sendOrderPaySuccess(sampleMessage());

        verify(rocketMQTemplate).syncSend(eq(MqConstants.ORDER_PAY_SUCCESS_TOPIC), any(Message.class));
    }

    @Test
    void doSend_exception_shouldNotPropagate() {
        doThrow(new RuntimeException("broker down"))
                .when(rocketMQTemplate).syncSend(anyString(), any(Message.class));

        assertDoesNotThrow(() -> producer.sendOrderCreated(sampleMessage()));
    }
}
