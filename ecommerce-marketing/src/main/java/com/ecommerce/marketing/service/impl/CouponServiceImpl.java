package com.ecommerce.marketing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.api.marketing.dto.CouponTemplateDTO;
import com.ecommerce.api.marketing.dto.CouponUseRequest;
import com.ecommerce.api.marketing.dto.CouponUseResponse;
import com.ecommerce.api.marketing.dto.UserCouponDTO;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.common.web.context.UserContextHolder;
import com.ecommerce.marketing.entity.CouponTemplate;
import com.ecommerce.marketing.entity.UserCoupon;
import com.ecommerce.marketing.mapper.CouponTemplateMapper;
import com.ecommerce.marketing.mapper.UserCouponMapper;
import com.ecommerce.marketing.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponTemplateMapper couponTemplateMapper;
    private final UserCouponMapper userCouponMapper;

    @Override
    public void createTemplate(CouponTemplateDTO dto) {
        CouponTemplate template = CouponTemplate.builder()
                .name(dto.getName())
                .type(dto.getType())
                .discountValue(dto.getDiscountValue())
                .minPurchase(dto.getMinPurchase() != null ? dto.getMinPurchase() : BigDecimal.ZERO)
                .maxDiscount(dto.getMaxDiscount())
                .totalCount(dto.getTotalCount())
                .claimedCount(0)
                .perLimit(dto.getPerLimit() != null ? dto.getPerLimit() : 1)
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .status(dto.getStatus() != null ? dto.getStatus() : 1)
                .description(dto.getDescription())
                .build();
        couponTemplateMapper.insert(template);
    }

    @Override
    public void updateTemplate(CouponTemplateDTO dto) {
        CouponTemplate template = couponTemplateMapper.selectById(dto.getId());
        if (template == null) {
            throw new BusinessException(ResultCode.COUPON_TEMPLATE_NOT_FOUND);
        }
        template.setName(dto.getName());
        template.setType(dto.getType());
        template.setDiscountValue(dto.getDiscountValue());
        template.setMinPurchase(dto.getMinPurchase());
        template.setMaxDiscount(dto.getMaxDiscount());
        template.setTotalCount(dto.getTotalCount());
        template.setPerLimit(dto.getPerLimit());
        template.setStartTime(dto.getStartTime());
        template.setEndTime(dto.getEndTime());
        template.setDescription(dto.getDescription());
        couponTemplateMapper.updateById(template);
    }

    @Override
    public void deleteTemplate(Long id) {
        couponTemplateMapper.deleteById(id);
    }

    @Override
    public void updateTemplateStatus(Long id, Integer status) {
        CouponTemplate template = couponTemplateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(ResultCode.COUPON_TEMPLATE_NOT_FOUND);
        }
        template.setStatus(status);
        couponTemplateMapper.updateById(template);
    }

    @Override
    public PageResult<CouponTemplateDTO> listTemplates(int pageNum, int pageSize, Integer status) {
        LambdaQueryWrapper<CouponTemplate> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(CouponTemplate::getStatus, status);
        }
        wrapper.orderByDesc(CouponTemplate::getCreateTime);
        Page<CouponTemplate> page = couponTemplateMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<CouponTemplateDTO> records = page.getRecords().stream().map(this::toTemplateDTO).toList();
        return PageResult.of(records, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public CouponTemplateDTO getTemplate(Long id) {
        CouponTemplate template = couponTemplateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(ResultCode.COUPON_TEMPLATE_NOT_FOUND);
        }
        return toTemplateDTO(template);
    }

    @Override
    public List<CouponTemplateDTO> listAvailableCoupons() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<CouponTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CouponTemplate::getStatus, 1)
                .le(CouponTemplate::getStartTime, now)
                .ge(CouponTemplate::getEndTime, now)
                .and(w -> w.eq(CouponTemplate::getTotalCount, -1)
                        .or()
                        .apply("claimed_count < total_count"))
                .orderByDesc(CouponTemplate::getCreateTime);
        return couponTemplateMapper.selectList(wrapper).stream().map(this::toTemplateDTO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long claimCoupon(Long templateId) {
        Long userId = UserContextHolder.getUserId();
        CouponTemplate template = couponTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException(ResultCode.COUPON_TEMPLATE_NOT_FOUND);
        }
        if (template.getStatus() != 1) {
            throw new BusinessException(ResultCode.COUPON_TEMPLATE_DISABLED);
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(template.getStartTime()) || now.isAfter(template.getEndTime())) {
            throw new BusinessException(ResultCode.COUPON_NOT_AVAILABLE);
        }
        if (template.getTotalCount() != -1 && template.getClaimedCount() >= template.getTotalCount()) {
            throw new BusinessException(ResultCode.COUPON_OUT_OF_STOCK);
        }

        long claimedByUser = userCouponMapper.selectCount(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .eq(UserCoupon::getTemplateId, templateId));
        if (claimedByUser >= template.getPerLimit()) {
            throw new BusinessException(ResultCode.COUPON_CLAIM_LIMIT_EXCEEDED);
        }

        UserCoupon userCoupon = UserCoupon.builder()
                .userId(userId)
                .templateId(templateId)
                .couponName(template.getName())
                .type(template.getType())
                .discountValue(template.getDiscountValue())
                .minPurchase(template.getMinPurchase())
                .maxDiscount(template.getMaxDiscount())
                .status(0)
                .expireTime(template.getEndTime())
                .build();
        userCouponMapper.insert(userCoupon);

        template.setClaimedCount(template.getClaimedCount() + 1);
        couponTemplateMapper.updateById(template);

        return userCoupon.getId();
    }

    @Override
    public List<UserCouponDTO> getMyCoupons(Integer status) {
        Long userId = UserContextHolder.getUserId();
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId);
        if (status != null) {
            wrapper.eq(UserCoupon::getStatus, status);
        }
        wrapper.orderByDesc(UserCoupon::getCreateTime);
        return userCouponMapper.selectList(wrapper).stream().map(this::toUserCouponDTO).toList();
    }

    @Override
    public CouponUseResponse checkCoupon(Long userCouponId, Long userId, BigDecimal orderAmount) {
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null) {
            throw new BusinessException(ResultCode.USER_COUPON_NOT_FOUND);
        }
        if (!userCoupon.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.USER_COUPON_NOT_FOUND);
        }
        if (userCoupon.getStatus() != 0) {
            throw new BusinessException(ResultCode.USER_COUPON_ALREADY_USED);
        }
        if (LocalDateTime.now().isAfter(userCoupon.getExpireTime())) {
            throw new BusinessException(ResultCode.USER_COUPON_EXPIRED);
        }
        if (orderAmount.compareTo(userCoupon.getMinPurchase()) < 0) {
            throw new BusinessException(ResultCode.COUPON_NOT_USABLE);
        }

        BigDecimal discount = calculateDiscount(userCoupon, orderAmount);
        return CouponUseResponse.builder()
                .userCouponId(userCouponId)
                .templateId(userCoupon.getTemplateId())
                .discountAmount(discount)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CouponUseResponse useCoupon(CouponUseRequest request) {
        UserCoupon userCoupon = userCouponMapper.selectById(request.getUserCouponId());
        if (userCoupon == null) {
            throw new BusinessException(ResultCode.USER_COUPON_NOT_FOUND);
        }
        if (userCoupon.getStatus() != 0) {
            throw new BusinessException(ResultCode.USER_COUPON_ALREADY_USED);
        }
        if (LocalDateTime.now().isAfter(userCoupon.getExpireTime())) {
            throw new BusinessException(ResultCode.USER_COUPON_EXPIRED);
        }

        BigDecimal discount = calculateDiscount(userCoupon, request.getOrderAmount());

        userCoupon.setStatus(1);
        userCoupon.setUseTime(LocalDateTime.now());
        userCouponMapper.updateById(userCoupon);

        return CouponUseResponse.builder()
                .userCouponId(request.getUserCouponId())
                .templateId(userCoupon.getTemplateId())
                .discountAmount(discount)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseCoupon(Long userCouponId) {
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null || userCoupon.getStatus() != 1) {
            return;
        }
        LambdaUpdateWrapper<UserCoupon> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserCoupon::getId, userCouponId)
                .set(UserCoupon::getStatus, 0)
                .set(UserCoupon::getOrderId, null)
                .set(UserCoupon::getUseTime, null);
        userCouponMapper.update(null, wrapper);
    }

    @Override
    public int expireUnusedCoupons() {
        LambdaUpdateWrapper<UserCoupon> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserCoupon::getStatus, 0)
                .lt(UserCoupon::getExpireTime, LocalDateTime.now())
                .set(UserCoupon::getStatus, 2);
        return userCouponMapper.update(null, wrapper);
    }

    private BigDecimal calculateDiscount(UserCoupon userCoupon, BigDecimal orderAmount) {
        BigDecimal discount;
        if (userCoupon.getType() == 1) {
            discount = userCoupon.getDiscountValue();
        } else {
            discount = orderAmount.multiply(userCoupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (userCoupon.getMaxDiscount() != null && discount.compareTo(userCoupon.getMaxDiscount()) > 0) {
                discount = userCoupon.getMaxDiscount();
            }
        }
        if (discount.compareTo(orderAmount) >= 0) {
            discount = orderAmount.subtract(BigDecimal.ONE);
        }
        return discount.max(BigDecimal.ZERO);
    }

    private CouponTemplateDTO toTemplateDTO(CouponTemplate t) {
        return CouponTemplateDTO.builder()
                .id(t.getId())
                .name(t.getName())
                .type(t.getType())
                .discountValue(t.getDiscountValue())
                .minPurchase(t.getMinPurchase())
                .maxDiscount(t.getMaxDiscount())
                .totalCount(t.getTotalCount())
                .claimedCount(t.getClaimedCount())
                .perLimit(t.getPerLimit())
                .startTime(t.getStartTime())
                .endTime(t.getEndTime())
                .status(t.getStatus())
                .description(t.getDescription())
                .createTime(t.getCreateTime())
                .build();
    }

    private UserCouponDTO toUserCouponDTO(UserCoupon uc) {
        return UserCouponDTO.builder()
                .id(uc.getId())
                .userId(uc.getUserId())
                .templateId(uc.getTemplateId())
                .couponName(uc.getCouponName())
                .type(uc.getType())
                .discountValue(uc.getDiscountValue())
                .minPurchase(uc.getMinPurchase())
                .maxDiscount(uc.getMaxDiscount())
                .status(uc.getStatus())
                .orderId(uc.getOrderId())
                .useTime(uc.getUseTime())
                .expireTime(uc.getExpireTime())
                .createTime(uc.getCreateTime())
                .build();
    }
}
