package com.ecommerce.marketing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.mybatis.entity.BaseEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_coupon_template")
public class CouponTemplate extends BaseEntity {
    private String name;
    private Integer type;
    private BigDecimal discountValue;
    private BigDecimal minPurchase;
    private BigDecimal maxDiscount;
    private Integer totalCount;
    private Integer claimedCount;
    private Integer perLimit;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private String description;
}
