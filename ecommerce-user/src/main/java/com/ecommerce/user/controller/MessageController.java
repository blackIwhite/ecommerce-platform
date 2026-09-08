package com.ecommerce.user.controller;

import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.user.dto.UnreadCountDTO;
import com.ecommerce.user.dto.UserMessageDTO;
import com.ecommerce.user.service.MessageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/message")
@RequiredArgsConstructor
@Tag(name = "消息中心", description = "Message center APIs")
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/list")
    public Result<PageResult<UserMessageDTO>> listMessages(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer isRead) {
        return Result.success(messageService.listMessages(userId, type, isRead, pageNum, pageSize));
    }

    @GetMapping("/unread-count")
    public Result<UnreadCountDTO> getUnreadCount(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(messageService.getUnreadCount(userId));
    }

    @PostMapping("/read/{id}")
    public Result<Void> markAsRead(@RequestHeader("X-User-Id") Long userId,
                                   @PathVariable Long id) {
        messageService.markAsRead(id, userId);
        return Result.success();
    }

    @PostMapping("/read-all")
    public Result<Void> markAllAsRead(@RequestHeader("X-User-Id") Long userId) {
        messageService.markAllAsRead(userId);
        return Result.success();
    }
}
