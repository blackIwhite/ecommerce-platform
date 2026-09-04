package com.ecommerce.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderShipRequest implements Serializable {

    @NotBlank(message = "Logistics company is required")
    @Size(max = 50)
    private String logisticsCompany;

    @NotBlank(message = "Tracking number is required")
    @Size(max = 50)
    private String trackingNo;
}
