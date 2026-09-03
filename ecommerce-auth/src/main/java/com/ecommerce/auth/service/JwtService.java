package com.ecommerce.auth.service;

import io.jsonwebtoken.Claims;

public interface JwtService {

    String generateAccessToken(Long userId, String phone);

    String generateRefreshToken(Long userId);

    Claims validateToken(String token);

    Long getUserIdFromToken(String token);
}
