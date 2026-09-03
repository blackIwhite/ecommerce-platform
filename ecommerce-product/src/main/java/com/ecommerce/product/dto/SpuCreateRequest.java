package com.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpuCreateRequest implements Serializable {

    private Long id;
    private String name;
    private Long categoryId;
    private Long brandId;
    private String description;
    private String images;
    private List<SkuCreateItem> skuList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkuCreateItem implements Serializable {
        private String skuName;
        private BigDecimal price;
        private Integer stock;
        private String image;
        private String specs;
    }
}
