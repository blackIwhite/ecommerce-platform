package com.ecommerce.aftersales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.aftersales.entity.AftersalesItem;
import com.ecommerce.aftersales.entity.AftersalesLog;
import com.ecommerce.aftersales.entity.AftersalesOrder;
import com.ecommerce.aftersales.mapper.AftersalesItemMapper;
import com.ecommerce.aftersales.mapper.AftersalesLogMapper;
import com.ecommerce.aftersales.mapper.AftersalesOrderMapper;
import com.ecommerce.aftersales.service.AftersalesService;
import com.ecommerce.api.order.OrderApi;
import com.ecommerce.api.order.dto.OrderDTO;
import com.ecommerce.api.order.dto.OrderItemDTO;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.common.core.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AftersalesServiceImpl implements AftersalesService {

    private final AftersalesOrderMapper aftersalesOrderMapper;
    private final AftersalesItemMapper aftersalesItemMapper;
    private final AftersalesLogMapper aftersalesLogMapper;
    private final OrderApi orderApi;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public Long apply(Long userId, Map<String, Object> request) {
        Long orderId = Long.valueOf(request.get("orderId").toString());
        Integer type = Integer.valueOf(request.get("type").toString());
        String reason = (String) request.get("reason");

        Result<OrderDTO> orderResult = orderApi.getOrderById(orderId);
        if (orderResult == null || orderResult.getCode() != ResultCode.SUCCESS.getCode() || orderResult.getData() == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        OrderDTO order = orderResult.getData();
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (order.getStatus() < 1) {
            throw new BusinessException(ResultCode.AFTERSALES_ORDER_NOT_PAID);
        }

        Result<List<OrderItemDTO>> itemsResult = orderApi.getOrderItems(orderId);
        List<OrderItemDTO> orderItems = (itemsResult != null && itemsResult.getCode() == ResultCode.SUCCESS.getCode() && itemsResult.getData() != null)
                ? itemsResult.getData() : Collections.emptyList();

        String aftersalesNo = "AS" + System.currentTimeMillis();
        BigDecimal refundAmount = orderItems.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        AftersalesOrder aftersalesOrder = new AftersalesOrder();
        aftersalesOrder.setAftersalesNo(aftersalesNo);
        aftersalesOrder.setOrderId(orderId);
        aftersalesOrder.setUserId(userId);
        aftersalesOrder.setType(type);
        aftersalesOrder.setStatus(0);
        aftersalesOrder.setReason(reason);
        aftersalesOrder.setDescription((String) request.getOrDefault("description", ""));
        aftersalesOrder.setImages((String) request.getOrDefault("images", ""));
        aftersalesOrder.setRefundAmount(refundAmount);
        if (type == 2 && request.containsKey("exchangeAddress")) {
            aftersalesOrder.setExchangeAddress((String) request.get("exchangeAddress"));
        }
        aftersalesOrderMapper.insert(aftersalesOrder);

        for (OrderItemDTO oi : orderItems) {
            AftersalesItem item = new AftersalesItem();
            item.setAftersalesId(aftersalesOrder.getId());
            item.setOrderItemId(oi.getId() != null ? oi.getId() : 0L);
            item.setSpuId(oi.getSpuId());
            item.setSkuId(oi.getSkuId());
            item.setProductName(oi.getSkuName());
            item.setSkuName(oi.getSkuName());
            item.setImage(oi.getImage());
            item.setPrice(oi.getPrice());
            item.setQuantity(oi.getQuantity());
            aftersalesItemMapper.insert(item);
        }

        addLog(aftersalesOrder.getId(), -1, 0, "user", "提交售后申请");

        return aftersalesOrder.getId();
    }

    @Override
    @Transactional
    public void cancel(Long id, Long userId) {
        AftersalesOrder order = aftersalesOrderMapper.selectById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.AFTERSALES_NOT_FOUND);
        }
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.AFTERSALES_STATUS_ERROR);
        }
        updateStatus(order, 8, "user", "用户取消");
    }

    @Override
    @Transactional
    public void fillTrackingNo(Long id, Long userId, String trackingNo, String company) {
        AftersalesOrder order = aftersalesOrderMapper.selectById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.AFTERSALES_NOT_FOUND);
        }
        if (order.getStatus() != 1) {
            throw new BusinessException(ResultCode.AFTERSALES_STATUS_ERROR);
        }
        order.setReturnTrackingNo(trackingNo);
        order.setReturnCompany(company);
        updateStatus(order, 2, "user", "用户填写退货物流");
    }

    @Override
    public PageResult<Map<String, Object>> listByUser(Long userId, Integer status, int pageNum, int pageSize) {
        LambdaQueryWrapper<AftersalesOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AftersalesOrder::getUserId, userId);
        if (status != null) {
            wrapper.eq(AftersalesOrder::getStatus, status);
        }
        wrapper.orderByDesc(AftersalesOrder::getCreateTime);

        Page<AftersalesOrder> page = aftersalesOrderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<Map<String, Object>> list = page.getRecords().stream().map(this::toMap).toList();
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public Map<String, Object> getDetail(Long id, Long userId) {
        AftersalesOrder order = aftersalesOrderMapper.selectById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.AFTERSALES_NOT_FOUND);
        }
        Map<String, Object> result = toMap(order);

        LambdaQueryWrapper<AftersalesItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(AftersalesItem::getAftersalesId, id);
        List<AftersalesItem> items = aftersalesItemMapper.selectList(itemWrapper);
        result.put("items", items.stream().map(this::itemToMap).toList());
        result.put("logs", getLogs(id));
        return result;
    }

    @Override
    public PageResult<Map<String, Object>> listForAdmin(Integer type, Integer status, int pageNum, int pageSize) {
        LambdaQueryWrapper<AftersalesOrder> wrapper = new LambdaQueryWrapper<>();
        if (type != null) wrapper.eq(AftersalesOrder::getType, type);
        if (status != null) wrapper.eq(AftersalesOrder::getStatus, status);
        wrapper.orderByDesc(AftersalesOrder::getCreateTime);

        Page<AftersalesOrder> page = aftersalesOrderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<Map<String, Object>> list = page.getRecords().stream().map(this::toMap).toList();
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public Map<String, Object> getAdminDetail(Long id) {
        AftersalesOrder order = aftersalesOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.AFTERSALES_NOT_FOUND);
        }
        Map<String, Object> result = toMap(order);

        LambdaQueryWrapper<AftersalesItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(AftersalesItem::getAftersalesId, id);
        List<AftersalesItem> items = aftersalesItemMapper.selectList(itemWrapper);
        result.put("items", items.stream().map(this::itemToMap).toList());
        result.put("logs", getLogs(id));
        return result;
    }

    @Override
    @Transactional
    public void approve(Long id, String handler) {
        AftersalesOrder order = aftersalesOrderMapper.selectById(id);
        if (order == null || order.getStatus() != 0) {
            throw new BusinessException(ResultCode.AFTERSALES_STATUS_ERROR);
        }
        order.setHandler(handler);
        order.setHandleRemark("审核通过");
        order.setHandleTime(LocalDateTime.now());
        updateStatus(order, 1, handler, "审核通过");
    }

    @Override
    @Transactional
    public void reject(Long id, String handler, String remark) {
        AftersalesOrder order = aftersalesOrderMapper.selectById(id);
        if (order == null || order.getStatus() != 0) {
            throw new BusinessException(ResultCode.AFTERSALES_STATUS_ERROR);
        }
        order.setHandler(handler);
        order.setHandleRemark(remark);
        order.setHandleTime(LocalDateTime.now());
        updateStatus(order, 7, handler, "审核拒绝: " + remark);
    }

    @Override
    @Transactional
    public void confirmReceive(Long id, String handler) {
        AftersalesOrder order = aftersalesOrderMapper.selectById(id);
        if (order == null || order.getStatus() != 2) {
            throw new BusinessException(ResultCode.AFTERSALES_STATUS_ERROR);
        }
        order.setReceiveTime(LocalDateTime.now());
        updateStatus(order, 3, handler, "确认收到退货");
    }

    @Override
    @Transactional
    public void refund(Long id, String handler) {
        AftersalesOrder order = aftersalesOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.AFTERSALES_NOT_FOUND);
        }
        if (order.getStatus() != 1 && order.getStatus() != 3) {
            throw new BusinessException(ResultCode.AFTERSALES_STATUS_ERROR);
        }
        order.setRefundTime(LocalDateTime.now());
        updateStatus(order, 5, handler, "退款完成 (mock)");

        order.setStatus(6);
        order.setCompleteTime(LocalDateTime.now());
        aftersalesOrderMapper.updateById(order);
        addLog(id, 5, 6, handler, "售后完成");
    }

    @Override
    @Transactional
    public void shipExchange(Long id, String handler, String trackingNo, String company) {
        AftersalesOrder order = aftersalesOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.AFTERSALES_NOT_FOUND);
        }
        if (order.getType() != 2 || order.getStatus() != 3) {
            throw new BusinessException(ResultCode.AFTERSALES_STATUS_ERROR);
        }
        order.setExchangeTrackingNo(trackingNo);
        order.setExchangeCompany(company);
        updateStatus(order, 6, handler, "换货已发出");
        order.setCompleteTime(LocalDateTime.now());
        aftersalesOrderMapper.updateById(order);
    }

    @Override
    public List<Map<String, Object>> getLogs(Long aftersalesId) {
        LambdaQueryWrapper<AftersalesLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AftersalesLog::getAftersalesId, aftersalesId);
        wrapper.orderByAsc(AftersalesLog::getCreateTime);
        List<AftersalesLog> logs = aftersalesLogMapper.selectList(wrapper);
        return logs.stream().map(l -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("fromStatus", l.getFromStatus());
            m.put("toStatus", l.getToStatus());
            m.put("toStatusName", statusName(l.getToStatus()));
            m.put("operator", l.getOperator());
            m.put("remark", l.getRemark());
            m.put("createTime", l.getCreateTime().format(DT_FMT));
            return m;
        }).toList();
    }

    private void updateStatus(AftersalesOrder order, int newStatus, String operator, String remark) {
        int oldStatus = order.getStatus();
        order.setStatus(newStatus);
        aftersalesOrderMapper.updateById(order);
        addLog(order.getId(), oldStatus, newStatus, operator, remark);
    }

    private void addLog(Long aftersalesId, int fromStatus, int toStatus, String operator, String remark) {
        AftersalesLog logEntry = new AftersalesLog();
        logEntry.setAftersalesId(aftersalesId);
        logEntry.setFromStatus(fromStatus);
        logEntry.setToStatus(toStatus);
        logEntry.setOperator(operator);
        logEntry.setRemark(remark);
        aftersalesLogMapper.insert(logEntry);
    }

    private Map<String, Object> toMap(AftersalesOrder o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId());
        m.put("aftersalesNo", o.getAftersalesNo());
        m.put("orderId", o.getOrderId());
        m.put("userId", o.getUserId());
        m.put("type", o.getType());
        m.put("typeName", typeName(o.getType()));
        m.put("status", o.getStatus());
        m.put("statusName", statusName(o.getStatus()));
        m.put("reason", o.getReason());
        m.put("description", o.getDescription());
        m.put("images", o.getImages());
        m.put("refundAmount", o.getRefundAmount());
        m.put("returnTrackingNo", o.getReturnTrackingNo());
        m.put("returnCompany", o.getReturnCompany());
        m.put("exchangeTrackingNo", o.getExchangeTrackingNo());
        m.put("exchangeCompany", o.getExchangeCompany());
        m.put("exchangeAddress", o.getExchangeAddress());
        m.put("handler", o.getHandler());
        m.put("handleRemark", o.getHandleRemark());
        m.put("createTime", o.getCreateTime().format(DT_FMT));
        return m;
    }

    private Map<String, Object> itemToMap(AftersalesItem i) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", i.getId());
        m.put("spuId", i.getSpuId());
        m.put("skuId", i.getSkuId());
        m.put("productName", i.getProductName());
        m.put("skuName", i.getSkuName());
        m.put("image", i.getImage());
        m.put("price", i.getPrice());
        m.put("quantity", i.getQuantity());
        return m;
    }

    private String typeName(int type) {
        return switch (type) {
            case 1 -> "退货退款";
            case 2 -> "换货";
            case 3 -> "仅退款";
            default -> "未知";
        };
    }

    private String statusName(int status) {
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "已批准";
            case 2 -> "退货中";
            case 3 -> "已收货";
            case 4 -> "退款中";
            case 5 -> "已退款";
            case 6 -> "已完成";
            case 7 -> "已拒绝";
            case 8 -> "已关闭";
            default -> "未知";
        };
    }
}
