package com.ecommerce.user.service;

import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.user.dto.BehaviorLogDTO;
import com.ecommerce.user.dto.BehaviorRecordRequest;
import com.ecommerce.user.dto.BrowseHistoryDTO;

import java.util.Map;

public interface UserBehaviorService {

    void recordBrowse(Long userId, Long spuId, Integer duration);

    void recordBehavior(Long userId, BehaviorRecordRequest request, String ip, String userAgent);

    PageResult<BrowseHistoryDTO> getBrowseHistory(Long userId, int pageNum, int pageSize);

    void clearBrowseHistory(Long userId);

    PageResult<BehaviorLogDTO> getBehaviorLogs(Long userId, String action, int pageNum, int pageSize);

    Map<String, Long> getBehaviorStats(Long userId);
}
