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
@TableName("t_hot_search")
public class HotSearch extends BaseEntity {

    private String keyword;
    private Integer searchCount;
    /** 1=manually pinned to top */
    private Integer isManual;
    private Integer sortOrder;
    /** 0=disabled, 1=enabled */
    private Integer status;
}
