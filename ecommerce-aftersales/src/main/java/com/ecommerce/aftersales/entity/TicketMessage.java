package com.ecommerce.aftersales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_ticket_message")
public class TicketMessage extends BaseEntity {

    private Long ticketId;
    private Integer senderType;
    private Long senderId;
    private String senderName;
    private String content;
}
