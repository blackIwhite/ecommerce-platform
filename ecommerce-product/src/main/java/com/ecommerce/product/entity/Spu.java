package com.ecommerce.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.mybatis.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_spu")
public class Spu extends BaseEntity {

    private String name;
    private Long categoryId;
    private Long brandId;
    private String description;
    /** JSON array of image URLs */
    private String images;
    /** 0=draft, 1=on-shelf, 2=off-shelf */
    private Integer status;
}
