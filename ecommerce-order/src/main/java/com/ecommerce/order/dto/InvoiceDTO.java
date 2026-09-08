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
public class InvoiceDTO implements Serializable {

    private Long id;
    private Long orderId;
    private String orderNo;
    private Long userId;
    /** 1=个人, 2=企业 */
    private Integer type;
    private String title;
    private String taxNo;
    private BigDecimal amount;
    private String email;
    private String phone;
    /** 0=待开票, 1=已开票, 2=已拒绝 */
    private Integer status;
    private String statusName;
    private String invoiceNo;
    private String invoiceUrl;
    private String remark;
    private String rejectReason;
    private LocalDateTime applyTime;
    private LocalDateTime issueTime;
    private LocalDateTime createTime;
}
