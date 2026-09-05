package com.ecommerce.order.mq;

import com.ecommerce.common.mq.constant.MqConstants;
import com.ecommerce.common.mq.message.OrderMessage;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendOrderCreated(OrderMessage msg) {
        sendAfterCommit(MqConstants.ORDER_EXCHANGE, MqConstants.ORDER_CREATE_KEY, msg);
    }

    public void sendOrderCancelled(OrderMessage msg) {
        sendAfterCommit(MqConstants.ORDER_EXCHANGE, MqConstants.ORDER_CANCEL_KEY, msg);
    }

    public void sendOrderPaySuccess(OrderMessage msg) {
        sendAfterCommit(MqConstants.ORDER_EXCHANGE, MqConstants.ORDER_PAY_SUCCESS_KEY, msg);
    }

    public void sendDelayAutoCancel(OrderMessage msg) {
        sendAfterCommitToQueue(MqConstants.ORDER_CLOSE_DELAY_QUEUE, msg);
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

    private void sendAfterCommit(String exchange, String routingKey, OrderMessage msg) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    doSend(exchange, routingKey, msg);
                }
            });
        } else {
            doSend(exchange, routingKey, msg);
        }
    }

    private void sendAfterCommitToQueue(String queue, OrderMessage msg) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    doSendToQueue(queue, msg);
                }
            });
        } else {
            doSendToQueue(queue, msg);
        }
    }

    private void doSend(String exchange, String routingKey, OrderMessage msg) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, msg);
            log.info("Sent MQ message, exchange={}, routingKey={}, orderId={}", exchange, routingKey, msg.getOrderId());
        } catch (AmqpException e) {
            log.error("Failed to send MQ message, exchange={}, routingKey={}, orderId={}", exchange, routingKey, msg.getOrderId(), e);
        }
    }

    private void doSendToQueue(String queue, OrderMessage msg) {
        try {
            rabbitTemplate.convertAndSend(queue, msg);
            log.info("Sent delayed MQ message, queue={}, orderId={}", queue, msg.getOrderId());
        } catch (AmqpException e) {
            log.error("Failed to send delayed MQ message, queue={}, orderId={}", queue, msg.getOrderId(), e);
        }
    }
}
