package com.ecommerce.order.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.enums.OrderStatus;
import com.ecommerce.order.mapper.OrderMapper;
import com.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderScheduledTask {

    private final OrderMapper orderMapper;
    private final OrderService orderService;

    @Scheduled(fixedRate = 60_000)
    public void autoCancelUnpaidOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, OrderStatus.PENDING_PAY.getCode())
                .lt(Order::getCreateTime, cutoff);
        List<Order> orders = orderMapper.selectList(wrapper);
        if (orders.isEmpty()) {
            return;
        }
        log.info("Found {} unpaid orders to auto-cancel", orders.size());
        for (Order order : orders) {
            try {
                orderService.autoCancelOrder(order.getId());
            } catch (Exception e) {
                log.error("Failed to auto-cancel order {}: {}", order.getId(), e.getMessage());
            }
        }
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void autoConfirmShippedOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(14);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, OrderStatus.SHIPPED.getCode())
                .lt(Order::getUpdateTime, cutoff);
        List<Order> orders = orderMapper.selectList(wrapper);
        if (orders.isEmpty()) {
            return;
        }
        log.info("Found {} shipped orders to auto-confirm", orders.size());
        for (Order order : orders) {
            try {
                orderService.autoConfirmOrder(order.getId());
            } catch (Exception e) {
                log.error("Failed to auto-confirm order {}: {}", order.getId(), e.getMessage());
            }
        }
    }
}
