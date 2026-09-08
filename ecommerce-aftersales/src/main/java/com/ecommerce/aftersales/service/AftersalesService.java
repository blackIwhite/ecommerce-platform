package com.ecommerce.aftersales.service;

import com.ecommerce.common.core.page.PageResult;

import java.util.List;
import java.util.Map;

public interface AftersalesService {

    PageResult<Map<String, Object>> listByUser(Long userId, Integer status, int pageNum, int pageSize);

    Map<String, Object> getDetail(Long id, Long userId);

    Long apply(Long userId, Map<String, Object> request);

    void cancel(Long id, Long userId);

    void fillTrackingNo(Long id, Long userId, String trackingNo, String company);

    PageResult<Map<String, Object>> listForAdmin(Integer type, Integer status, int pageNum, int pageSize);

    Map<String, Object> getAdminDetail(Long id);

    void approve(Long id, String handler);

    void reject(Long id, String handler, String remark);

    void confirmReceive(Long id, String handler);

    void refund(Long id, String handler);

    void shipExchange(Long id, String handler, String trackingNo, String company);

    List<Map<String, Object>> getLogs(Long aftersalesId);
}
