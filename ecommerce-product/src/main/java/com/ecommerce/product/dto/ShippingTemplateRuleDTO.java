package com.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingTemplateRuleDTO {
    private Long id;
    private Long templateId;
    private String regionCodes;
    private String regionNames;
    private BigDecimal startThreshold;
    private BigDecimal startFee;
    private BigDecimal additionalThreshold;
    private BigDecimal additionalFee;
}
