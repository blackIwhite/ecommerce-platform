package com.ecommerce.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginResponse {

    private String accessToken;

    private String refreshToken;

    private Long id;

    private String username;

    private String realName;

    private String avatar;

    private List<String> roles;

    private List<String> permissions;
}
