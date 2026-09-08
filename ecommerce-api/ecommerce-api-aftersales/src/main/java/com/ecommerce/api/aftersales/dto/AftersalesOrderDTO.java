package com.ecommerce.api.aftersales.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AftersalesOrderDTO implements Serializable {

    private Long id;
    private String aftersalesNo;
    private Long orderId;
    private Long userId;
    private Integer type;
    private Integer status;
    private String reason;
    private BigDecimal refundAmount;
    private LocalDateTime createTime;
}
