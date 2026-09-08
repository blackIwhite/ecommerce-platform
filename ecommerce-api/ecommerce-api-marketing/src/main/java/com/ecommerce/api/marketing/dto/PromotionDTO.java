package com.ecommerce.api.marketing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDTO implements Serializable {
    private Long id;
    private String name;
    private Integer type;
    private String ruleJson;
    private List<FullReductionRule> rules;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private String description;
    private LocalDateTime createTime;
    private List<FlashSaleItemDTO> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FullReductionRule implements Serializable {
        private BigDecimal minAmount;
        private BigDecimal reduction;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlashSaleItemDTO implements Serializable {
        private Long id;
        private Long spuId;
        private Long skuId;
        private java.math.BigDecimal flashPrice;
        private Integer totalStock;
        private Integer availableStock;
        private Integer limitPerUser;
    }
}
