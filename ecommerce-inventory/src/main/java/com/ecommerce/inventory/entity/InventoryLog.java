package com.ecommerce.inventory.entity;

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
@TableName("t_inventory_log")
public class InventoryLog extends BaseEntity {

    private Long skuId;
    private Long orderId;
    /** LOCK / UNLOCK / DEDUCT */
    private String type;
    private Integer quantity;
    private Integer beforeStock;
    private Integer afterStock;
}
