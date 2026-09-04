package com.ecommerce.order.controller;

import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.common.web.annotation.RequireLogin;
import com.ecommerce.common.web.context.UserContextHolder;
import com.ecommerce.order.dto.OrderCancelRequest;
import com.ecommerce.order.dto.OrderConfirmResponse;
import com.ecommerce.order.dto.OrderDTO;
import com.ecommerce.order.dto.OrderPageRequest;
import com.ecommerce.order.dto.OrderSubmitRequest;
import com.ecommerce.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @RequireLogin
    @PostMapping("/confirm")
    public Result<OrderConfirmResponse> confirmOrder(@RequestBody @Valid OrderSubmitRequest request) {
        return Result.success(orderService.confirmOrder(request));
    }

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

    @RequireLogin
    @PutMapping("/{orderId}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long orderId,
                                    @RequestBody(required = false) OrderCancelRequest request) {
        String reason = request != null ? request.getCancelReason() : null;
        orderService.cancelOrder(orderId, reason);
        return Result.success();
    }

    @RequireLogin
    @PutMapping("/{orderId}/pay")
    public Result<Void> payOrder(@PathVariable Long orderId) {
        orderService.payOrder(orderId);
        return Result.success();
    }

    @RequireLogin
    @PutMapping("/{orderId}/receive")
    public Result<Void> receiveOrder(@PathVariable Long orderId) {
        orderService.receiveOrder(orderId);
        return Result.success();
    }
}
