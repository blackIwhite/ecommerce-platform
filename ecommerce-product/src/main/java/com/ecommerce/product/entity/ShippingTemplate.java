package com.ecommerce.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.mybatis.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_shipping_template")
public class ShippingTemplate extends BaseEntity {

    private String name;
    /** 1=按件 2=按重量(kg) 3=按体积(m³) */
    private Integer chargeType;
    private java.math.BigDecimal defaultFee;
    private java.math.BigDecimal freeThreshold;
    private Integer status;
}
