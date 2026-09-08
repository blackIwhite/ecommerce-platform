package com.ecommerce.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFavoriteDTO implements Serializable {

    private Long spuId;
    private String spuName;
    private String image;
    private BigDecimal minPrice;
    private LocalDateTime createTime;
}
