package com.ecommerce.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 200, message = "商品名称不能超过200字")
    private String name;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotNull(message = "品牌不能为空")
    private Long brandId;

    @Size(max = 5000, message = "商品描述不能超过5000字")
    private String description;

    @NotBlank(message = "商品图片不能为空")
    @Size(max = 2000, message = "图片URL过长")
    private String images;

    @NotEmpty(message = "SKU列表不能为空")
    @Valid
    private List<SkuCreateItem> skuList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkuCreateItem implements Serializable {
        @NotBlank(message = "SKU名称不能为空")
        @Size(max = 200)
        private String skuName;

        @NotNull(message = "价格不能为空")
        @DecimalMin(value = "0.01", message = "价格必须大于0")
        private BigDecimal price;

        @NotNull(message = "库存不能为空")
        private Integer stock;

        @Size(max = 500)
        private String image;

        @Size(max = 500)
        private String specs;
    }
}
