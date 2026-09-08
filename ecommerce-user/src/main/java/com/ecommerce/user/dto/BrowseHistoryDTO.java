package com.ecommerce.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrowseHistoryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long spuId;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private String browseTime;
    private Integer duration;
}
