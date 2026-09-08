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
@TableName("t_review")
public class Review extends BaseEntity {

    private Long spuId;
    private Long skuId;
    private Long userId;
    private Long orderId;
    private Integer rating;
    private String content;
    private String images;
}
