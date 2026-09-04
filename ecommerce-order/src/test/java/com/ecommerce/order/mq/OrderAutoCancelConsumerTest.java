package com.ecommerce.order.mq;

import com.ecommerce.common.mq.message.OrderMessage;
import com.ecommerce.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderAutoCancelConsumerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderAutoCancelConsumer consumer;

    @Test
    void onMessage_shouldDelegateToAutoCancelOrder() {
        OrderMessage msg = OrderMessage.builder().orderId(1L).build();

        consumer.onMessage(msg);

        verify(orderService).autoCancelOrder(1L);
    }

    @Test
    void onMessage_serviceThrows_shouldPropagate() {
        OrderMessage msg = OrderMessage.builder().orderId(1L).build();
        doThrow(new RuntimeException("db error")).when(orderService).autoCancelOrder(1L);

        assertThrows(RuntimeException.class, () -> consumer.onMessage(msg));
    }
}
