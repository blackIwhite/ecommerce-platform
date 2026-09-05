package com.ecommerce.order.mq;

import com.ecommerce.common.mq.constant.MqConstants;
import com.ecommerce.common.mq.message.OrderMessage;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rocketmq.enabled", havingValue = "true")
public class OrderMessageProducer {

    private final RocketMQTemplate rocketMQTemplate;

    public void sendOrderCreated(OrderMessage msg) {
        sendAfterCommit(MqConstants.ORDER_CREATE_TOPIC, msg, 0);
    }

    public void sendOrderCancelled(OrderMessage msg) {
        sendAfterCommit(MqConstants.ORDER_CANCEL_TOPIC, msg, 0);
    }

    public void sendOrderPaySuccess(OrderMessage msg) {
        sendAfterCommit(MqConstants.ORDER_PAY_SUCCESS_TOPIC, msg, 0);
    }

    public void sendDelayAutoCancel(OrderMessage msg) {
        sendAfterCommit(MqConstants.ORDER_CLOSE_TOPIC, msg, MqConstants.DELAY_LEVEL_ORDER_CANCEL);
    }

    public OrderMessage buildMessage(Order order, List<OrderItem> items, String cancelReason) {
        return OrderMessage.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .skuIds(items.stream().map(OrderItem::getSkuId).toList())
                .quantities(items.stream().map(OrderItem::getQuantity).toList())
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getStatus())
                .cancelReason(cancelReason)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private void sendAfterCommit(String topic, OrderMessage msg, int delayLevel) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    doSend(topic, msg, delayLevel);
                }
            });
        } else {
            doSend(topic, msg, delayLevel);
        }
    }

    private void doSend(String topic, OrderMessage msg, int delayLevel) {
        try {
            Message<OrderMessage> message = MessageBuilder.withPayload(msg)
                    .setHeader(RocketMQHeaders.KEYS, String.valueOf(msg.getOrderId()))
                    .build();
            if (delayLevel > 0) {
                rocketMQTemplate.syncSend(topic, message, 3000, delayLevel);
            } else {
                rocketMQTemplate.syncSend(topic, message);
            }
            log.info("Sent MQ message, topic={}, orderId={}", topic, msg.getOrderId());
        } catch (Exception e) {
            log.error("Failed to send MQ message, topic={}, orderId={}", topic, msg.getOrderId(), e);
        }
    }
}
