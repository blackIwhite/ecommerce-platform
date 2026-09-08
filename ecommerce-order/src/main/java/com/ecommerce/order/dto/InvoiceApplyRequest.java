package com.ecommerce.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InvoiceApplyRequest {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "发票类型不能为空")
    private Integer type;

    @NotBlank(message = "发票抬头不能为空")
    private String title;

    private String taxNo;

    private String email;

    private String phone;

    private String remark;
}
