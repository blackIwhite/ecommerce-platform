package com.ecommerce.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.order.dto.InvoiceApplyRequest;
import com.ecommerce.order.dto.InvoiceDTO;
import com.ecommerce.order.entity.Invoice;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.mapper.InvoiceMapper;
import com.ecommerce.order.mapper.OrderMapper;
import com.ecommerce.order.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceMapper invoiceMapper;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyInvoice(InvoiceApplyRequest request, Long userId) {
        Order order = orderMapper.selectById(request.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ORDER_ACCESS_DENIED);
        }
        // status >= 2 means the order has been paid
        if (order.getStatus() == null || order.getStatus() < 2) {
            throw new BusinessException(ResultCode.INVOICE_ORDER_NOT_PAID);
        }
        Long exists = invoiceMapper.selectCount(new LambdaQueryWrapper<Invoice>()
                .eq(Invoice::getOrderId, request.getOrderId()));
        if (exists != null && exists > 0) {
            throw new BusinessException(ResultCode.INVOICE_ALREADY_EXISTS);
        }

        Invoice invoice = Invoice.builder()
                .orderId(request.getOrderId())
                .userId(userId)
                .type(request.getType())
                .title(request.getTitle())
                .taxNo(request.getTaxNo())
                .amount(order.getTotalAmount())
                .email(request.getEmail())
                .phone(request.getPhone())
                .remark(request.getRemark())
                .status(0)
                .applyTime(LocalDateTime.now())
                .build();
        invoiceMapper.insert(invoice);
        log.info("Invoice applied, orderId={}, userId={}, invoiceId={}",
                request.getOrderId(), userId, invoice.getId());
    }

    @Override
    public PageResult<InvoiceDTO> listUserInvoices(Long userId, int pageNum, int pageSize) {
        Page<Invoice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Invoice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invoice::getUserId, userId);
        wrapper.orderByDesc(Invoice::getCreateTime);
        Page<Invoice> invoicePage = invoiceMapper.selectPage(page, wrapper);
        return toPageResult(invoicePage, pageNum, pageSize);
    }

    @Override
    public InvoiceDTO getInvoice(Long id) {
        Invoice invoice = invoiceMapper.selectById(id);
        if (invoice == null) {
            throw new BusinessException(ResultCode.INVOICE_NOT_FOUND);
        }
        Order order = orderMapper.selectById(invoice.getOrderId());
        return toDTO(invoice, order != null ? order.getOrderNo() : null);
    }

    @Override
    public PageResult<InvoiceDTO> listAdminInvoices(int pageNum, int pageSize, Integer status) {
        Page<Invoice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Invoice> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Invoice::getStatus, status);
        }
        wrapper.orderByDesc(Invoice::getCreateTime);
        Page<Invoice> invoicePage = invoiceMapper.selectPage(page, wrapper);
        return toPageResult(invoicePage, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void issueInvoice(Long id, String invoiceNo, String invoiceUrl) {
        Invoice invoice = invoiceMapper.selectById(id);
        if (invoice == null) {
            throw new BusinessException(ResultCode.INVOICE_NOT_FOUND);
        }
        invoice.setStatus(1);
        invoice.setInvoiceNo(invoiceNo);
        invoice.setInvoiceUrl(invoiceUrl);
        invoice.setIssueTime(LocalDateTime.now());
        invoiceMapper.updateById(invoice);
        log.info("Invoice issued, invoiceId={}, invoiceNo={}", id, invoiceNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectInvoice(Long id, String reason) {
        Invoice invoice = invoiceMapper.selectById(id);
        if (invoice == null) {
            throw new BusinessException(ResultCode.INVOICE_NOT_FOUND);
        }
        invoice.setStatus(2);
        invoice.setRejectReason(reason);
        invoiceMapper.updateById(invoice);
        log.info("Invoice rejected, invoiceId={}, reason={}", id, reason);
    }

    private PageResult<InvoiceDTO> toPageResult(Page<Invoice> invoicePage, int pageNum, int pageSize) {
        List<Invoice> records = invoicePage.getRecords();
        if (records == null || records.isEmpty()) {
            return PageResult.of(Collections.emptyList(), invoicePage.getTotal(), pageNum, pageSize);
        }
        List<Long> orderIds = records.stream()
                .map(Invoice::getOrderId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Order> orderMap = orderMapper.selectBatchIds(orderIds).stream()
                .collect(Collectors.toMap(Order::getId, Function.identity(), (a, b) -> a));
        List<InvoiceDTO> dtoList = records.stream()
                .map(invoice -> {
                    Order order = orderMap.get(invoice.getOrderId());
                    return toDTO(invoice, order != null ? order.getOrderNo() : null);
                })
                .collect(Collectors.toList());
        return PageResult.of(dtoList, invoicePage.getTotal(), pageNum, pageSize);
    }

    private InvoiceDTO toDTO(Invoice invoice, String orderNo) {
        return InvoiceDTO.builder()
                .id(invoice.getId())
                .orderId(invoice.getOrderId())
                .orderNo(orderNo)
                .userId(invoice.getUserId())
                .type(invoice.getType())
                .title(invoice.getTitle())
                .taxNo(invoice.getTaxNo())
                .amount(invoice.getAmount())
                .email(invoice.getEmail())
                .phone(invoice.getPhone())
                .status(invoice.getStatus())
                .statusName(statusName(invoice.getStatus()))
                .invoiceNo(invoice.getInvoiceNo())
                .invoiceUrl(invoice.getInvoiceUrl())
                .remark(invoice.getRemark())
                .rejectReason(invoice.getRejectReason())
                .applyTime(invoice.getApplyTime())
                .issueTime(invoice.getIssueTime())
                .createTime(invoice.getCreateTime())
                .build();
    }

    private String statusName(Integer status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case 0 -> "待开票";
            case 1 -> "已开票";
            case 2 -> "已拒绝";
            default -> "未知";
        };
    }
}
