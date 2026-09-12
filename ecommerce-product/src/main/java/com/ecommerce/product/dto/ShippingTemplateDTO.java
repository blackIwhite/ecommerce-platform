package com.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingTemplateDTO {
    private Long id;
    private String name;
    private Integer chargeType;
    private BigDecimal defaultFee;
    private BigDecimal freeThreshold;
    private Integer status;
    private String createTime;
    private List<ShippingTemplateRuleDTO> rules;
}
