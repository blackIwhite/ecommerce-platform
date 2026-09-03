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
public class InventoryLogDTO implements Serializable {

    private Long id;

    private Long skuId;

    private Long orderId;

    private String type;

    private Integer quantity;

    private Integer beforeStock;

    private Integer afterStock;

    private LocalDateTime createTime;
}
