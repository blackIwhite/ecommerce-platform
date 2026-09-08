package com.ecommerce.marketing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.mybatis.entity.BaseEntity;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_promotion")
public class Promotion extends BaseEntity {
    private String name;
    private Integer type;
    private String ruleJson;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private String description;
}
