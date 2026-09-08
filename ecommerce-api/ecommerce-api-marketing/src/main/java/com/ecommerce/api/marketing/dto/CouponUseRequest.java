package com.ecommerce.api.marketing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponUseRequest implements Serializable {
    private Long userCouponId;
    private Long userId;
    private BigDecimal orderAmount;
}
