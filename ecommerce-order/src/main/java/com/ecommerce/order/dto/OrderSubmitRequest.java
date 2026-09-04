package com.ecommerce.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class OrderSubmitRequest implements Serializable {

    private Long userId;

    @NotNull(message = "Address id is required")
    private Long addressId;

    @NotEmpty(message = "Order items are required")
    @Valid
    private List<OrderItemRequest> items;

    @Size(max = 500)
    private String remark;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest implements Serializable {
        @NotNull(message = "SKU id is required")
        private Long skuId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 999, message = "Quantity must not exceed 999")
        private Integer quantity;
    }
}
