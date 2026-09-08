package com.ecommerce.marketing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.mybatis.entity.BaseEntity;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_points_rule")
public class PointsRule extends BaseEntity {

    private String ruleKey;
    private String ruleValue;
    private String description;
    private Integer status;
}
