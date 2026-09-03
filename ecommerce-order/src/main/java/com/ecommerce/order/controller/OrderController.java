package com.ecommerce.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.api.order.dto.OrderDTO;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.common.web.annotation.RequireLogin;
import com.ecommerce.order.dto.OrderConfirmResponse;
import com.ecommerce.order.dto.OrderPageRequest;
import com.ecommerce.order.dto.OrderSubmitRequest;
import com.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@RequireLogin
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/confirm")
    public Result<OrderConfirmResponse> confirmOrder(@RequestBody OrderSubmitRequest request) {
        return Result.success(orderService.confirmOrder(request));
    }

    @PostMapping("/submit")
    public Result<Long> submitOrder(@RequestBody OrderSubmitRequest request) {
        return Result.success(orderService.submitOrder(request));
    }

    @GetMapping("/list")
    public Result<Page<com.ecommerce.order.dto.OrderDTO>> listOrders(OrderPageRequest request) {
        return Result.success(orderService.listOrders(request));
    }

    @GetMapping("/{orderId}")
    public Result<com.ecommerce.order.dto.OrderDTO> getOrderDetail(@PathVariable Long orderId) {
        return Result.success(orderService.getOrderDetail(orderId));
    }

    @GetMapping("/status/{orderId}")
    public Result<Integer> getOrderStatus(@PathVariable Long orderId) {
        return Result.success(orderService.getOrderStatus(orderId));
    }

    @PutMapping("/{orderId}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return Result.success();
    }

    @PutMapping("/{orderId}/pay")
    public Result<Void> payOrder(@PathVariable Long orderId) {
        orderService.payOrder(orderId);
        return Result.success();
    }
}
