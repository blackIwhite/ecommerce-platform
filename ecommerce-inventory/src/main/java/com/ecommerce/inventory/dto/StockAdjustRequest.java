package com.ecommerce.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustRequest implements Serializable {

    @NotNull
    private Long skuId;

    @NotNull
    private Integer adjustQuantity;

    private String reason;
}
