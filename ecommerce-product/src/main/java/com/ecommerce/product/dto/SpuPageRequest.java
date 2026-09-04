package com.ecommerce.product.dto;

import com.ecommerce.common.core.page.PageQuery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SpuPageRequest extends PageQuery implements Serializable {

    private Long categoryId;
    private Long brandId;
    private Integer status;
    private String keyword;
}
