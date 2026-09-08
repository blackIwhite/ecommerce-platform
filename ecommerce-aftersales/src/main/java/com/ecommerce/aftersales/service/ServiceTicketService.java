package com.ecommerce.aftersales.service;

import com.ecommerce.common.core.page.PageResult;

import java.util.List;
import java.util.Map;

public interface ServiceTicketService {

    Long create(Long userId, Map<String, Object> request);

    PageResult<Map<String, Object>> listByUser(Long userId, Integer status, int pageNum, int pageSize);

    Map<String, Object> getDetail(Long id, Long userId);

    Long sendMessage(Long ticketId, Long userId, String content);

    List<Map<String, Object>> getMessages(Long ticketId);

    PageResult<Map<String, Object>> listForAdmin(Integer type, Integer status, String assignedTo, int pageNum, int pageSize);

    Map<String, Object> getAdminDetail(Long id);

    void assign(Long id, String agent);

    Long adminReply(Long ticketId, String agent, String content);

    void resolve(Long id, String agent);

    void close(Long id, String agent);
}
