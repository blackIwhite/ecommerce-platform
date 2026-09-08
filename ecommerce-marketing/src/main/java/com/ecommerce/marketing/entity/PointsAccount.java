package com.ecommerce.marketing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.mybatis.entity.BaseEntity;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_points_account")
public class PointsAccount extends BaseEntity {

    private Long userId;
    private Integer totalPoints;
    private Integer availablePoints;
    private Integer usedPoints;
    private Integer expiredPoints;
    private Integer level;
}
