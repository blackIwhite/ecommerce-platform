package com.ecommerce.aftersales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_aftersales_item")
public class AftersalesItem extends BaseEntity {

    private Long aftersalesId;
    private Long orderItemId;
    private Long spuId;
    private Long skuId;
    private String productName;
    private String skuName;
    private String image;
    private BigDecimal price;
    private Integer quantity;
}
