package com.ecommerce.order.controller;

import com.ecommerce.common.core.annotation.AuditLog;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.common.web.annotation.RequireLogin;
import com.ecommerce.common.web.annotation.ShowSensitive;
import com.ecommerce.order.dto.OrderDTO;
import com.ecommerce.order.dto.OrderPageRequest;
import com.ecommerce.order.dto.OrderShipRequest;
import com.ecommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order/admin")
@RequiredArgsConstructor
@RequireLogin
@ShowSensitive
@Tag(name = "订单管理(后台)", description = "Order admin APIs")
public class OrderAdminController {

    private final OrderService orderService;

    @GetMapping("/list")
    public Result<PageResult<OrderDTO>> listOrders(OrderPageRequest request) {
        return Result.success(orderService.listOrders(request));
    }

    @GetMapping("/{orderId}")
    public Result<OrderDTO> getOrderDetail(@PathVariable Long orderId) {
        return Result.success(orderService.getOrderDetailAdmin(orderId));
    }

    @AuditLog(module = "订单", operation = "发货", description = "管理员订单发货")
    @PutMapping("/{orderId}/ship")
    public Result<Void> shipOrder(@PathVariable Long orderId,
                                  @RequestBody @Valid OrderShipRequest request) {
        orderService.shipOrder(orderId, request);
        return Result.success();
    }
}
