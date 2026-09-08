package com.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatsDTO implements Serializable {

    private Long spuId;
    private BigDecimal avgRating;
    private Integer reviewCount;
    private Map<Integer, Integer> ratingDistribution;
}
