package com.ecommerce.user.dto;

import com.ecommerce.common.core.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserPageRequest extends PageQuery {

    private String keyword;
    private Integer status;
}
