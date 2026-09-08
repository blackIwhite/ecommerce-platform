package com.ecommerce.order.dto;

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
public class PaymentDTO implements Serializable {

    private Long paymentId;
    private Long orderId;
    private String paymentNo;
    private BigDecimal amount;
    private String payMethod;
    private Integer status;
    private LocalDateTime payTime;
    private LocalDateTime createTime;
}
