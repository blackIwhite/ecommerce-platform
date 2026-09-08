package com.ecommerce.api.marketing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointsEarnRequest implements Serializable {
    private Long userId;
    private Integer points;
    private String source;
    private Long referenceId;
    private String description;
}
