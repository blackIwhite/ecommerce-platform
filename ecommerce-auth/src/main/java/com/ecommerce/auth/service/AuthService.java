package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.dto.LoginResponse;
import com.ecommerce.auth.dto.RefreshRequest;
import com.ecommerce.auth.dto.RegisterRequest;

import java.util.Map;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse register(RegisterRequest request);

    void logout(String token);

    Map<String, Object> getUserInfo(String token);

    void sendSmsCode(String phone);

    LoginResponse refreshToken(RefreshRequest request);
}
