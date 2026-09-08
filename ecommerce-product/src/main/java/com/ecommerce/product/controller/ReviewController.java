package com.ecommerce.product.controller;

import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.common.web.annotation.RequireLogin;
import com.ecommerce.product.dto.ReviewCreateRequest;
import com.ecommerce.product.dto.ReviewDTO;
import com.ecommerce.product.dto.ReviewStatsDTO;
import com.ecommerce.product.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @RequireLogin
    @PostMapping("/review")
    public Result<Void> createReview(@Valid @RequestBody ReviewCreateRequest request) {
        reviewService.createReview(request);
        return Result.success();
    }

    @GetMapping("/review/list")
    public Result<PageResult<ReviewDTO>> listReviews(
            @RequestParam Long spuId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(reviewService.listReviews(spuId, pageNum, pageSize));
    }

    @GetMapping("/review/stats")
    public Result<ReviewStatsDTO> getReviewStats(@RequestParam Long spuId) {
        return Result.success(reviewService.getReviewStats(spuId));
    }
}
