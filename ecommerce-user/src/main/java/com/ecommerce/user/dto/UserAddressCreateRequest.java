package com.ecommerce.user.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class UserAddressCreateRequest implements Serializable {

    private Long userId;

    @NotBlank(message = "Receiver name is required")
    @Size(max = 50)
    private String receiverName;

    @NotBlank(message = "Receiver phone is required")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "Invalid phone number")
    private String receiverPhone;

    @NotBlank(message = "Province is required")
    private String province;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "District is required")
    private String district;

    @NotBlank(message = "Detail address is required")
    @Size(max = 500)
    private String detailAddress;

    @Min(0) @Max(1)
    private Integer isDefault;
}
