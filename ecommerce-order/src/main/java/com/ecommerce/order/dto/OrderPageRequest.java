package com.ecommerce.order.dto;

import com.ecommerce.common.core.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderPageRequest extends PageQuery {

    private Long userId;
    private Integer status;
    private String orderNo;
}
