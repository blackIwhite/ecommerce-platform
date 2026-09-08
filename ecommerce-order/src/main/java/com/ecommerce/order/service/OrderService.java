package com.ecommerce.order.service;

import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.order.dto.LogisticsTraceDTO;
import com.ecommerce.order.dto.OrderConfirmResponse;
import com.ecommerce.order.dto.OrderDTO;
import com.ecommerce.order.dto.OrderPageRequest;
import com.ecommerce.order.dto.OrderShipRequest;
import com.ecommerce.order.dto.OrderSubmitRequest;
import com.ecommerce.order.dto.OrderItemDTO;

import java.util.List;

public interface OrderService {

    OrderConfirmResponse confirmOrder(OrderSubmitRequest request);

    Long submitOrder(OrderSubmitRequest request);

    PageResult<OrderDTO> listOrders(OrderPageRequest request);

    OrderDTO getOrderDetail(Long orderId);

    OrderDTO getOrderDetailAdmin(Long orderId);

    Integer getOrderStatus(Long orderId);

    void cancelOrder(Long orderId, String cancelReason);

    void payOrder(Long orderId);

    void shipOrder(Long orderId, OrderShipRequest request);

    void receiveOrder(Long orderId);

    void autoCancelOrder(Long orderId);

    LogisticsTraceDTO getLogisticsTrace(Long orderId);

    List<OrderItemDTO> getOrderItems(Long orderId);
}
