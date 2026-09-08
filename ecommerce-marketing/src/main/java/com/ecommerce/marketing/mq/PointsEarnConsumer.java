package com.ecommerce.marketing.mq;

import com.ecommerce.api.marketing.dto.PointsEarnRequest;
import com.ecommerce.common.mq.message.OrderMessage;
import com.ecommerce.marketing.service.PointsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointsEarnConsumer {

    private final PointsService pointsService;

    private static final BigDecimal EARN_RATIO = BigDecimal.valueOf(1);

    @RabbitListener(queues = "marketing.queue.order-pay-success")
    public void onOrderPaySuccess(OrderMessage message) {
        log.info("Received order pay-success event: orderId={}, userId={}, amount={}",
                message.getOrderId(), message.getUserId(), message.getTotalAmount());

        try {
            int points = message.getTotalAmount().multiply(EARN_RATIO).intValue();
            if (points <= 0) {
                return;
            }

            PointsEarnRequest request = PointsEarnRequest.builder()
                    .userId(message.getUserId())
                    .points(points)
                    .source("order")
                    .referenceId(message.getOrderId())
                    .description("订单支付奖励，订单号：" + message.getOrderId())
                    .build();
            pointsService.earnPoints(request);
        } catch (Exception e) {
            log.error("Failed to award points for order {}: {}", message.getOrderId(), e.getMessage(), e);
        }
    }
}
