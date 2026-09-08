package com.ecommerce.user.controller;

import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.user.dto.BehaviorLogDTO;
import com.ecommerce.user.dto.BehaviorRecordRequest;
import com.ecommerce.user.dto.BrowseHistoryDTO;
import com.ecommerce.user.service.UserBehaviorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user/behavior")
@RequiredArgsConstructor
@Tag(name = "用户行为", description = "User behavior tracking APIs")
public class UserBehaviorController {

    private final UserBehaviorService userBehaviorService;

    @PostMapping("/record")
    public Result<Void> recordBehavior(@RequestHeader("X-User-Id") Long userId,
                                       @RequestBody @Valid BehaviorRecordRequest request,
                                       HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        userBehaviorService.recordBehavior(userId, request, ip, userAgent);
        return Result.success();
    }

    @GetMapping("/browse-history")
    public Result<PageResult<BrowseHistoryDTO>> getBrowseHistory(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(userBehaviorService.getBrowseHistory(userId, pageNum, pageSize));
    }

    @DeleteMapping("/browse-history")
    public Result<Void> clearBrowseHistory(@RequestHeader("X-User-Id") Long userId) {
        userBehaviorService.clearBrowseHistory(userId);
        return Result.success();
    }

    @GetMapping("/logs")
    public Result<PageResult<BehaviorLogDTO>> getBehaviorLogs(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(userBehaviorService.getBehaviorLogs(userId, action, pageNum, pageSize));
    }

    @GetMapping("/stats")
    public Result<Map<String, Long>> getBehaviorStats(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(userBehaviorService.getBehaviorStats(userId));
    }
}
