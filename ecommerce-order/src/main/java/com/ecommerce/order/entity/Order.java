package com.ecommerce.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.mybatis.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_order")
public class Order extends BaseEntity {

    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private Long couponId;
    private BigDecimal discountAmount;
    private Long promotionId;
    /** 0=pending_pay, 1=paid, 2=pending_ship, 3=shipped, 4=completed, 5=cancelled */
    private Integer status;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
    private String logisticsCompany;
    private String trackingNo;
}
