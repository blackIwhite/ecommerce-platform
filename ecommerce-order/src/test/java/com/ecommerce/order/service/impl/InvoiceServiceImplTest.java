package com.ecommerce.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.order.dto.InvoiceApplyRequest;
import com.ecommerce.order.dto.InvoiceDTO;
import com.ecommerce.order.entity.Invoice;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.mapper.InvoiceMapper;
import com.ecommerce.order.mapper.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTest {

    @Mock
    private InvoiceMapper invoiceMapper;
    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    private Order paidOrder;

    @BeforeEach
    void setUp() {
        paidOrder = Order.builder()
                .orderNo("202609040000000001")
                .userId(100L)
                .totalAmount(new BigDecimal("1998"))
                .status(2)
                .build();
        paidOrder.setId(1L);
    }

    private InvoiceApplyRequest buildRequest() {
        InvoiceApplyRequest request = new InvoiceApplyRequest();
        request.setOrderId(1L);
        request.setType(2);
        request.setTitle("Acme Ltd");
        request.setTaxNo("91440300MA5XXXXX0X");
        request.setEmail("finance@acme.com");
        request.setPhone("13800138000");
        request.setRemark("monthly");
        return request;
    }

    // ---- applyInvoice ----

    @Test
    void applyInvoice_paidOrder_shouldInsertPendingInvoice() {
        when(orderMapper.selectById(1L)).thenReturn(paidOrder);
        when(invoiceMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(invoiceMapper.insert(any(Invoice.class))).thenReturn(1);

        invoiceService.applyInvoice(buildRequest(), 100L);

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceMapper).insert(captor.capture());
        Invoice saved = captor.getValue();
        assertEquals(1L, saved.getOrderId());
        assertEquals(100L, saved.getUserId());
        assertEquals(2, saved.getType());
        assertEquals("Acme Ltd", saved.getTitle());
        assertEquals(new BigDecimal("1998"), saved.getAmount());
        assertEquals(0, saved.getStatus());
        assertNotNull(saved.getApplyTime());
    }

    @Test
    void applyInvoice_orderNotFound_shouldThrow() {
        when(orderMapper.selectById(1L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> invoiceService.applyInvoice(buildRequest(), 100L));
        assertEquals(ResultCode.ORDER_NOT_FOUND.getCode(), ex.getCode());
        verify(invoiceMapper, never()).insert(any(Invoice.class));
    }

    @Test
    void applyInvoice_otherUsersOrder_shouldThrowAccessDenied() {
        when(orderMapper.selectById(1L)).thenReturn(paidOrder);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> invoiceService.applyInvoice(buildRequest(), 999L));
        assertEquals(ResultCode.ORDER_ACCESS_DENIED.getCode(), ex.getCode());
        verify(invoiceMapper, never()).insert(any(Invoice.class));
    }

    @Test
    void applyInvoice_orderNotPaid_shouldThrow() {
        paidOrder.setStatus(1);
        when(orderMapper.selectById(1L)).thenReturn(paidOrder);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> invoiceService.applyInvoice(buildRequest(), 100L));
        assertEquals(ResultCode.INVOICE_ORDER_NOT_PAID.getCode(), ex.getCode());
        verify(invoiceMapper, never()).insert(any(Invoice.class));
    }

    @Test
    void applyInvoice_invoiceAlreadyExists_shouldThrow() {
        when(orderMapper.selectById(1L)).thenReturn(paidOrder);
        when(invoiceMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> invoiceService.applyInvoice(buildRequest(), 100L));
        assertEquals(ResultCode.INVOICE_ALREADY_EXISTS.getCode(), ex.getCode());
        verify(invoiceMapper, never()).insert(any(Invoice.class));
    }

    // ---- issueInvoice / rejectInvoice ----

    @Test
    void issueInvoice_existingInvoice_shouldMarkIssued() {
        Invoice invoice = Invoice.builder().orderId(1L).userId(100L).status(0).build();
        invoice.setId(10L);
        when(invoiceMapper.selectById(10L)).thenReturn(invoice);
        when(invoiceMapper.updateById(any(Invoice.class))).thenReturn(1);

        invoiceService.issueInvoice(10L, "INV-001", "https://files/inv-001.pdf");

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceMapper).updateById(captor.capture());
        Invoice updated = captor.getValue();
        assertEquals(1, updated.getStatus());
        assertEquals("INV-001", updated.getInvoiceNo());
        assertEquals("https://files/inv-001.pdf", updated.getInvoiceUrl());
        assertNotNull(updated.getIssueTime());
    }

    @Test
    void issueInvoice_missingInvoice_shouldThrow() {
        when(invoiceMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> invoiceService.issueInvoice(999L, "INV-001", "url"));
        assertEquals(ResultCode.INVOICE_NOT_FOUND.getCode(), ex.getCode());
        verify(invoiceMapper, never()).updateById(any(Invoice.class));
    }

    @Test
    void rejectInvoice_existingInvoice_shouldMarkRejectedWithReason() {
        Invoice invoice = Invoice.builder().orderId(1L).userId(100L).status(0).build();
        invoice.setId(10L);
        when(invoiceMapper.selectById(10L)).thenReturn(invoice);
        when(invoiceMapper.updateById(any(Invoice.class))).thenReturn(1);

        invoiceService.rejectInvoice(10L, "tax number invalid");

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceMapper).updateById(captor.capture());
        assertEquals(2, captor.getValue().getStatus());
        assertEquals("tax number invalid", captor.getValue().getRejectReason());
    }

    // ---- getInvoice ----

    @Test
    void getInvoice_existing_shouldEnrichWithOrderNoAndStatusName() {
        Invoice invoice = Invoice.builder()
                .orderId(1L).userId(100L).type(1).title("张三").status(1).build();
        invoice.setId(10L);
        when(invoiceMapper.selectById(10L)).thenReturn(invoice);
        when(orderMapper.selectById(1L)).thenReturn(paidOrder);

        InvoiceDTO dto = invoiceService.getInvoice(10L);

        assertEquals(10L, dto.getId());
        assertEquals("202609040000000001", dto.getOrderNo());
        assertEquals("已开票", dto.getStatusName());
    }

    @Test
    void getInvoice_missing_shouldThrow() {
        when(invoiceMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> invoiceService.getInvoice(999L));
        assertEquals(ResultCode.INVOICE_NOT_FOUND.getCode(), ex.getCode());
    }
}
