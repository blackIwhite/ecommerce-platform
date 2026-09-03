package com.ecommerce.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.order.dto.*;

public interface OrderService {

    OrderConfirmResponse confirmOrder(OrderSubmitRequest request);

    Long submitOrder(OrderSubmitRequest request);

    Page<OrderDTO> listOrders(OrderPageRequest request);

    OrderDTO getOrderDetail(Long orderId);

    Integer getOrderStatus(Long orderId);

    void cancelOrder(Long orderId);

    void payOrder(Long orderId);
}
