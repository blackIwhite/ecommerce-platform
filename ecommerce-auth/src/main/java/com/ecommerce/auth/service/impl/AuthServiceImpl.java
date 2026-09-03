package com.ecommerce.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.dto.LoginResponse;
import com.ecommerce.auth.dto.RefreshRequest;
import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.mapper.UserMapper;
import com.ecommerce.auth.service.AuthService;
import com.ecommerce.auth.service.JwtService;
import com.ecommerce.common.core.constant.CommonConstants;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.common.core.exception.BusinessException;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final StringRedisTemplate redisTemplate;

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    private static final String TOKEN_BLACKLIST_PREFIX = "auth:blacklist:";
    private static final String SMS_CODE_PREFIX = "auth:sms:";
    private static final String SMS_RATE_LIMIT_PREFIX = "auth:sms:rate:";
    private static final long SMS_CODE_TTL_SECONDS = 300;
    private static final long SMS_RATE_LIMIT_SECONDS = 60;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getPhone, request.getPhone())
                        .eq(User::getDeleted, CommonConstants.NOT_DELETED)
        );

        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "User not found");
        }

        if (user.getStatus() != null && user.getStatus() == CommonConstants.STATUS_DISABLED) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Account is disabled");
        }

        if ("SMS".equalsIgnoreCase(request.getLoginType())) {
            verifySmsCode(request.getPhone(), request.getSmsCode());
        } else {
            verifyPassword(user, request.getPassword());
        }

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getPhone());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        log.info("User logged in: userId={}, phone={}", user.getId(), user.getPhone());

        return LoginResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .build();
    }

    @Override
    public LoginResponse register(RegisterRequest request) {
        verifySmsCode(request.getPhone(), request.getSmsCode());

        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getPhone, request.getPhone())
                        .eq(User::getDeleted, CommonConstants.NOT_DELETED)
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Phone number already registered");
        }

        User user = new User();
        user.setPhone(request.getPhone());
        user.setPassword(PASSWORD_ENCODER.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : "User" + request.getPhone().substring(7));
        user.setStatus(CommonConstants.STATUS_ENABLED);
        user.setDeleted(CommonConstants.NOT_DELETED);

        userMapper.insert(user);

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getPhone());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        log.info("User registered: userId={}, phone={}", user.getId(), user.getPhone());

        return LoginResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .build();
    }

    @Override
    public void logout(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
        try {
            Claims claims = jwtService.validateToken(token);
            long remainingMs = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (remainingMs > 0) {
                redisTemplate.opsForValue().set(
                        TOKEN_BLACKLIST_PREFIX + token,
                        "1",
                        remainingMs,
                        TimeUnit.MILLISECONDS
                );
            }
            log.info("Token blacklisted for logout");
        } catch (Exception e) {
            log.warn("Failed to blacklist token during logout: {}", e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getUserInfo(String token) {
        if (token == null || token.isEmpty()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "Token is required");
        }

        Long userId = jwtService.getUserIdFromToken(token);
        User user = userMapper.selectById(userId);

        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "User not found");
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", user.getId());
        userInfo.put("phone", user.getPhone());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("status", user.getStatus());
        userInfo.put("createTime", user.getCreateTime());

        return userInfo;
    }

    @Override
    public void sendSmsCode(String phone) {
        if (phone == null || phone.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Phone number is required");
        }

        String rateLimitKey = SMS_RATE_LIMIT_PREFIX + phone;
        Boolean rateLimited = redisTemplate.opsForValue().setIfAbsent(rateLimitKey, "1", SMS_RATE_LIMIT_SECONDS, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(rateLimited)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Please wait before requesting another code");
        }

        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
        redisTemplate.opsForValue().set(SMS_CODE_PREFIX + phone, code, SMS_CODE_TTL_SECONDS, TimeUnit.SECONDS);

        log.info("SMS code sent to phone={}, code={}", phone, code);
    }

    @Override
    public LoginResponse refreshToken(RefreshRequest request) {
        if (request.getRefreshToken() == null || request.getRefreshToken().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Refresh token is required");
        }

        Claims claims = jwtService.validateToken(request.getRefreshToken());
        String tokenType = claims.get("type", String.class);
        if (!"refresh".equals(tokenType)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "Invalid refresh token");
        }

        Long userId = claims.get("userId", Long.class);
        User user = userMapper.selectById(userId);

        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "User not found");
        }

        if (user.getStatus() != null && user.getStatus() == CommonConstants.STATUS_DISABLED) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Account is disabled");
        }

        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getPhone());
        String newRefreshToken = jwtService.generateRefreshToken(user.getId());

        log.info("Token refreshed for userId={}", userId);

        return LoginResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .build();
    }

    private void verifySmsCode(String phone, String code) {
        if (code == null || code.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "SMS code is required");
        }
        String storedCode = redisTemplate.opsForValue().get(SMS_CODE_PREFIX + phone);
        if (storedCode == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "SMS code expired or not found");
        }
        if (!storedCode.equals(code)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Invalid SMS code");
        }
        redisTemplate.delete(SMS_CODE_PREFIX + phone);
    }

    private void verifyPassword(User user, String password) {
        if (password == null || password.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Password is required");
        }
        if (!PASSWORD_ENCODER.matches(password, user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "Invalid password");
        }
    }
}
