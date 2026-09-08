package com.ecommerce.aftersales.controller;

import com.ecommerce.aftersales.service.AftersalesService;
import com.ecommerce.common.core.annotation.AuditLog;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.common.web.annotation.RequireLogin;
import com.ecommerce.common.web.context.UserContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/aftersales")
@RequiredArgsConstructor
public class AftersalesController {

    private final AftersalesService aftersalesService;

    @AuditLog(module = "售后", operation = "申请售后", description = "用户申请售后")
    @RequireLogin
    @PostMapping("/apply")
    public Result<Long> apply(@RequestBody Map<String, Object> request) {
        Long userId = UserContextHolder.getUserId();
        return Result.success(aftersalesService.apply(userId, request));
    }

    @RequireLogin
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        aftersalesService.cancel(id, UserContextHolder.getUserId());
        return Result.success();
    }

    @RequireLogin
    @PutMapping("/{id}/tracking")
    public Result<Void> fillTrackingNo(@PathVariable Long id,
                                         @RequestBody Map<String, String> request) {
        aftersalesService.fillTrackingNo(id, UserContextHolder.getUserId(),
                request.get("trackingNo"), request.get("company"));
        return Result.success();
    }

    @RequireLogin
    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(aftersalesService.listByUser(UserContextHolder.getUserId(), status, pageNum, pageSize));
    }

    @RequireLogin
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(aftersalesService.getDetail(id, UserContextHolder.getUserId()));
    }

    @RequireLogin
    @GetMapping("/{id}/logs")
    public Result<List<Map<String, Object>>> logs(@PathVariable Long id) {
        return Result.success(aftersalesService.getLogs(id));
    }
}
