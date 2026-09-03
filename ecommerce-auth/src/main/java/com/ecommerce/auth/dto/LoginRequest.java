package com.ecommerce.auth.dto;

import lombok.Data;

/**
 * Login request DTO.
 */
@Data
public class LoginRequest {

    private String phone;

    private String password;

    private String smsCode;

    /**
     * Login type: PASSWORD or SMS.
     */
    private String loginType;
}
