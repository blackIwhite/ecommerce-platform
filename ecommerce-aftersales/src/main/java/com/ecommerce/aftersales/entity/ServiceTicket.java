package com.ecommerce.aftersales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_service_ticket")
public class ServiceTicket extends BaseEntity {

    private String ticketNo;
    private Long userId;
    private Integer type;
    private String subject;
    private String content;
    private Integer priority;
    private Integer status;
    private String assignedTo;
    private Long orderId;
}
