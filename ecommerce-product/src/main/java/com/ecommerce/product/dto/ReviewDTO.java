package com.ecommerce.product.dto;

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
public class ReviewDTO implements Serializable {

    private Long id;
    private Long spuId;
    private Long skuId;
    private String skuName;
    private Long userId;
    private String nickname;
    private String avatar;
    private Integer rating;
    private String content;
    private String images;
    private LocalDateTime createTime;
}
