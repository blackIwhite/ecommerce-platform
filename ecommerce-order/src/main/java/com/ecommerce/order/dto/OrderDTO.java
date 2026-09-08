package com.ecommerce.order.dto;

import com.ecommerce.common.core.annotation.SensitiveData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO implements Serializable {

    private Long orderId;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private Long couponId;
    private BigDecimal discountAmount;
    private Integer status;
    @SensitiveData(type = SensitiveData.SensitiveType.NAME)
    private String receiverName;
    @SensitiveData(type = SensitiveData.SensitiveType.PHONE)
    private String receiverPhone;
    @SensitiveData(type = SensitiveData.SensitiveType.ADDRESS)
    private String receiverAddress;
    private String remark;
    private LocalDateTime createTime;
    private String logisticsCompany;
    private String trackingNo;
    private List<OrderItemDTO> items;
    private List<StatusLogDTO> statusLogs;
}
