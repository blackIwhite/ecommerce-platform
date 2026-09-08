package com.ecommerce.marketing.controller;

import com.ecommerce.api.marketing.dto.PointsEarnRequest;
import com.ecommerce.api.marketing.dto.PointsRedeemRequest;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.marketing.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/marketing/internal/points")
@RequiredArgsConstructor
public class PointsInternalController {

    private final PointsService pointsService;

    @PostMapping("/earn")
    public Result<Void> earnPoints(@RequestBody PointsEarnRequest request) {
        pointsService.earnPoints(request);
        return Result.success();
    }

    @PostMapping("/redeem")
    public Result<Void> redeemPoints(@RequestBody PointsRedeemRequest request) {
        pointsService.redeemPoints(request);
        return Result.success();
    }
}
