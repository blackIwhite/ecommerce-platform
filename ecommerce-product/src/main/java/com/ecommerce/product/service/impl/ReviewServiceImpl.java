package com.ecommerce.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.api.order.OrderApi;
import com.ecommerce.api.order.dto.OrderDTO;
import com.ecommerce.api.user.UserApi;
import com.ecommerce.api.user.dto.UserDTO;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.common.web.context.UserContextHolder;
import com.ecommerce.product.dto.ReviewCreateRequest;
import com.ecommerce.product.dto.ReviewDTO;
import com.ecommerce.product.dto.ReviewStatsDTO;
import com.ecommerce.product.entity.Review;
import com.ecommerce.product.entity.Spu;
import com.ecommerce.product.mapper.ReviewMapper;
import com.ecommerce.product.mapper.SpuMapper;
import com.ecommerce.product.service.ReviewService;
import com.ecommerce.product.service.SensitiveWordFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final SpuMapper spuMapper;
    private final OrderApi orderApi;
    private final UserApi userApi;
    private final SensitiveWordFilter sensitiveWordFilter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createReview(ReviewCreateRequest request) {
        Long currentUserId = UserContextHolder.getUserId();

        Result<OrderDTO> orderResult = orderApi.getOrderById(request.getOrderId());
        if (orderResult == null || orderResult.getData() == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        OrderDTO order = orderResult.getData();
        if (!currentUserId.equals(order.getUserId())) {
            throw new BusinessException(ResultCode.REVIEW_ORDER_ACCESS_DENIED);
        }
        if (order.getStatus() == null || order.getStatus() != 4) {
            throw new BusinessException(ResultCode.REVIEW_ORDER_NOT_COMPLETED);
        }

        LambdaQueryWrapper<Review> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(Review::getSpuId, request.getSpuId())
                .eq(Review::getUserId, currentUserId)
                .eq(Review::getOrderId, request.getOrderId());
        if (reviewMapper.selectCount(existWrapper) > 0) {
            throw new BusinessException(ResultCode.REVIEW_ALREADY_EXISTS);
        }

        String content = request.getContent();
        if (content != null && sensitiveWordFilter.containsSensitiveWord(content)) {
            content = sensitiveWordFilter.filter(content);
        }

        Review review = Review.builder()
                .spuId(request.getSpuId())
                .skuId(request.getSkuId())
                .userId(currentUserId)
                .orderId(request.getOrderId())
                .rating(request.getRating())
                .content(content)
                .images(request.getImages())
                .build();
        reviewMapper.insert(review);

        updateSpuRating(request.getSpuId());
    }

    @Override
    public PageResult<ReviewDTO> listReviews(Long spuId, Integer pageNum, Integer pageSize) {
        Page<Review> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getSpuId, spuId)
                .orderByDesc(Review::getCreateTime);
        Page<Review> reviewPage = reviewMapper.selectPage(page, wrapper);

        List<Review> reviews = reviewPage.getRecords();
        if (reviews.isEmpty()) {
            return PageResult.of(Collections.emptyList(), reviewPage.getTotal(), pageNum, pageSize);
        }

        Set<Long> userIds = reviews.stream().map(Review::getUserId).collect(Collectors.toSet());
        Map<Long, UserDTO> userMap = new HashMap<>();
        for (Long userId : userIds) {
            try {
                Result<UserDTO> userResult = userApi.getUserById(userId);
                if (userResult != null && userResult.getData() != null) {
                    userMap.put(userId, userResult.getData());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch user info for userId={}: {}", userId, e.getMessage());
            }
        }

        List<ReviewDTO> dtoList = reviews.stream().map(review -> {
            UserDTO user = userMap.get(review.getUserId());
            return ReviewDTO.builder()
                    .id(review.getId())
                    .spuId(review.getSpuId())
                    .skuId(review.getSkuId())
                    .userId(review.getUserId())
                    .nickname(user != null ? user.getNickname() : null)
                    .avatar(user != null ? user.getAvatar() : null)
                    .rating(review.getRating())
                    .content(review.getContent())
                    .images(review.getImages())
                    .createTime(review.getCreateTime())
                    .build();
        }).collect(Collectors.toList());

        return PageResult.of(dtoList, reviewPage.getTotal(), pageNum, pageSize);
    }

    @Override
    public ReviewStatsDTO getReviewStats(Long spuId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getSpuId, spuId);
        List<Review> reviews = reviewMapper.selectList(wrapper);

        Map<Integer, Integer> distribution = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0);
        }
        double totalRating = 0;
        for (Review review : reviews) {
            totalRating += review.getRating();
            distribution.merge(review.getRating(), 1, Integer::sum);
        }

        BigDecimal avgRating = reviews.isEmpty() ? null
                : BigDecimal.valueOf(totalRating / reviews.size()).setScale(1, RoundingMode.HALF_UP);

        return ReviewStatsDTO.builder()
                .spuId(spuId)
                .avgRating(avgRating)
                .reviewCount(reviews.size())
                .ratingDistribution(distribution)
                .build();
    }

    private void updateSpuRating(Long spuId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getSpuId, spuId);
        List<Review> reviews = reviewMapper.selectList(wrapper);

        double totalRating = reviews.stream().mapToInt(Review::getRating).sum();
        BigDecimal avgRating = BigDecimal.valueOf(totalRating / reviews.size())
                .setScale(1, RoundingMode.HALF_UP);

        Spu spu = spuMapper.selectById(spuId);
        if (spu != null) {
            spu.setAvgRating(avgRating);
            spu.setReviewCount(reviews.size());
            spuMapper.updateById(spu);
        }
    }
}
