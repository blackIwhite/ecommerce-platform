package com.ecommerce.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.mybatis.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_shipping_template_rule")
public class ShippingTemplateRule extends BaseEntity {

    private Long templateId;
    private String regionCodes;
    private String regionNames;
    private BigDecimal startThreshold;
    private BigDecimal startFee;
    private BigDecimal additionalThreshold;
    private BigDecimal additionalFee;
}
