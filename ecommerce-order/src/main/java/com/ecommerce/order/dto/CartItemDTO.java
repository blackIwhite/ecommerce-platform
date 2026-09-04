package com.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {

    private Long id;
    private Long skuId;
    private String skuName;
    private BigDecimal price;
    private Integer stock;
    private String image;
    private String specs;
    private Integer quantity;
}
