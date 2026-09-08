package com.ecommerce.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.user.dto.BehaviorRecordRequest;
import com.ecommerce.user.dto.BrowseHistoryDTO;
import com.ecommerce.user.entity.UserBehaviorLog;
import com.ecommerce.user.entity.UserBrowseHistory;
import com.ecommerce.user.mapper.UserBehaviorLogMapper;
import com.ecommerce.user.mapper.UserBrowseHistoryMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserBehaviorServiceImplTest {

    @Mock
    private UserBrowseHistoryMapper userBrowseHistoryMapper;
    @Mock
    private UserBehaviorLogMapper userBehaviorLogMapper;

    @InjectMocks
    private UserBehaviorServiceImpl userBehaviorService;

    private BehaviorRecordRequest request(String action, Long spuId, Integer duration) {
        BehaviorRecordRequest request = new BehaviorRecordRequest();
        request.setAction(action);
        request.setTargetType("spu");
        request.setTargetId(spuId);
        request.setSpuId(spuId);
        request.setDuration(duration);
        request.setExtraData("{\"source\":\"home\"}");
        return request;
    }

    // ---- recordBehavior ----

    @Test
    void recordBehavior_browseWithSpuId_shouldInsertLogAndBrowseHistory() {
        when(userBehaviorLogMapper.insert(any(UserBehaviorLog.class))).thenReturn(1);
        when(userBrowseHistoryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userBrowseHistoryMapper.insert(any(UserBrowseHistory.class))).thenReturn(1);

        userBehaviorService.recordBehavior(100L, request("browse", 9L, 30), "127.0.0.1", "JUnit-Agent");

        ArgumentCaptor<UserBehaviorLog> logCaptor = ArgumentCaptor.forClass(UserBehaviorLog.class);
        verify(userBehaviorLogMapper).insert(logCaptor.capture());
        UserBehaviorLog savedLog = logCaptor.getValue();
        assertEquals(100L, savedLog.getUserId());
        assertEquals("browse", savedLog.getAction());
        assertEquals(9L, savedLog.getTargetId());
        assertEquals("127.0.0.1", savedLog.getIp());
        assertEquals("JUnit-Agent", savedLog.getUserAgent());
        assertNotNull(savedLog.getCreateTime());

        ArgumentCaptor<UserBrowseHistory> historyCaptor = ArgumentCaptor.forClass(UserBrowseHistory.class);
        verify(userBrowseHistoryMapper).insert(historyCaptor.capture());
        assertEquals(9L, historyCaptor.getValue().getSpuId());
        assertEquals(30, historyCaptor.getValue().getDuration());
    }

    @Test
    void recordBehavior_nonBrowseAction_shouldNotTouchBrowseHistory() {
        when(userBehaviorLogMapper.insert(any(UserBehaviorLog.class))).thenReturn(1);

        userBehaviorService.recordBehavior(100L, request("cart", 9L, null), "127.0.0.1", "JUnit-Agent");

        verify(userBehaviorLogMapper).insert(any(UserBehaviorLog.class));
        verifyNoInteractions(userBrowseHistoryMapper);
    }

    // ---- recordBrowse ----

    @Test
    void recordBrowse_existingHistory_shouldRefreshTimeAndDuration() {
        UserBrowseHistory existing = UserBrowseHistory.builder()
                .userId(100L).spuId(9L)
                .browseTime(LocalDateTime.now().minusDays(1))
                .duration(5)
                .build();
        existing.setId(1L);
        when(userBrowseHistoryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
        when(userBrowseHistoryMapper.updateById(any(UserBrowseHistory.class))).thenReturn(1);

        userBehaviorService.recordBrowse(100L, 9L, 42);

        ArgumentCaptor<UserBrowseHistory> captor = ArgumentCaptor.forClass(UserBrowseHistory.class);
        verify(userBrowseHistoryMapper).updateById(captor.capture());
        assertEquals(42, captor.getValue().getDuration());
        assertNotNull(captor.getValue().getBrowseTime());
        verify(userBrowseHistoryMapper, never()).insert(any(UserBrowseHistory.class));
    }

    @Test
    void recordBrowse_nullUserId_shouldDoNothing() {
        userBehaviorService.recordBrowse(null, 9L, 10);

        verifyNoInteractions(userBrowseHistoryMapper);
    }

    // ---- getBrowseHistory ----

    @Test
    void getBrowseHistory_shouldFormatBrowseTime() {
        UserBrowseHistory history = UserBrowseHistory.builder()
                .userId(100L).spuId(9L)
                .browseTime(LocalDateTime.of(2026, 9, 3, 10, 30, 0))
                .duration(15)
                .build();
        history.setId(1L);

        Page<UserBrowseHistory> page = new Page<>(1, 10);
        page.setRecords(List.of(history));
        page.setTotal(1);
        when(userBrowseHistoryMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        PageResult<BrowseHistoryDTO> result = userBehaviorService.getBrowseHistory(100L, 1, 10);

        assertEquals(1, result.getTotal());
        BrowseHistoryDTO dto = result.getList().getFirst();
        assertEquals(9L, dto.getSpuId());
        assertEquals("2026-09-03 10:30:00", dto.getBrowseTime());
        assertEquals(15, dto.getDuration());
    }

    // ---- getBehaviorStats ----

    @Test
    void getBehaviorStats_shouldMapAggregatedRows() {
        Map<String, Object> row1 = new HashMap<>();
        row1.put("action", "browse");
        row1.put("cnt", 7L);
        Map<String, Object> row2 = new HashMap<>();
        row2.put("action", "cart");
        row2.put("cnt", 3);
        Map<String, Object> row3 = new HashMap<>();
        row3.put("action", null);
        row3.put("cnt", 1);
        when(userBehaviorLogMapper.selectMaps(any(QueryWrapper.class)))
                .thenReturn(List.of(row1, row2, row3));

        Map<String, Long> stats = userBehaviorService.getBehaviorStats(100L);

        assertEquals(2, stats.size());
        assertEquals(7L, stats.get("browse"));
        assertEquals(3L, stats.get("cart"));
    }
}
