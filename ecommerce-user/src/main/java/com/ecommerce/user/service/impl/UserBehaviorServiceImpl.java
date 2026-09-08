package com.ecommerce.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.user.dto.BehaviorLogDTO;
import com.ecommerce.user.dto.BehaviorRecordRequest;
import com.ecommerce.user.dto.BrowseHistoryDTO;
import com.ecommerce.user.entity.UserBehaviorLog;
import com.ecommerce.user.entity.UserBrowseHistory;
import com.ecommerce.user.mapper.UserBehaviorLogMapper;
import com.ecommerce.user.mapper.UserBrowseHistoryMapper;
import com.ecommerce.user.service.UserBehaviorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserBehaviorServiceImpl implements UserBehaviorService {

    private final UserBrowseHistoryMapper userBrowseHistoryMapper;
    private final UserBehaviorLogMapper userBehaviorLogMapper;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Map<String, String> ACTION_NAME_MAP = Map.of(
            "browse", "浏览",
            "cart", "加购",
            "favorite", "收藏",
            "share", "分享",
            "search", "搜索",
            "purchase", "购买"
    );

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordBrowse(Long userId, Long spuId, Integer duration) {
        if (userId == null || spuId == null) {
            return;
        }
        LambdaQueryWrapper<UserBrowseHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBrowseHistory::getUserId, userId)
               .eq(UserBrowseHistory::getSpuId, spuId);
        UserBrowseHistory existing = userBrowseHistoryMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setBrowseTime(LocalDateTime.now());
            if (duration != null) {
                existing.setDuration(duration);
            }
            userBrowseHistoryMapper.updateById(existing);
        } else {
            UserBrowseHistory history = UserBrowseHistory.builder()
                    .userId(userId)
                    .spuId(spuId)
                    .browseTime(LocalDateTime.now())
                    .duration(duration != null ? duration : 0)
                    .build();
            userBrowseHistoryMapper.insert(history);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordBehavior(Long userId, BehaviorRecordRequest request, String ip, String userAgent) {
        UserBehaviorLog behaviorLog = UserBehaviorLog.builder()
                .userId(userId)
                .action(request.getAction())
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .extraData(request.getExtraData())
                .ip(ip)
                .userAgent(userAgent)
                .createTime(LocalDateTime.now())
                .build();
        userBehaviorLogMapper.insert(behaviorLog);

        if ("browse".equals(request.getAction()) && request.getSpuId() != null) {
            recordBrowse(userId, request.getSpuId(), request.getDuration());
        }
    }

    @Override
    public PageResult<BrowseHistoryDTO> getBrowseHistory(Long userId, int pageNum, int pageSize) {
        Page<UserBrowseHistory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserBrowseHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBrowseHistory::getUserId, userId)
               .orderByDesc(UserBrowseHistory::getBrowseTime);

        Page<UserBrowseHistory> result = userBrowseHistoryMapper.selectPage(page, wrapper);
        // Product name/image/price would require a Feign call to the product service;
        // left as null for now so the frontend can fetch product details separately.
        List<BrowseHistoryDTO> dtoList = result.getRecords().stream()
                .map(history -> BrowseHistoryDTO.builder()
                        .id(history.getId())
                        .spuId(history.getSpuId())
                        .browseTime(history.getBrowseTime() != null
                                ? history.getBrowseTime().format(DATE_TIME_FORMATTER) : null)
                        .duration(history.getDuration())
                        .build())
                .toList();
        return PageResult.of(dtoList, result.getTotal(), pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearBrowseHistory(Long userId) {
        LambdaQueryWrapper<UserBrowseHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBrowseHistory::getUserId, userId);
        userBrowseHistoryMapper.delete(wrapper);
    }

    @Override
    public PageResult<BehaviorLogDTO> getBehaviorLogs(Long userId, String action, int pageNum, int pageSize) {
        Page<UserBehaviorLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserBehaviorLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBehaviorLog::getUserId, userId);
        if (StringUtils.hasText(action)) {
            wrapper.eq(UserBehaviorLog::getAction, action);
        }
        wrapper.orderByDesc(UserBehaviorLog::getCreateTime);

        Page<UserBehaviorLog> result = userBehaviorLogMapper.selectPage(page, wrapper);
        List<BehaviorLogDTO> dtoList = result.getRecords().stream()
                .map(this::toBehaviorLogDTO)
                .toList();
        return PageResult.of(dtoList, result.getTotal(), pageNum, pageSize);
    }

    @Override
    public Map<String, Long> getBehaviorStats(Long userId) {
        QueryWrapper<UserBehaviorLog> wrapper = new QueryWrapper<>();
        wrapper.select("action", "COUNT(*) AS cnt")
               .eq("user_id", userId)
               .groupBy("action");

        List<Map<String, Object>> rows = userBehaviorLogMapper.selectMaps(wrapper);
        Map<String, Long> stats = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Object action = row.get("action");
            Object cnt = row.get("cnt");
            if (action != null && cnt != null) {
                stats.put(action.toString(), ((Number) cnt).longValue());
            }
        }
        return stats;
    }

    private BehaviorLogDTO toBehaviorLogDTO(UserBehaviorLog behaviorLog) {
        return BehaviorLogDTO.builder()
                .id(behaviorLog.getId())
                .action(behaviorLog.getAction())
                .actionName(ACTION_NAME_MAP.getOrDefault(behaviorLog.getAction(), behaviorLog.getAction()))
                .targetType(behaviorLog.getTargetType())
                .targetId(behaviorLog.getTargetId())
                .extraData(behaviorLog.getExtraData())
                .createTime(behaviorLog.getCreateTime() != null
                        ? behaviorLog.getCreateTime().format(DATE_TIME_FORMATTER) : null)
                .build();
    }
}
