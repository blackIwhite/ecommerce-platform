package com.ecommerce.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordResetRequest {

    @NotBlank(message = "New password cannot be empty")
    @Size(min = 6, max = 20, message = "Password length must be 6-20 characters")
    private String newPassword;
}
