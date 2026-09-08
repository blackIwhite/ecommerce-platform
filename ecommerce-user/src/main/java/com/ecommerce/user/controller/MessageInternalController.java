package com.ecommerce.user.controller;

import com.ecommerce.common.core.result.Result;
import com.ecommerce.user.dto.MessageSendRequest;
import com.ecommerce.user.service.MessageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal message API for other services (via Feign) to send messages to users.
 */
@RestController
@RequestMapping("/user/internal/message")
@RequiredArgsConstructor
@Tag(name = "消息内部接口", description = "Message internal APIs")
public class MessageInternalController {

    private final MessageService messageService;

    @PostMapping("/send")
    public Result<Void> sendMessage(@RequestBody @Valid MessageSendRequest request) {
        messageService.sendMessage(
                request.getUserId(),
                request.getType(),
                request.getTitle(),
                request.getContent(),
                request.getReferenceId());
        return Result.success();
    }
}
