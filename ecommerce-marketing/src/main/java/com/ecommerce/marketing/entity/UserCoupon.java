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
@TableName("t_user_coupon")
public class UserCoupon extends BaseEntity {
    private Long userId;
    private Long templateId;
    private String couponName;
    private Integer type;
    private BigDecimal discountValue;
    private BigDecimal minPurchase;
    private BigDecimal maxDiscount;
    private Integer status;
    private Long orderId;
    private LocalDateTime useTime;
    private LocalDateTime expireTime;
}
