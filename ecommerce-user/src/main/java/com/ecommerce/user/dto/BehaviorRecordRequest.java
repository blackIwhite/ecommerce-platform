package com.ecommerce.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class BehaviorRecordRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "action is required")
    private String action;

    private String targetType;
    private Long targetId;
    private String extraData;
    private Long spuId;
    private Integer duration;
}
