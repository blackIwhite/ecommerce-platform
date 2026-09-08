package com.ecommerce.order.controller;

import com.ecommerce.common.core.annotation.AuditLog;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.common.web.annotation.RequireLogin;
import com.ecommerce.common.web.context.UserContextHolder;
import com.ecommerce.order.dto.OrderCancelRequest;
import com.ecommerce.order.dto.OrderConfirmResponse;
import com.ecommerce.order.dto.OrderDTO;
import com.ecommerce.order.dto.OrderPageRequest;
import com.ecommerce.order.dto.OrderSubmitRequest;
import com.ecommerce.order.dto.LogisticsTraceDTO;
import com.ecommerce.order.dto.PaymentDTO;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.order.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    @RequireLogin
    @PostMapping("/confirm")
    public Result<OrderConfirmResponse> confirmOrder(@RequestBody @Valid OrderSubmitRequest request) {
        return Result.success(orderService.confirmOrder(request));
    }

    @AuditLog(module = "订单", operation = "提交订单", description = "用户提交订单")
    @RequireLogin
    @PostMapping("/submit")
    public Result<Long> submitOrder(@RequestBody @Valid OrderSubmitRequest request) {
        return Result.success(orderService.submitOrder(request));
    }

    @RequireLogin
    @GetMapping("/list")
    public Result<PageResult<OrderDTO>> listOrders(OrderPageRequest request) {
        request.setUserId(UserContextHolder.getUserId());
        return Result.success(orderService.listOrders(request));
    }

    @GetMapping("/{orderId}")
    public Result<OrderDTO> getOrderDetail(@PathVariable Long orderId) {
        return Result.success(orderService.getOrderDetail(orderId));
    }

    @GetMapping("/status/{orderId}")
    public Result<Integer> getOrderStatus(@PathVariable Long orderId) {
        return Result.success(orderService.getOrderStatus(orderId));
    }

    @AuditLog(module = "订单", operation = "取消订单", description = "用户取消订单")
    @RequireLogin
    @PutMapping("/{orderId}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long orderId,
                                    @RequestBody(required = false) OrderCancelRequest request) {
        String reason = request != null ? request.getCancelReason() : null;
        orderService.cancelOrder(orderId, reason);
        return Result.success();
    }

    @AuditLog(module = "订单", operation = "支付订单", description = "用户支付订单")
    @RequireLogin
    @PutMapping("/{orderId}/pay")
    public Result<Void> payOrder(@PathVariable Long orderId) {
        orderService.payOrder(orderId);
        return Result.success();
    }

    @RequireLogin
    @GetMapping("/payment/{orderId}")
    public Result<PaymentDTO> getPayment(@PathVariable Long orderId) {
        return Result.success(paymentService.getPaymentByOrderId(orderId));
    }

    @RequireLogin
    @PutMapping("/{orderId}/receive")
    public Result<Void> receiveOrder(@PathVariable Long orderId) {
        orderService.receiveOrder(orderId);
        return Result.success();
    }

    @RequireLogin
    @GetMapping("/{orderId}/logistics")
    public Result<LogisticsTraceDTO> getLogisticsTrace(@PathVariable Long orderId) {
        return Result.success(orderService.getLogisticsTrace(orderId));
    }

    @GetMapping("/internal/{orderId}/items")
    public Result<List<com.ecommerce.order.dto.OrderItemDTO>> getOrderItems(@PathVariable Long orderId) {
        return Result.success(orderService.getOrderItems(orderId));
    }
}
