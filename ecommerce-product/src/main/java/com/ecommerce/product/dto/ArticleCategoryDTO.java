package com.ecommerce.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleCategoryDTO implements Serializable {

    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 50)
    private String code;

    /** Parent category id, 0 for root */
    private Long parentId;
    private Integer sortOrder;
    /** 0=disabled, 1=enabled */
    private Integer status;
    private LocalDateTime createTime;
    private List<ArticleCategoryDTO> children;
}
