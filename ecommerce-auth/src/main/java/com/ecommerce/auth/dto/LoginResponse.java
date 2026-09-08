package com.ecommerce.auth.dto;

import com.ecommerce.common.core.annotation.SensitiveData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;

    private String refreshToken;

    private Long userId;

    @SensitiveData(type = SensitiveData.SensitiveType.PHONE)
    private String phone;

    private String nickname;
}
