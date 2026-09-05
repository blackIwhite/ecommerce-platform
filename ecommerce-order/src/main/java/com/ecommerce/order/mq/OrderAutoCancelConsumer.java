package com.ecommerce.order.mq;

import com.ecommerce.common.mq.constant.MqConstants;
import com.ecommerce.common.mq.message.OrderMessage;
import com.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderAutoCancelConsumer {

    private final OrderService orderService;

    @RabbitListener(queues = MqConstants.ORDER_CLOSE_QUEUE)
    public void onMessage(OrderMessage message) {
        log.info("Received delayed auto-cancel message, orderId={}", message.getOrderId());
        orderService.autoCancelOrder(message.getOrderId());
    }
}
