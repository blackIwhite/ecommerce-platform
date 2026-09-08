package com.ecommerce.aftersales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_aftersales_log")
public class AftersalesLog extends BaseEntity {

    private Long aftersalesId;
    private Integer fromStatus;
    private Integer toStatus;
    private String operator;
    private String remark;
}
