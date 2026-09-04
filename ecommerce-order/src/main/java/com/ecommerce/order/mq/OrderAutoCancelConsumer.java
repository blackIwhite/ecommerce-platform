package com.ecommerce.order.mq;

import com.ecommerce.common.mq.constant.MqConstants;
import com.ecommerce.common.mq.message.OrderMessage;
import com.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = MqConstants.ORDER_CLOSE_TOPIC,
        consumerGroup = MqConstants.ORDER_CONSUMER_GROUP
)
public class OrderAutoCancelConsumer implements RocketMQListener<OrderMessage> {

    private final OrderService orderService;

    @Override
    public void onMessage(OrderMessage message) {
        log.info("Received delayed auto-cancel message, orderId={}", message.getOrderId());
        orderService.autoCancelOrder(message.getOrderId());
    }
}
