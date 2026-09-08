package com.ecommerce.api.marketing.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PointsAccountDTO implements Serializable {
    private Long userId;
    private Integer totalPoints;
    private Integer availablePoints;
    private Integer usedPoints;
    private Integer expiredPoints;
    private Integer level;
    private String levelName;
}
