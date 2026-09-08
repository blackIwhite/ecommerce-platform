package com.ecommerce.api.marketing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCouponDTO implements Serializable {
    private Long id;
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
    private LocalDateTime createTime;
}
