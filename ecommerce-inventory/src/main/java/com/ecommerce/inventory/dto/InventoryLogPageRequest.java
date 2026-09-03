package com.ecommerce.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryLogPageRequest implements Serializable {

    private Long skuId;

    private String type;

    private Long orderId;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
