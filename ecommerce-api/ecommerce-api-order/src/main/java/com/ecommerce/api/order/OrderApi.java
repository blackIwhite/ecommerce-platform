package com.ecommerce.api.order;

import com.ecommerce.api.order.dto.OrderDTO;
import com.ecommerce.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ecommerce-order")
public interface OrderApi {

    @GetMapping("/order/{orderId}")
    Result<OrderDTO> getOrderById(@PathVariable("orderId") Long orderId);

    @GetMapping("/order/status/{orderId}")
    Result<Integer> getOrderStatus(@PathVariable("orderId") Long orderId);
}
