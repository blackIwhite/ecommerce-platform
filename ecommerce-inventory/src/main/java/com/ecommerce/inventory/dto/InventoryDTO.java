package com.ecommerce.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDTO implements Serializable {

    private Long id;

    private Long skuId;

    private Integer availableStock;

    private Integer lockedStock;

    private Integer version;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
