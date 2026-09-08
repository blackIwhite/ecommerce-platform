package com.ecommerce.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorLogDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String action;
    private String actionName;
    private String targetType;
    private Long targetId;
    private String extraData;
    private String createTime;
}
