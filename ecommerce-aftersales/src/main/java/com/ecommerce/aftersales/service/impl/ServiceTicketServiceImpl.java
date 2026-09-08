package com.ecommerce.aftersales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.aftersales.entity.ServiceTicket;
import com.ecommerce.aftersales.entity.TicketMessage;
import com.ecommerce.aftersales.mapper.ServiceTicketMapper;
import com.ecommerce.aftersales.mapper.TicketMessageMapper;
import com.ecommerce.aftersales.service.ServiceTicketService;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceTicketServiceImpl implements ServiceTicketService {

    private final ServiceTicketMapper ticketMapper;
    private final TicketMessageMapper messageMapper;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public Long create(Long userId, Map<String, Object> request) {
        String ticketNo = "TK" + System.currentTimeMillis();
        ServiceTicket ticket = new ServiceTicket();
        ticket.setTicketNo(ticketNo);
        ticket.setUserId(userId);
        ticket.setType(Integer.valueOf(request.getOrDefault("type", "1").toString()));
        ticket.setSubject((String) request.get("subject"));
        ticket.setContent((String) request.get("content"));
        ticket.setPriority(Integer.valueOf(request.getOrDefault("priority", "0").toString()));
        ticket.setStatus(0);
        if (request.containsKey("orderId")) {
            ticket.setOrderId(Long.valueOf(request.get("orderId").toString()));
        }
        ticketMapper.insert(ticket);

        TicketMessage msg = new TicketMessage();
        msg.setTicketId(ticket.getId());
        msg.setSenderType(1);
        msg.setSenderId(userId);
        msg.setSenderName("用户");
        msg.setContent(ticket.getSubject() + "\n\n" + ticket.getContent());
        messageMapper.insert(msg);

        return ticket.getId();
    }

    @Override
    public PageResult<Map<String, Object>> listByUser(Long userId, Integer status, int pageNum, int pageSize) {
        LambdaQueryWrapper<ServiceTicket> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceTicket::getUserId, userId);
        if (status != null) wrapper.eq(ServiceTicket::getStatus, status);
        wrapper.orderByDesc(ServiceTicket::getCreateTime);

        Page<ServiceTicket> page = ticketMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        var list = page.getRecords().stream().map(this::toMap).toList();
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public Map<String, Object> getDetail(Long id, Long userId) {
        ServiceTicket ticket = ticketMapper.selectById(id);
        if (ticket == null || !ticket.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.AFTERSALES_NOT_FOUND);
        }
        Map<String, Object> result = toMap(ticket);
        result.put("messages", getMessages(id));
        return result;
    }

    @Override
    @Transactional
    public Long sendMessage(Long ticketId, Long userId, String content) {
        ServiceTicket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null || !ticket.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.AFTERSALES_NOT_FOUND);
        }
        if (ticket.getStatus() == 4) {
            throw new BusinessException(ResultCode.AFTERSALES_STATUS_ERROR);
        }

        TicketMessage msg = new TicketMessage();
        msg.setTicketId(ticketId);
        msg.setSenderType(1);
        msg.setSenderId(userId);
        msg.setSenderName("用户");
        msg.setContent(content);
        messageMapper.insert(msg);

        if (ticket.getStatus() < 2) {
            ticket.setStatus(2);
            ticketMapper.updateById(ticket);
        }
        return msg.getId();
    }

    @Override
    public java.util.List<Map<String, Object>> getMessages(Long ticketId) {
        LambdaQueryWrapper<TicketMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TicketMessage::getTicketId, ticketId);
        wrapper.orderByAsc(TicketMessage::getCreateTime);
        return messageMapper.selectList(wrapper).stream().map(m -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", m.getId());
            map.put("senderType", m.getSenderType());
            map.put("senderId", m.getSenderId());
            map.put("senderName", m.getSenderName());
            map.put("content", m.getContent());
            map.put("createTime", m.getCreateTime().format(DT_FMT));
            return map;
        }).toList();
    }

    @Override
    public PageResult<Map<String, Object>> listForAdmin(Integer type, Integer status, String assignedTo, int pageNum, int pageSize) {
        LambdaQueryWrapper<ServiceTicket> wrapper = new LambdaQueryWrapper<>();
        if (type != null) wrapper.eq(ServiceTicket::getType, type);
        if (status != null) wrapper.eq(ServiceTicket::getStatus, status);
        if (assignedTo != null && !assignedTo.isBlank()) wrapper.eq(ServiceTicket::getAssignedTo, assignedTo);
        wrapper.orderByDesc(ServiceTicket::getCreateTime);

        Page<ServiceTicket> page = ticketMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        var list = page.getRecords().stream().map(this::toMap).toList();
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public Map<String, Object> getAdminDetail(Long id) {
        ServiceTicket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            throw new BusinessException(ResultCode.AFTERSALES_NOT_FOUND);
        }
        Map<String, Object> result = toMap(ticket);
        result.put("messages", getMessages(id));
        return result;
    }

    @Override
    @Transactional
    public void assign(Long id, String agent) {
        ServiceTicket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            throw new BusinessException(ResultCode.AFTERSALES_NOT_FOUND);
        }
        ticket.setAssignedTo(agent);
        ticket.setStatus(1);
        ticketMapper.updateById(ticket);

        TicketMessage msg = new TicketMessage();
        msg.setTicketId(id);
        msg.setSenderType(3);
        msg.setSenderName("系统");
        msg.setContent("工单已分配给客服: " + agent);
        messageMapper.insert(msg);
    }

    @Override
    @Transactional
    public Long adminReply(Long ticketId, String agent, String content) {
        ServiceTicket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) {
            throw new BusinessException(ResultCode.AFTERSALES_NOT_FOUND);
        }

        TicketMessage msg = new TicketMessage();
        msg.setTicketId(ticketId);
        msg.setSenderType(2);
        msg.setSenderName(agent);
        msg.setContent(content);
        messageMapper.insert(msg);

        if (ticket.getStatus() < 2) {
            ticket.setStatus(2);
            ticketMapper.updateById(ticket);
        }
        return msg.getId();
    }

    @Override
    @Transactional
    public void resolve(Long id, String agent) {
        ServiceTicket ticket = ticketMapper.selectById(id);
        if (ticket == null || ticket.getStatus() > 3) {
            throw new BusinessException(ResultCode.AFTERSALES_STATUS_ERROR);
        }
        ticket.setStatus(3);
        ticketMapper.updateById(ticket);

        TicketMessage msg = new TicketMessage();
        msg.setTicketId(id);
        msg.setSenderType(2);
        msg.setSenderName(agent);
        msg.setContent("工单已标记为解决");
        messageMapper.insert(msg);
    }

    @Override
    @Transactional
    public void close(Long id, String agent) {
        ServiceTicket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            throw new BusinessException(ResultCode.AFTERSALES_NOT_FOUND);
        }
        ticket.setStatus(4);
        ticketMapper.updateById(ticket);

        TicketMessage msg = new TicketMessage();
        msg.setTicketId(id);
        msg.setSenderType(3);
        msg.setSenderName("系统");
        msg.setContent("工单已关闭");
        messageMapper.insert(msg);
    }

    private Map<String, Object> toMap(ServiceTicket t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("ticketNo", t.getTicketNo());
        m.put("userId", t.getUserId());
        m.put("type", t.getType());
        m.put("typeName", typeName(t.getType()));
        m.put("subject", t.getSubject());
        m.put("content", t.getContent());
        m.put("priority", t.getPriority());
        m.put("priorityName", priorityName(t.getPriority()));
        m.put("status", t.getStatus());
        m.put("statusName", statusName(t.getStatus()));
        m.put("assignedTo", t.getAssignedTo());
        m.put("orderId", t.getOrderId());
        m.put("createTime", t.getCreateTime().format(DT_FMT));
        m.put("updateTime", t.getUpdateTime().format(DT_FMT));
        return m;
    }

    private String typeName(int type) {
        return switch (type) {
            case 1 -> "一般咨询";
            case 2 -> "订单问题";
            case 3 -> "商品问题";
            case 4 -> "投诉建议";
            default -> "未知";
        };
    }

    private String priorityName(int priority) {
        return switch (priority) {
            case 0 -> "普通";
            case 1 -> "高";
            case 2 -> "紧急";
            default -> "未知";
        };
    }

    private String statusName(int status) {
        return switch (status) {
            case 0 -> "待处理";
            case 1 -> "已分配";
            case 2 -> "处理中";
            case 3 -> "已解决";
            case 4 -> "已关闭";
            default -> "未知";
        };
    }
}
