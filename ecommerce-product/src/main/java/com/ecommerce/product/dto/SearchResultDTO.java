package com.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String keyword;
    private long total;
    private List<SpuDTO> items;
    private List<String> suggestions;
}
