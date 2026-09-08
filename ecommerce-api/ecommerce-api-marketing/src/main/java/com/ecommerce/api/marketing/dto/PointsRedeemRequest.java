package com.ecommerce.api.marketing.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PointsRedeemRequest implements Serializable {
    private Long userId;
    private Integer points;
    private Long orderId;
    private String description;
}
