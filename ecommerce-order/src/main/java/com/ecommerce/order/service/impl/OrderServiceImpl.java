package com.ecommerce.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.api.inventory.InventoryApi;
import com.ecommerce.api.inventory.dto.InventoryItem;
import com.ecommerce.api.inventory.dto.InventoryLockRequest;
import com.ecommerce.api.inventory.dto.InventoryUnlockRequest;
import com.ecommerce.api.product.ProductApi;
import com.ecommerce.api.product.dto.SkuDTO;
import com.ecommerce.api.user.UserApi;
import com.ecommerce.api.user.dto.UserAddressDTO;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.order.dto.*;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.entity.OrderStatusLog;
import com.ecommerce.order.mapper.OrderItemMapper;
import com.ecommerce.order.mapper.OrderMapper;
import com.ecommerce.order.mapper.OrderStatusLogMapper;
import com.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final ProductApi productApi;
    private final UserApi userApi;
    private final InventoryApi inventoryApi;

    @Override
    public OrderConfirmResponse confirmOrder(OrderSubmitRequest request) {
        List<Long> skuIds = request.getItems().stream()
                .map(OrderSubmitRequest.OrderItemRequest::getSkuId)
                .collect(Collectors.toList());

        Result<List<SkuDTO>> skuResult = productApi.getSkuListByIds(skuIds);
        if (skuResult.getCode() != 0 || skuResult.getData() == null) {
            throw new BusinessException("Failed to get product info");
        }

        List<SkuDTO> skus = skuResult.getData();
        List<OrderItemDTO> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderSubmitRequest.OrderItemRequest itemReq : request.getItems()) {
            SkuDTO sku = skus.stream()
                    .filter(s -> s.getSkuId().equals(itemReq.getSkuId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("SKU not found: " + itemReq.getSkuId()));

            BigDecimal itemTotal = sku.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            items.add(OrderItemDTO.builder()
                    .skuId(sku.getSkuId())
                    .skuName(sku.getSkuName())
                    .price(sku.getPrice())
                    .quantity(itemReq.getQuantity())
                    .totalPrice(itemTotal)
                    .image(sku.getImage())
                    .build());
        }

        return OrderConfirmResponse.builder()
                .totalAmount(totalAmount)
                .items(items)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitOrder(OrderSubmitRequest request) {
        // 1. Get SKU info and calculate total
        OrderConfirmResponse confirm = confirmOrder(request);

        // 2. Get user address
        Result<UserAddressDTO> addressResult = userApi.getAddressById(request.getAddressId());
        if (addressResult.getCode() != 0 || addressResult.getData() == null) {
            throw new BusinessException("Address not found");
        }
        UserAddressDTO address = addressResult.getData();
        String fullAddress = address.getProvince() + address.getCity() + address.getDistrict() + address.getDetailAddress();

        // 3. Create order
        String orderNo = generateOrderNo();
        Order order = Order.builder()
                .orderNo(orderNo)
                .userId(request.getUserId())
                .totalAmount(confirm.getTotalAmount())
                .status(0)
                .receiverName(address.getReceiverName())
                .receiverPhone(address.getReceiverPhone())
                .receiverAddress(fullAddress)
                .remark(request.getRemark())
                .build();
        orderMapper.insert(order);

        // 4. Create order items
        for (OrderItemDTO item : confirm.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .orderId(order.getId())
                    .skuId(item.getSkuId())
                    .skuName(item.getSkuName())
                    .price(item.getPrice())
                    .quantity(item.getQuantity())
                    .totalPrice(item.getTotalPrice())
                    .image(item.getImage())
                    .build();
            orderItemMapper.insert(orderItem);
        }

        // 5. Log status
        saveStatusLog(order.getId(), null, 0, "system", "order created");

        // 6. Lock inventory
        List<InventoryItem> inventoryItems = request.getItems().stream()
                .map(item -> InventoryItem.builder()
                        .skuId(item.getSkuId())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        InventoryLockRequest lockRequest = InventoryLockRequest.builder()
                .orderId(order.getId())
                .items(inventoryItems)
                .build();

        Result<Boolean> lockResult = inventoryApi.lockInventory(lockRequest);
        if (lockResult.getCode() != 0 || !Boolean.TRUE.equals(lockResult.getData())) {
            throw new BusinessException("Failed to lock inventory");
        }

        return order.getId();
    }

    @Override
    public Page<OrderDTO> listOrders(OrderPageRequest request) {
        Page<Order> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (request.getUserId() != null) {
            wrapper.eq(Order::getUserId, request.getUserId());
        }
        if (request.getStatus() != null) {
            wrapper.eq(Order::getStatus, request.getStatus());
        }
        wrapper.orderByDesc(Order::getCreateTime);
        Page<Order> orderPage = orderMapper.selectPage(page, wrapper);

        Page<OrderDTO> resultPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        resultPage.setRecords(orderPage.getRecords().stream()
                .map(this::toOrderDTO)
                .collect(Collectors.toList()));
        return resultPage;
    }

    @Override
    public OrderDTO getOrderDetail(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("Order not found");
        }
        OrderDTO dto = toOrderDTO(order);

        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
        dto.setItems(items.stream().map(this::toOrderItemDTO).collect(Collectors.toList()));

        return dto;
    }

    @Override
    public Integer getOrderStatus(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("Order not found");
        }
        return order.getStatus();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("Order not found");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("Only pending payment orders can be cancelled");
        }

        Integer oldStatus = order.getStatus();
        order.setStatus(5);
        orderMapper.updateById(order);
        saveStatusLog(orderId, oldStatus, 5, "system", "order cancelled");

        // Unlock inventory
        InventoryUnlockRequest unlockRequest = InventoryUnlockRequest.builder()
                .orderId(orderId)
                .build();
        inventoryApi.unlockInventory(unlockRequest);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("Order not found");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("Only pending payment orders can be paid");
        }

        Integer oldStatus = order.getStatus();
        order.setStatus(1);
        orderMapper.updateById(order);
        saveStatusLog(orderId, oldStatus, 1, "payment", "payment completed");

        // Deduct inventory
        com.ecommerce.api.inventory.dto.InventoryDeductRequest deductRequest =
                com.ecommerce.api.inventory.dto.InventoryDeductRequest.builder()
                        .orderId(orderId)
                        .build();
        inventoryApi.deductInventory(deductRequest);
    }

    private void saveStatusLog(Long orderId, Integer fromStatus, Integer toStatus, String operator, String remark) {
        OrderStatusLog log = OrderStatusLog.builder()
                .orderId(orderId)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .operator(operator)
                .remark(remark)
                .build();
        orderStatusLogMapper.insert(log);
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(100000, 999999);
        return timestamp + random;
    }

    private OrderDTO toOrderDTO(Order order) {
        return OrderDTO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverAddress(order.getReceiverAddress())
                .remark(order.getRemark())
                .createTime(order.getCreateTime())
                .build();
    }

    private OrderItemDTO toOrderItemDTO(OrderItem item) {
        return OrderItemDTO.builder()
                .skuId(item.getSkuId())
                .skuName(item.getSkuName())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .totalPrice(item.getTotalPrice())
                .image(item.getImage())
                .build();
    }
}
