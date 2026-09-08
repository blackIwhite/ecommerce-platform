package com.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusLogDTO implements Serializable {

    private Integer fromStatus;
    private Integer toStatus;
    private String operator;
    private String remark;
    private LocalDateTime createTime;
}
