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
@TableName("t_article_category")
public class ArticleCategory extends BaseEntity {

    private String name;
    private String code;
    /** Parent category id, 0 for root */
    private Long parentId;
    private Integer sortOrder;
    /** 0=disabled, 1=enabled */
    private Integer status;
}
