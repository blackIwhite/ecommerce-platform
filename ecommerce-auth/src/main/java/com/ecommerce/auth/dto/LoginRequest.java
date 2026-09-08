package com.ecommerce.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Size(min = 6, max = 20, message = "密码长度为6-20位")
    private String password;

    @Size(min = 4, max = 6, message = "验证码长度为4-6位")
    private String smsCode;

    @Pattern(regexp = "^(PASSWORD|SMS)$", message = "登录类型不正确")
    private String loginType;
}
