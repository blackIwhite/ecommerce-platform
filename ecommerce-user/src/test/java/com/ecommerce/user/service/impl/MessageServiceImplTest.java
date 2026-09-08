package com.ecommerce.user.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.user.dto.UnreadCountDTO;
import com.ecommerce.user.entity.UserMessage;
import com.ecommerce.user.mapper.UserMessageMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private UserMessageMapper userMessageMapper;

    @InjectMocks
    private MessageServiceImpl messageService;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), UserMessage.class);
    }

    private UserMessage message(long id, long userId, int type, int isRead) {
        UserMessage msg = UserMessage.builder()
                .userId(userId)
                .type(type)
                .title("title-" + id)
                .content("content")
                .isRead(isRead)
                .build();
        msg.setId(id);
        return msg;
    }

    // ---- getUnreadCount ----

    @Test
    void getUnreadCount_shouldGroupUnreadMessagesByType() {
        when(userMessageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                message(1L, 100L, 1, 0),
                message(2L, 100L, 1, 0),
                message(3L, 100L, 2, 0),
                message(4L, 100L, 3, 0),
                message(5L, 100L, 4, 0)
        ));

        UnreadCountDTO dto = messageService.getUnreadCount(100L);

        assertEquals(5, dto.getTotal());
        assertEquals(2, dto.getSystem());
        assertEquals(1, dto.getOrder());
        assertEquals(1, dto.getPromotion());
        assertEquals(1, dto.getAftersales());
    }

    @Test
    void getUnreadCount_noUnread_shouldReturnZeros() {
        when(userMessageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        UnreadCountDTO dto = messageService.getUnreadCount(100L);

        assertEquals(0, dto.getTotal());
        assertEquals(0, dto.getSystem());
    }

    // ---- markAsRead ----

    @Test
    void markAsRead_ownUnreadMessage_shouldUpdateReadFlagAndTime() {
        when(userMessageMapper.selectById(1L)).thenReturn(message(1L, 100L, 2, 0));
        when(userMessageMapper.updateById(any(UserMessage.class))).thenReturn(1);

        messageService.markAsRead(1L, 100L);

        ArgumentCaptor<UserMessage> captor = ArgumentCaptor.forClass(UserMessage.class);
        verify(userMessageMapper).updateById(captor.capture());
        assertEquals(1, captor.getValue().getIsRead());
        assertNotNull(captor.getValue().getReadTime());
    }

    @Test
    void markAsRead_messageNotFound_shouldThrow() {
        when(userMessageMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> messageService.markAsRead(999L, 100L));
        assertEquals(ResultCode.MESSAGE_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void markAsRead_otherUsersMessage_shouldThrowAccessDenied() {
        when(userMessageMapper.selectById(1L)).thenReturn(message(1L, 200L, 2, 0));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> messageService.markAsRead(1L, 100L));
        assertEquals(ResultCode.MESSAGE_ACCESS_DENIED.getCode(), ex.getCode());
        verify(userMessageMapper, never()).updateById(any(UserMessage.class));
    }

    @Test
    void markAsRead_alreadyRead_shouldSkipUpdate() {
        when(userMessageMapper.selectById(1L)).thenReturn(message(1L, 100L, 2, 1));

        messageService.markAsRead(1L, 100L);

        verify(userMessageMapper, never()).updateById(any(UserMessage.class));
    }

    // ---- markAllAsRead ----

    @Test
    void markAllAsRead_shouldIssueBulkUpdate() {
        when(userMessageMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(3);

        messageService.markAllAsRead(100L);

        verify(userMessageMapper).update(isNull(), any(LambdaUpdateWrapper.class));
    }

    // ---- sendMessage ----

    @Test
    void sendMessage_shouldInsertUnreadMessage() {
        when(userMessageMapper.insert(any(UserMessage.class))).thenReturn(1);

        messageService.sendMessage(100L, 2, "Order shipped", "Your order is on the way", 55L);

        ArgumentCaptor<UserMessage> captor = ArgumentCaptor.forClass(UserMessage.class);
        verify(userMessageMapper).insert(captor.capture());
        UserMessage saved = captor.getValue();
        assertEquals(100L, saved.getUserId());
        assertEquals(2, saved.getType());
        assertEquals("Order shipped", saved.getTitle());
        assertEquals(55L, saved.getReferenceId());
        assertEquals(0, saved.getIsRead());
    }
}
