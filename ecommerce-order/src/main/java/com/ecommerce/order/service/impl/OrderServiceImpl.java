package com.ecommerce.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.api.inventory.InventoryApi;
import com.ecommerce.api.inventory.dto.InventoryDeductRequest;
import com.ecommerce.api.inventory.dto.InventoryItem;
import com.ecommerce.api.inventory.dto.InventoryLockRequest;
import com.ecommerce.api.inventory.dto.InventoryUnlockRequest;
import com.ecommerce.api.product.ProductApi;
import com.ecommerce.api.product.dto.SkuDTO;
import com.ecommerce.api.user.UserApi;
import com.ecommerce.api.user.dto.UserAddressDTO;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.common.mq.message.OrderMessage;
import com.ecommerce.common.redis.util.RedisUtils;
import com.ecommerce.common.web.context.UserContextHolder;
import com.ecommerce.order.dto.OrderCancelRequest;
import com.ecommerce.order.dto.OrderConfirmResponse;
import com.ecommerce.order.dto.OrderDTO;
import com.ecommerce.order.dto.OrderItemDTO;
import com.ecommerce.order.dto.OrderPageRequest;
import com.ecommerce.order.dto.OrderShipRequest;
import com.ecommerce.order.dto.OrderSubmitRequest;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.entity.OrderStatusLog;
import com.ecommerce.order.enums.OrderStatus;
import com.ecommerce.order.mapper.OrderItemMapper;
import com.ecommerce.order.mapper.OrderMapper;
import com.ecommerce.order.mapper.OrderStatusLogMapper;
import com.ecommerce.order.mq.OrderMessageProducer;
import com.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

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
    private final RedisUtils redisUtils;
    private final OrderMessageProducer orderMessageProducer;

    private static final String ORDER_NO_SEQ_KEY_PREFIX = "order:no:seq:";

    @Override
    public OrderConfirmResponse confirmOrder(OrderSubmitRequest request) {
        List<Long> skuIds = request.getItems().stream()
                .map(OrderSubmitRequest.OrderItemRequest::getSkuId)
                .toList();

        List<SkuDTO> skus = checkFeignResult(productApi.getSkuListByIds(skuIds), ResultCode.ORDER_PRODUCT_QUERY_FAILED);

        List<OrderItemDTO> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderSubmitRequest.OrderItemRequest itemReq : request.getItems()) {
            SkuDTO sku = skus.stream()
                    .filter(s -> s.getSkuId().equals(itemReq.getSkuId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ResultCode.PRODUCT_NOT_FOUND,
                            "SKU not found: " + itemReq.getSkuId()));

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
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        OrderConfirmResponse confirm = confirmOrder(request);

        UserAddressDTO address = checkFeignResult(userApi.getAddressById(request.getAddressId()),
                ResultCode.ORDER_ADDRESS_QUERY_FAILED);
        if (!userId.equals(address.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Address does not belong to current user");
        }
        String fullAddress = address.getProvince() + address.getCity()
                + address.getDistrict() + address.getDetailAddress();

        String orderNo = generateOrderNo();
        Order order = Order.builder()
                .orderNo(orderNo)
                .userId(userId)
                .totalAmount(confirm.getTotalAmount())
                .status(OrderStatus.PENDING_PAY.getCode())
                .receiverName(address.getReceiverName())
                .receiverPhone(address.getReceiverPhone())
                .receiverAddress(fullAddress)
                .remark(request.getRemark())
                .build();
        orderMapper.insert(order);

        List<OrderItem> orderItems = new ArrayList<>();
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
            orderItems.add(orderItem);
        }

        saveStatusLog(order.getId(), null, OrderStatus.PENDING_PAY.getCode(), "user:" + userId, "order created");

        List<InventoryItem> inventoryItems = request.getItems().stream()
                .map(item -> InventoryItem.builder()
                        .skuId(item.getSkuId())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        InventoryLockRequest lockRequest = InventoryLockRequest.builder()
                .orderId(order.getId())
                .items(inventoryItems)
                .build();

        Boolean locked = checkFeignResult(inventoryApi.lockInventory(lockRequest), ResultCode.ORDER_INVENTORY_LOCK_FAILED);
        if (!Boolean.TRUE.equals(locked)) {
            throw new BusinessException(ResultCode.ORDER_INVENTORY_LOCK_FAILED);
        }

        OrderMessage msg = orderMessageProducer.buildMessage(order, orderItems, null);
        orderMessageProducer.sendOrderCreated(msg);
        orderMessageProducer.sendDelayAutoCancel(msg);

        return order.getId();
    }

    @Override
    public PageResult<OrderDTO> listOrders(OrderPageRequest request) {
        Page<Order> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (request.getUserId() != null) {
            wrapper.eq(Order::getUserId, request.getUserId());
        }
        if (request.getStatus() != null) {
            wrapper.eq(Order::getStatus, request.getStatus());
        }
        if (StringUtils.hasText(request.getOrderNo())) {
            wrapper.eq(Order::getOrderNo, request.getOrderNo());
        }
        wrapper.orderByDesc(Order::getCreateTime);

        Page<Order> orderPage = orderMapper.selectPage(page, wrapper);
        List<OrderDTO> dtoList = orderPage.getRecords().stream()
                .map(this::toOrderDTO)
                .toList();
        return PageResult.of(dtoList, orderPage.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public OrderDTO getOrderDetail(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        checkOwnership(order);
        return buildOrderDetail(order);
    }

    @Override
    public OrderDTO getOrderDetailAdmin(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        return buildOrderDetail(order);
    }

    @Override
    public Integer getOrderStatus(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        return order.getStatus();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, String cancelReason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        checkOwnership(order);
        requireStatus(order, OrderStatus.PENDING_PAY);

        Long userId = UserContextHolder.getUserId();
        doCancel(order, cancelReason != null ? cancelReason : "user cancelled", "user:" + userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        checkOwnership(order);
        requireStatus(order, OrderStatus.PENDING_PAY);

        InventoryDeductRequest deductRequest = InventoryDeductRequest.builder().orderId(orderId).build();
        Boolean deducted = checkFeignResult(inventoryApi.deductInventory(deductRequest), ResultCode.ORDER_INVENTORY_DEDUCT_FAILED);
        if (!Boolean.TRUE.equals(deducted)) {
            throw new BusinessException(ResultCode.ORDER_INVENTORY_DEDUCT_FAILED);
        }

        order.setStatus(OrderStatus.PAID.getCode());
        orderMapper.updateById(order);
        saveStatusLog(orderId, OrderStatus.PENDING_PAY.getCode(), OrderStatus.PAID.getCode(), "payment", "payment completed");

        order.setStatus(OrderStatus.PENDING_SHIP.getCode());
        orderMapper.updateById(order);
        saveStatusLog(orderId, OrderStatus.PAID.getCode(), OrderStatus.PENDING_SHIP.getCode(), "system", "awaiting shipment");

        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
        OrderMessage msg = orderMessageProducer.buildMessage(order, items, null);
        orderMessageProducer.sendOrderPaySuccess(msg);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Long orderId, OrderShipRequest request) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!OrderStatus.from(order.getStatus()).canTransitionTo(OrderStatus.SHIPPED)) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR,
                    "Current status: " + order.getStatus());
        }

        Long userId = UserContextHolder.getUserId();
        order.setStatus(OrderStatus.SHIPPED.getCode());
        orderMapper.updateById(order);
        saveStatusLog(orderId, OrderStatus.PENDING_SHIP.getCode(), OrderStatus.SHIPPED.getCode(),
                "admin:" + userId, request.getLogisticsCompany() + " " + request.getTrackingNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        checkOwnership(order);
        requireStatus(order, OrderStatus.SHIPPED);

        Long userId = UserContextHolder.getUserId();
        order.setStatus(OrderStatus.COMPLETED.getCode());
        orderMapper.updateById(order);
        saveStatusLog(orderId, OrderStatus.SHIPPED.getCode(), OrderStatus.COMPLETED.getCode(),
                "user:" + userId, "order received");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoCancelOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != OrderStatus.PENDING_PAY.getCode()) {
            log.info("Auto-cancel skipped, orderId={}, status={}", orderId,
                    order != null ? order.getStatus() : "not found");
            return;
        }
        doCancel(order, "Payment timeout auto-cancel", "system");
    }

    private void doCancel(Order order, String reason, String operator) {
        order.setStatus(OrderStatus.CANCELLED.getCode());
        orderMapper.updateById(order);
        saveStatusLog(order.getId(), OrderStatus.PENDING_PAY.getCode(), OrderStatus.CANCELLED.getCode(),
                operator, reason);

        InventoryUnlockRequest unlockRequest = InventoryUnlockRequest.builder()
                .orderId(order.getId()).build();
        Boolean unlocked = checkFeignResult(inventoryApi.unlockInventory(unlockRequest),
                ResultCode.ORDER_INVENTORY_UNLOCK_FAILED);
        if (!Boolean.TRUE.equals(unlocked)) {
            throw new BusinessException(ResultCode.ORDER_INVENTORY_UNLOCK_FAILED);
        }

        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, order.getId());
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
        OrderMessage msg = orderMessageProducer.buildMessage(order, items, reason);
        orderMessageProducer.sendOrderCancelled(msg);
    }

    private void checkOwnership(Order order) {
        Long currentUserId = UserContextHolder.getUserId();
        if (currentUserId != null && !currentUserId.equals(order.getUserId())) {
            throw new BusinessException(ResultCode.ORDER_ACCESS_DENIED);
        }
    }

    private void requireStatus(Order order, OrderStatus expected) {
        if (order.getStatus() != expected.getCode()) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR,
                    "Expected status " + expected.getCode() + ", current: " + order.getStatus());
        }
    }

    private <T> T checkFeignResult(Result<T> result, ResultCode failCode) {
        if (result == null || result.getCode() != ResultCode.SUCCESS.getCode() || result.getData() == null) {
            log.error("Feign call failed, code={}", result != null ? result.getCode() : null);
            throw new BusinessException(failCode);
        }
        return result.getData();
    }

    private OrderDTO buildOrderDetail(Order order) {
        OrderDTO dto = toOrderDTO(order);
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, order.getId());
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
        dto.setItems(items.stream().map(this::toOrderItemDTO).toList());
        return dto;
    }

    private String generateOrderNo() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String seqKey = ORDER_NO_SEQ_KEY_PREFIX + datePrefix;
        Long seq = redisUtils.increment(seqKey, 1);
        if (seq != null && seq == 1L) {
            redisUtils.expire(seqKey, 2, TimeUnit.DAYS);
        }
        if (seq == null) {
            log.warn("Redis unavailable for order no generation, using fallback");
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            int random = ThreadLocalRandom.current().nextInt(100000, 999999);
            return timestamp + random;
        }
        return datePrefix + String.format("%010d", seq);
    }

    private void saveStatusLog(Long orderId, Integer fromStatus, Integer toStatus, String operator, String remark) {
        OrderStatusLog statusLog = OrderStatusLog.builder()
                .orderId(orderId)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .operator(operator)
                .remark(remark)
                .build();
        orderStatusLogMapper.insert(statusLog);
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
