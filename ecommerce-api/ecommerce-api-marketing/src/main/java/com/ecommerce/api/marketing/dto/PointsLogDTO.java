package com.ecommerce.api.marketing.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PointsLogDTO implements Serializable {
    private Long id;
    private Long userId;
    private Integer type;
    private String typeName;
    private Integer points;
    private Integer balance;
    private String source;
    private Long referenceId;
    private String description;
    private String createTime;
}
