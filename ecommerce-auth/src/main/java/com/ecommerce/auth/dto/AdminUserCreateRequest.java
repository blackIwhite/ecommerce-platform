package com.ecommerce.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class AdminUserCreateRequest {

    @NotBlank(message = "Username cannot be empty")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    private String password;

    private String realName;

    private String phone;

    private String email;

    private List<Long> roleIds;
}
