package com.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpuDTO implements Serializable {

    private Long spuId;
    private String name;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private String description;
    private String images;
    private Integer status;
    private Integer salesCount;
    private Integer viewCount;
    private BigDecimal minPrice;
    private BigDecimal avgRating;
    private Integer reviewCount;
    private LocalDateTime createTime;
    private List<SkuDTO> skuList;
}
