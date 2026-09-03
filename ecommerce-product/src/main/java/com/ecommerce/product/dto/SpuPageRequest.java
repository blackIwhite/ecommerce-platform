package com.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpuPageRequest implements Serializable {

    private Long categoryId;
    private String keyword;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
