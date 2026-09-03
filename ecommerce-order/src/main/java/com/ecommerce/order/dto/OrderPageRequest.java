package com.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPageRequest implements Serializable {

    private Long userId;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
