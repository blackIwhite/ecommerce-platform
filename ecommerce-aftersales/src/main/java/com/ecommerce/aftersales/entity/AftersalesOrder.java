package com.ecommerce.aftersales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_aftersales_order")
public class AftersalesOrder extends BaseEntity {

    private String aftersalesNo;
    private Long orderId;
    private Long userId;
    private Integer type;
    private Integer status;
    private String reason;
    private String description;
    private String images;
    private BigDecimal refundAmount;
    private String exchangeAddress;
    private String returnTrackingNo;
    private String returnCompany;
    private String exchangeTrackingNo;
    private String exchangeCompany;
    private String handler;
    private String handleRemark;
    private LocalDateTime handleTime;
    private LocalDateTime receiveTime;
    private LocalDateTime refundTime;
    private LocalDateTime completeTime;
}
