package com.ecommerce.auth.dto;

import lombok.Data;

/**
 * Register request DTO.
 */
@Data
public class RegisterRequest {

    private String phone;

    private String password;

    private String smsCode;

    private String nickname;
}
