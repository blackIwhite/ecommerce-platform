package com.ecommerce.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandCreateRequest implements Serializable {

    private Long id;

    @NotBlank
    private String name;

    private String logo;
    private String description;
}
