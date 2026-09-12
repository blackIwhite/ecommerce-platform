package com.ecommerce.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ShippingTemplateCreateRequest {
    private Long id;

    @NotBlank(message = "模板名称不能为空")
    private String name;

    @NotNull(message = "计费方式不能为空")
    private Integer chargeType;

    @NotNull(message = "默认运费不能为空")
    private BigDecimal defaultFee;

    private BigDecimal freeThreshold;
    private Integer status;
    private List<RuleItem> rules;

    @Data
    public static class RuleItem {
        private Long id;
        private String regionCodes;
        private String regionNames;
        private BigDecimal startThreshold;
        private BigDecimal startFee;
        private BigDecimal additionalThreshold;
        private BigDecimal additionalFee;
    }
}
