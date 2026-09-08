package com.ecommerce.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.api.inventory.InventoryApi;
import com.ecommerce.api.inventory.dto.InventoryDeductRequest;
import com.ecommerce.api.inventory.dto.InventoryLockRequest;
import com.ecommerce.api.inventory.dto.InventoryUnlockRequest;
import com.ecommerce.api.marketing.MarketingApi;
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
import com.ecommerce.common.web.context.UserContext;
import com.ecommerce.common.web.context.UserContextHolder;
import com.ecommerce.order.dto.OrderConfirmResponse;
import com.ecommerce.order.dto.OrderDTO;
import com.ecommerce.order.dto.OrderPageRequest;
import com.ecommerce.order.dto.OrderShipRequest;
import com.ecommerce.order.dto.OrderSubmitRequest;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.entity.OrderStatusLog;
import com.ecommerce.order.mapper.OrderItemMapper;
import com.ecommerce.order.mapper.OrderMapper;
import com.ecommerce.order.mapper.OrderStatusLogMapper;
import com.ecommerce.order.mq.OrderMessageProducer;
import com.ecommerce.order.service.PaymentService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderMapper orderMapper;
    @Mock private OrderItemMapper orderItemMapper;
    @Mock private OrderStatusLogMapper orderStatusLogMapper;
    @Mock private ProductApi productApi;
    @Mock private UserApi userApi;
    @Mock private InventoryApi inventoryApi;
    @Mock private RedisUtils redisUtils;
    @Mock private OrderMessageProducer orderMessageProducer;
    @Mock private PaymentService paymentService;
    @Mock private MarketingApi marketingApi;
    @Mock private Counter orderCreatedCounter;
    @Mock private Counter orderPaidCounter;
    @Mock private Counter orderCancelledCounter;
    @Mock private Timer orderProcessingTimer;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order sampleOrder;
    private OrderItem sampleItem;
    private SkuDTO sampleSku;
    private UserAddressDTO sampleAddress;

    @BeforeEach
    void setUp() {
        UserContextHolder.set(UserContext.builder().userId(100L).build());

        sampleOrder = Order.builder()
                .orderNo("202609040000000001").userId(100L).totalAmount(new BigDecimal("1998"))
                .status(0).receiverName("张三").receiverPhone("13800138000")
                .receiverAddress("广东省深圳市南山区科技园路1号").remark("test").build();
        sampleOrder.setId(1L);

        sampleItem = OrderItem.builder()
                .orderId(1L).skuId(1001L).skuName("Phone 128GB")
                .price(new BigDecimal("999")).quantity(2).totalPrice(new BigDecimal("1998"))
                .image("phone.jpg").build();
        sampleItem.setId(1L);

        sampleSku = SkuDTO.builder()
                .skuId(1001L).spuId(1L).skuName("Phone 128GB")
                .price(new BigDecimal("999")).stock(50).image("phone.jpg").build();

        sampleAddress = UserAddressDTO.builder()
                .addressId(10L).userId(100L).receiverName("张三").receiverPhone("13800138000")
                .province("广东省").city("深圳市").district("南山区").detailAddress("科技园路1号")
                .isDefault(1).build();
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    // ---- confirmOrder ----

    @Test
    void confirmOrder_success_shouldCalculateTotals() {
        OrderSubmitRequest request = OrderSubmitRequest.builder()
                .addressId(10L)
                .items(List.of(OrderSubmitRequest.OrderItemRequest.builder().skuId(1001L).quantity(2).build()))
                .build();

        when(productApi.getSkuListByIds(anyList())).thenReturn(Result.success(List.of(sampleSku)));

        OrderConfirmResponse result = orderService.confirmOrder(request);

        assertEquals(new BigDecimal("1998"), result.getTotalAmount());
        assertEquals(1, result.getItems().size());
    }

    @Test
    void confirmOrder_feignFail_shouldThrow() {
        OrderSubmitRequest request = OrderSubmitRequest.builder()
                .addressId(10L)
                .items(List.of(OrderSubmitRequest.OrderItemRequest.builder().skuId(1001L).quantity(1).build()))
                .build();

        when(productApi.getSkuListByIds(anyList())).thenReturn(Result.fail("service down"));

        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.confirmOrder(request));
        assertEquals(ResultCode.ORDER_PRODUCT_QUERY_FAILED.getCode(), ex.getCode());
    }

    @Test
    void confirmOrder_skuNotFound_shouldThrow() {
        OrderSubmitRequest request = OrderSubmitRequest.builder()
                .addressId(10L)
                .items(List.of(OrderSubmitRequest.OrderItemRequest.builder().skuId(9999L).quantity(1).build()))
                .build();

        when(productApi.getSkuListByIds(anyList())).thenReturn(Result.success(List.of(sampleSku)));

        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.confirmOrder(request));
        assertEquals(ResultCode.PRODUCT_NOT_FOUND.getCode(), ex.getCode());
    }

    // ---- submitOrder ----

    @Test
    void submitOrder_success_shouldCreateOrderAndSendMQ() {
        OrderSubmitRequest request = OrderSubmitRequest.builder()
                .addressId(10L)
                .items(List.of(OrderSubmitRequest.OrderItemRequest.builder().skuId(1001L).quantity(2).build()))
                .build();

        when(productApi.getSkuListByIds(anyList())).thenReturn(Result.success(List.of(sampleSku)));
        when(userApi.getAddressById(10L)).thenReturn(Result.success(sampleAddress));
        when(redisUtils.increment(anyString(), anyLong())).thenReturn(1L);
        when(orderMapper.insert(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0); o.setId(1L); return 1;
        });
        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);
        when(inventoryApi.lockInventory(any(InventoryLockRequest.class))).thenReturn(Result.success(true));

        OrderMessage mockMsg = OrderMessage.builder().orderId(1L).build();
        when(orderMessageProducer.buildMessage(any(Order.class), anyList(), isNull())).thenReturn(mockMsg);

        Long orderId = orderService.submitOrder(request);

        assertEquals(1L, orderId);
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderMapper).insert(orderCaptor.capture());
        assertEquals(100L, orderCaptor.getValue().getUserId());
        assertEquals(0, orderCaptor.getValue().getStatus());
        assertTrue(orderCaptor.getValue().getOrderNo().matches("^\\d{18}$"));

        verify(orderMessageProducer).sendOrderCreated(mockMsg);
        verify(orderMessageProducer).sendDelayAutoCancel(mockMsg);
    }

    @Test
    void submitOrder_addressFeignFail_shouldThrow() {
        OrderSubmitRequest request = OrderSubmitRequest.builder()
                .addressId(10L)
                .items(List.of(OrderSubmitRequest.OrderItemRequest.builder().skuId(1001L).quantity(1).build()))
                .build();

        when(productApi.getSkuListByIds(anyList())).thenReturn(Result.success(List.of(sampleSku)));
        when(userApi.getAddressById(10L)).thenReturn(Result.fail("not found"));

        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.submitOrder(request));
        assertEquals(ResultCode.ORDER_ADDRESS_QUERY_FAILED.getCode(), ex.getCode());
    }

    @Test
    void submitOrder_addressNotOwnedByUser_shouldThrow() {
        sampleAddress.setUserId(999L);
        OrderSubmitRequest request = OrderSubmitRequest.builder()
                .addressId(10L)
                .items(List.of(OrderSubmitRequest.OrderItemRequest.builder().skuId(1001L).quantity(1).build()))
                .build();

        when(productApi.getSkuListByIds(anyList())).thenReturn(Result.success(List.of(sampleSku)));
        when(userApi.getAddressById(10L)).thenReturn(Result.success(sampleAddress));

        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.submitOrder(request));
        assertEquals(ResultCode.FORBIDDEN.getCode(), ex.getCode());
    }

    @Test
    void submitOrder_inventoryLockFail_shouldThrow() {
        OrderSubmitRequest request = OrderSubmitRequest.builder()
                .addressId(10L)
                .items(List.of(OrderSubmitRequest.OrderItemRequest.builder().skuId(1001L).quantity(1).build()))
                .build();

        when(productApi.getSkuListByIds(anyList())).thenReturn(Result.success(List.of(sampleSku)));
        when(userApi.getAddressById(10L)).thenReturn(Result.success(sampleAddress));
        when(redisUtils.increment(anyString(), anyLong())).thenReturn(2L);
        when(orderMapper.insert(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0); o.setId(2L); return 1;
        });
        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);
        when(inventoryApi.lockInventory(any(InventoryLockRequest.class))).thenReturn(Result.success(false));

        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.submitOrder(request));
        assertEquals(ResultCode.ORDER_INVENTORY_LOCK_FAILED.getCode(), ex.getCode());
    }

    // ---- generateOrderNo ----

    @Test
    void generateOrderNo_redisIncrement_shouldReturn18Digits() {
        OrderSubmitRequest request = OrderSubmitRequest.builder()
                .addressId(10L)
                .items(List.of(OrderSubmitRequest.OrderItemRequest.builder().skuId(1001L).quantity(1).build()))
                .build();

        when(productApi.getSkuListByIds(anyList())).thenReturn(Result.success(List.of(sampleSku)));
        when(userApi.getAddressById(10L)).thenReturn(Result.success(sampleAddress));
        when(redisUtils.increment(anyString(), anyLong())).thenReturn(7L);
        when(orderMapper.insert(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0); o.setId(3L); return 1;
        });
        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);
        when(inventoryApi.lockInventory(any())).thenReturn(Result.success(true));
        when(orderMessageProducer.buildMessage(any(), anyList(), isNull()))
                .thenReturn(OrderMessage.builder().orderId(3L).build());

        orderService.submitOrder(request);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderMapper).insert(captor.capture());
        String orderNo = captor.getValue().getOrderNo();
        assertEquals(18, orderNo.length());
        assertTrue(orderNo.endsWith("0000000007"));
    }

    @Test
    void generateOrderNo_redisUnavailable_shouldFallback() {
        OrderSubmitRequest request = OrderSubmitRequest.builder()
                .addressId(10L)
                .items(List.of(OrderSubmitRequest.OrderItemRequest.builder().skuId(1001L).quantity(1).build()))
                .build();

        when(productApi.getSkuListByIds(anyList())).thenReturn(Result.success(List.of(sampleSku)));
        when(userApi.getAddressById(10L)).thenReturn(Result.success(sampleAddress));
        when(redisUtils.increment(anyString(), anyLong())).thenReturn(null);
        when(orderMapper.insert(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0); o.setId(4L); return 1;
        });
        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);
        when(inventoryApi.lockInventory(any())).thenReturn(Result.success(true));
        when(orderMessageProducer.buildMessage(any(), anyList(), isNull()))
                .thenReturn(OrderMessage.builder().orderId(4L).build());

        orderService.submitOrder(request);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderMapper).insert(captor.capture());
        assertEquals(20, captor.getValue().getOrderNo().length());
    }

    // ---- listOrders ----

    @Test
    void listOrders_shouldReturnPageResult() {
        OrderPageRequest request = new OrderPageRequest();
        request.setUserId(100L);
        request.setPageNum(1);
        request.setPageSize(10);

        Page<Order> page = new Page<>(1, 10);
        page.setRecords(List.of(sampleOrder));
        page.setTotal(1);

        when(orderMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        PageResult<OrderDTO> result = orderService.listOrders(request);

        assertEquals(1, result.getTotal());
        assertEquals("202609040000000001", result.getList().getFirst().getOrderNo());
    }

    @Test
    void listOrders_emptyResult_shouldReturnEmpty() {
        OrderPageRequest request = new OrderPageRequest();
        request.setPageNum(1);
        request.setPageSize(10);

        Page<Order> page = new Page<>(1, 10);
        page.setRecords(Collections.emptyList());
        page.setTotal(0);

        when(orderMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        PageResult<OrderDTO> result = orderService.listOrders(request);

        assertEquals(0, result.getTotal());
    }

    // ---- getOrderDetail ----

    @Test
    void getOrderDetail_found_shouldReturnWithItems() {
        when(orderMapper.selectById(1L)).thenReturn(sampleOrder);
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(sampleItem));

        OrderDTO result = orderService.getOrderDetail(1L);

        assertEquals("202609040000000001", result.getOrderNo());
        assertEquals(1, result.getItems().size());
    }

    @Test
    void getOrderDetail_notFound_shouldThrow() {
        when(orderMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.getOrderDetail(999L));
        assertEquals(ResultCode.ORDER_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void getOrderDetail_notOwner_shouldThrow() {
        UserContextHolder.set(UserContext.builder().userId(200L).build());
        when(orderMapper.selectById(1L)).thenReturn(sampleOrder);

        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.getOrderDetail(1L));
        assertEquals(ResultCode.ORDER_ACCESS_DENIED.getCode(), ex.getCode());
    }

    // ---- payOrder ----

    @Test
    void payOrder_success_shouldTransitionToPendingShip() {
        when(orderMapper.selectById(1L)).thenReturn(sampleOrder);
        when(inventoryApi.deductInventory(any(InventoryDeductRequest.class))).thenReturn(Result.success(true));
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(sampleItem));

        OrderMessage mockMsg = OrderMessage.builder().orderId(1L).build();
        when(orderMessageProducer.buildMessage(any(Order.class), anyList(), isNull())).thenReturn(mockMsg);

        orderService.payOrder(1L);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderMapper, times(2)).updateById(orderCaptor.capture());
        assertEquals(2, orderCaptor.getAllValues().get(1).getStatus());

        ArgumentCaptor<OrderStatusLog> logCaptor = ArgumentCaptor.forClass(OrderStatusLog.class);
        verify(orderStatusLogMapper, times(2)).insert(logCaptor.capture());
        assertEquals(1, logCaptor.getAllValues().get(0).getToStatus());
        assertEquals(2, logCaptor.getAllValues().get(1).getToStatus());

        verify(orderMessageProducer).sendOrderPaySuccess(mockMsg);
    }

    @Test
    void payOrder_wrongStatus_shouldThrow() {
        sampleOrder.setStatus(2);
        when(orderMapper.selectById(1L)).thenReturn(sampleOrder);

        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.payOrder(1L));
        assertEquals(ResultCode.ORDER_STATUS_ERROR.getCode(), ex.getCode());
    }

    @Test
    void payOrder_deductFail_shouldThrowAndNotUpdateStatus() {
        when(orderMapper.selectById(1L)).thenReturn(sampleOrder);
        when(inventoryApi.deductInventory(any())).thenReturn(Result.fail("deduct error"));

        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.payOrder(1L));
        assertEquals(ResultCode.ORDER_INVENTORY_DEDUCT_FAILED.getCode(), ex.getCode());
        verify(orderMapper, never()).updateById(any(Order.class));
    }

    @Test
    void payOrder_notOwner_shouldThrow() {
        UserContextHolder.set(UserContext.builder().userId(200L).build());
        when(orderMapper.selectById(1L)).thenReturn(sampleOrder);

        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.payOrder(1L));
        assertEquals(ResultCode.ORDER_ACCESS_DENIED.getCode(), ex.getCode());
    }

    // ---- cancelOrder ----

    @Test
    void cancelOrder_success_shouldUnlockAndSendMQ() {
        when(orderMapper.selectById(1L)).thenReturn(sampleOrder);
        when(inventoryApi.unlockInventory(any(InventoryUnlockRequest.class))).thenReturn(Result.success(true));
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(sampleItem));

        OrderMessage mockMsg = OrderMessage.builder().orderId(1L).build();
        when(orderMessageProducer.buildMessage(any(Order.class), anyList(), eq("user cancelled"))).thenReturn(mockMsg);

        orderService.cancelOrder(1L, "user cancelled");

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderMapper).updateById(captor.capture());
        assertEquals(5, captor.getValue().getStatus());
        verify(orderMessageProducer).sendOrderCancelled(mockMsg);
    }

    @Test
    void cancelOrder_wrongStatus_shouldThrow() {
        sampleOrder.setStatus(1);
        when(orderMapper.selectById(1L)).thenReturn(sampleOrder);

        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.cancelOrder(1L, null));
        assertEquals(ResultCode.ORDER_STATUS_ERROR.getCode(), ex.getCode());
    }

    @Test
    void cancelOrder_unlockFail_shouldThrow() {
        when(orderMapper.selectById(1L)).thenReturn(sampleOrder);
        when(inventoryApi.unlockInventory(any())).thenReturn(Result.fail("unlock error"));

        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.cancelOrder(1L, null));
        assertEquals(ResultCode.ORDER_INVENTORY_UNLOCK_FAILED.getCode(), ex.getCode());
    }

    @Test
    void cancelOrder_notOwner_shouldThrow() {
        UserContextHolder.set(UserContext.builder().userId(200L).build());
        when(orderMapper.selectById(1L)).thenReturn(sampleOrder);

        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.cancelOrder(1L, null));
        assertEquals(ResultCode.ORDER_ACCESS_DENIED.getCode(), ex.getCode());
    }

    // ---- shipOrder ----

    @Test
    void shipOrder_success_shouldTransitionToShipped() {
        sampleOrder.setStatus(2);
        when(orderMapper.selectById(1L)).thenReturn(sampleOrder);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        OrderShipRequest request = OrderShipRequest.builder()
                .logisticsCompany("SF").trackingNo("SF123456").build();
        orderService.shipOrder(1L, request);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderMapper).updateById(captor.capture());
        assertEquals(3, captor.getValue().getStatus());

        ArgumentCaptor<OrderStatusLog> logCaptor = ArgumentCaptor.forClass(OrderStatusLog.class);
        verify(orderStatusLogMapper).insert(logCaptor.capture());
        assertTrue(logCaptor.getValue().getRemark().contains("SF123456"));
    }

    @Test
    void shipOrder_wrongStatus_shouldThrow() {
        sampleOrder.setStatus(0);
        when(orderMapper.selectById(1L)).thenReturn(sampleOrder);

        OrderShipRequest request = OrderShipRequest.builder()
                .logisticsCompany("SF").trackingNo("SF123").build();

        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.shipOrder(1L, request));
        assertEquals(ResultCode.ORDER_STATUS_ERROR.getCode(), ex.getCode());
    }

    @Test
    void shipOrder_notFound_shouldThrow() {
        when(orderMapper.selectById(999L)).thenReturn(null);

        OrderShipRequest request = OrderShipRequest.builder()
                .logisticsCompany("SF").trackingNo("SF123").build();

        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.shipOrder(999L, request));
        assertEquals(ResultCode.ORDER_NOT_FOUND.getCode(), ex.getCode());
    }

    // ---- receiveOrder ----

    @Test
    void receiveOrder_success_shouldTransitionToCompleted() {
        sampleOrder.setStatus(3);
        when(orderMapper.selectById(1L)).thenReturn(sampleOrder);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        orderService.receiveOrder(1L);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderMapper).updateById(captor.capture());
        assertEquals(4, captor.getValue().getStatus());
    }

    @Test
    void receiveOrder_wrongStatus_shouldThrow() {
        sampleOrder.setStatus(2);
        when(orderMapper.selectById(1L)).thenReturn(sampleOrder);

        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.receiveOrder(1L));
        assertEquals(ResultCode.ORDER_STATUS_ERROR.getCode(), ex.getCode());
    }

    @Test
    void receiveOrder_notOwner_shouldThrow() {
        UserContextHolder.set(UserContext.builder().userId(200L).build());
        sampleOrder.setStatus(3);
        when(orderMapper.selectById(1L)).thenReturn(sampleOrder);

        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.receiveOrder(1L));
        assertEquals(ResultCode.ORDER_ACCESS_DENIED.getCode(), ex.getCode());
    }

    // ---- autoCancelOrder ----

    @Test
    void autoCancelOrder_pendingPay_shouldCancel() {
        when(orderMapper.selectById(1L)).thenReturn(sampleOrder);
        when(inventoryApi.unlockInventory(any())).thenReturn(Result.success(true));
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(sampleItem));
        when(orderMessageProducer.buildMessage(any(), anyList(), anyString()))
                .thenReturn(OrderMessage.builder().orderId(1L).build());

        orderService.autoCancelOrder(1L);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderMapper).updateById(captor.capture());
        assertEquals(5, captor.getValue().getStatus());
    }

    @Test
    void autoCancelOrder_alreadyPaid_shouldSkip() {
        sampleOrder.setStatus(1);
        when(orderMapper.selectById(1L)).thenReturn(sampleOrder);

        orderService.autoCancelOrder(1L);

        verify(orderMapper, never()).updateById(any(Order.class));
        verify(inventoryApi, never()).unlockInventory(any());
    }

    @Test
    void autoCancelOrder_notFound_shouldSkip() {
        when(orderMapper.selectById(999L)).thenReturn(null);

        orderService.autoCancelOrder(999L);

        verify(orderMapper, never()).updateById(any(Order.class));
    }
}
