package com.ecommerce.product.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class ReviewCreateRequest implements Serializable {

    @NotNull
    private Long spuId;

    @NotNull
    private Long skuId;

    @NotNull
    private Long orderId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    private String content;

    private String images;
}
