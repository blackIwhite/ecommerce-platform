package com.ecommerce.marketing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.mybatis.entity.BaseEntity;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_points_log")
public class PointsLog extends BaseEntity {

    private Long userId;
    private Integer type;
    private Integer points;
    private Integer balance;
    private String source;
    private Long referenceId;
    private String description;
}
