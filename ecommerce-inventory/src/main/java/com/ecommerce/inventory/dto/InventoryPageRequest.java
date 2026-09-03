package com.ecommerce.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryPageRequest implements Serializable {

    private Long skuId;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
