package com.ecommerce.api.marketing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CouponTemplateDTO implements Serializable {
    private Long id;

    @NotBlank(message = "优惠券名称不能为空")
    @Size(max = 100, message = "优惠券名称不能超过100字")
    private String name;

    @NotNull(message = "优惠券类型不能为空")
    private Integer type;

    @NotNull(message = "优惠金额不能为空")
    @DecimalMin(value = "0.01", message = "优惠金额必须大于0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0", message = "最低消费金额不能小于0")
    private BigDecimal minPurchase;

    @DecimalMin(value = "0", message = "最大优惠金额不能小于0")
    private BigDecimal maxDiscount;

    @NotNull(message = "发放总量不能为空")
    @Min(value = 1, message = "发放总量至少为1")
    private Integer totalCount;

    private Integer claimedCount;

    @Min(value = 1, message = "每人限领至少1张")
    private Integer perLimit;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    private Integer status;

    @Size(max = 500, message = "描述不能超过500字")
    private String description;

    private LocalDateTime createTime;
}
