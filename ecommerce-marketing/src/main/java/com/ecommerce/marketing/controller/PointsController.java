package com.ecommerce.marketing.controller;

import com.ecommerce.api.marketing.dto.PointsAccountDTO;
import com.ecommerce.api.marketing.dto.PointsLogDTO;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.common.web.annotation.RequireLogin;
import com.ecommerce.common.web.context.UserContextHolder;
import com.ecommerce.marketing.service.PointsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/marketing/points")
@RequiredArgsConstructor
@RequireLogin
@Tag(name = "积分管理", description = "Points management APIs")
public class PointsController {

    private final PointsService pointsService;

    @GetMapping("/account")
    public Result<PointsAccountDTO> getAccount() {
        Long userId = UserContextHolder.getUserId();
        return Result.success(pointsService.getAccount(userId));
    }

    @GetMapping("/logs")
    public Result<PageResult<PointsLogDTO>> getLogs(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContextHolder.getUserId();
        return Result.success(pointsService.getLogs(userId, type, pageNum, pageSize));
    }
}
