package com.ecommerce.user.service;

import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.user.dto.UnreadCountDTO;
import com.ecommerce.user.dto.UserMessageDTO;

public interface MessageService {

    PageResult<UserMessageDTO> listMessages(Long userId, Integer type, Integer isRead, int pageNum, int pageSize);

    UnreadCountDTO getUnreadCount(Long userId);

    void markAsRead(Long messageId, Long userId);

    void markAllAsRead(Long userId);

    void sendMessage(Long userId, Integer type, String title, String content, Long referenceId);
}
