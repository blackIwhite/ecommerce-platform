package com.ecommerce.product.service;

import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.product.dto.ReviewCreateRequest;
import com.ecommerce.product.dto.ReviewDTO;
import com.ecommerce.product.dto.ReviewStatsDTO;

public interface ReviewService {

    void createReview(ReviewCreateRequest request);

    PageResult<ReviewDTO> listReviews(Long spuId, Integer pageNum, Integer pageSize);

    ReviewStatsDTO getReviewStats(Long spuId);
}
