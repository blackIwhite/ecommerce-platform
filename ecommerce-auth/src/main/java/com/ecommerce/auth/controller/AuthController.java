package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.dto.LoginResponse;
import com.ecommerce.auth.dto.RefreshRequest;
import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.auth.dto.SmsRequest;
import com.ecommerce.auth.service.AuthService;
import com.ecommerce.common.core.annotation.AuditLog;
import com.ecommerce.common.core.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "Authentication APIs")
public class AuthController {

    private final AuthService authService;

    @AuditLog(module = "认证", operation = "登录", description = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    @AuditLog(module = "认证", operation = "注册", description = "用户注册")
    @PostMapping("/register")
    public Result<LoginResponse> register(@RequestBody @Valid RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return Result.success(response);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }
        return Result.success();
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        Map<String, Object> userInfo = authService.getUserInfo(token);
        return Result.success(userInfo);
    }

    @PostMapping("/sms/send")
    public Result<Void> sendSmsCode(@RequestBody @Valid SmsRequest request) {
        authService.sendSmsCode(request.getPhone());
        return Result.success();
    }

    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@RequestBody @Valid RefreshRequest request) {
        LoginResponse response = authService.refreshToken(request);
        return Result.success(response);
    }
}
