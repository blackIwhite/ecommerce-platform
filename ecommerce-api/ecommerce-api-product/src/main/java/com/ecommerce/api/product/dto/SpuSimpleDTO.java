package com.ecommerce.api.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpuSimpleDTO implements Serializable {

    private Long spuId;
    private String name;
    private Long categoryId;
    private Integer status;
}
