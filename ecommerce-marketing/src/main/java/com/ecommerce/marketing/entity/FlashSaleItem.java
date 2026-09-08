package com.ecommerce.marketing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.mybatis.entity.BaseEntity;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_flash_sale_item")
public class FlashSaleItem extends BaseEntity {
    private Long promotionId;
    private Long spuId;
    private Long skuId;
    private BigDecimal flashPrice;
    private Integer totalStock;
    private Integer availableStock;
    private Integer limitPerUser;
}
