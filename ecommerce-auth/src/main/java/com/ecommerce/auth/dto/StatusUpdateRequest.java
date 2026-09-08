package com.ecommerce.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateRequest {

    @NotNull(message = "Status cannot be null")
    private Integer status;
}
