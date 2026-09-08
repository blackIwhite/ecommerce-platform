package com.ecommerce.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.user.dto.UnreadCountDTO;
import com.ecommerce.user.dto.UserMessageDTO;
import com.ecommerce.user.entity.UserMessage;
import com.ecommerce.user.mapper.UserMessageMapper;
import com.ecommerce.user.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final UserMessageMapper userMessageMapper;

    private static final int TYPE_SYSTEM = 1;
    private static final int TYPE_ORDER = 2;
    private static final int TYPE_PROMOTION = 3;
    private static final int TYPE_AFTERSALES = 4;

    private static final Map<Integer, String> TYPE_NAMES = Map.of(
            TYPE_SYSTEM, "系统通知",
            TYPE_ORDER, "订单消息",
            TYPE_PROMOTION, "促销消息",
            TYPE_AFTERSALES, "售后消息"
    );

    @Override
    public PageResult<UserMessageDTO> listMessages(Long userId, Integer type, Integer isRead, int pageNum, int pageSize) {
        Page<UserMessage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserMessage::getUserId, userId);
        if (type != null) {
            wrapper.eq(UserMessage::getType, type);
        }
        if (isRead != null) {
            wrapper.eq(UserMessage::getIsRead, isRead);
        }
        wrapper.orderByDesc(UserMessage::getCreateTime);

        Page<UserMessage> result = userMessageMapper.selectPage(page, wrapper);
        List<UserMessageDTO> dtoList = result.getRecords().stream()
                .map(this::toDTO)
                .toList();
        return PageResult.of(dtoList, result.getTotal(), pageNum, pageSize);
    }

    @Override
    public UnreadCountDTO getUnreadCount(Long userId) {
        LambdaQueryWrapper<UserMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserMessage::getUserId, userId)
               .eq(UserMessage::getIsRead, 0);
        List<UserMessage> unreadMessages = userMessageMapper.selectList(wrapper);

        return UnreadCountDTO.builder()
                .total(unreadMessages.size())
                .system(countByType(unreadMessages, TYPE_SYSTEM))
                .order(countByType(unreadMessages, TYPE_ORDER))
                .promotion(countByType(unreadMessages, TYPE_PROMOTION))
                .aftersales(countByType(unreadMessages, TYPE_AFTERSALES))
                .build();
    }

    @Override
    public void markAsRead(Long messageId, Long userId) {
        UserMessage message = userMessageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException(ResultCode.MESSAGE_NOT_FOUND);
        }
        if (!message.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.MESSAGE_ACCESS_DENIED);
        }
        if (message.getIsRead() != null && message.getIsRead() == 1) {
            return;
        }
        message.setIsRead(1);
        message.setReadTime(LocalDateTime.now());
        userMessageMapper.updateById(message);
    }

    @Override
    public void markAllAsRead(Long userId) {
        LambdaUpdateWrapper<UserMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserMessage::getUserId, userId)
               .eq(UserMessage::getIsRead, 0)
               .set(UserMessage::getIsRead, 1)
               .set(UserMessage::getReadTime, LocalDateTime.now());
        int updated = userMessageMapper.update(null, wrapper);
        log.info("Marked {} messages as read for user {}", updated, userId);
    }

    @Override
    public void sendMessage(Long userId, Integer type, String title, String content, Long referenceId) {
        UserMessage message = UserMessage.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .content(content)
                .referenceId(referenceId)
                .isRead(0)
                .build();
        userMessageMapper.insert(message);
    }

    private int countByType(List<UserMessage> messages, int type) {
        return (int) messages.stream()
                .filter(m -> m.getType() != null && m.getType() == type)
                .count();
    }

    private UserMessageDTO toDTO(UserMessage message) {
        return UserMessageDTO.builder()
                .id(message.getId())
                .userId(message.getUserId())
                .type(message.getType())
                .typeName(TYPE_NAMES.getOrDefault(message.getType(), ""))
                .title(message.getTitle())
                .content(message.getContent())
                .referenceId(message.getReferenceId())
                .isRead(message.getIsRead())
                .readTime(message.getReadTime())
                .createTime(message.getCreateTime())
                .build();
    }
}
