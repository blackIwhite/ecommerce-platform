package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.AdminLoginRequest;
import com.ecommerce.auth.dto.AdminLoginResponse;
import com.ecommerce.auth.dto.AdminUserDTO;
import com.ecommerce.auth.dto.MenuDTO;
import com.ecommerce.auth.service.AdminService;
import com.ecommerce.auth.service.JwtService;
import com.ecommerce.common.core.annotation.AuditLog;
import com.ecommerce.common.core.result.Result;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminService adminService;
    private final JwtService jwtService;

    @AuditLog(module = "RBAC", operation = "Login", description = "Admin login")
    @PostMapping("/login")
    public Result<AdminLoginResponse> adminLogin(@RequestBody @Valid AdminLoginRequest request) {
        AdminLoginResponse response = adminService.adminLogin(request);
        return Result.success(response);
    }

    @GetMapping("/info")
    public Result<AdminUserDTO> getAdminInfo(HttpServletRequest request) {
        Long adminUserId = extractAdminUserId(request);
        AdminUserDTO info = adminService.getAdminInfo(adminUserId);
        return Result.success(info);
    }

    @GetMapping("/menus")
    public Result<List<MenuDTO>> getAdminMenus(HttpServletRequest request) {
        Long adminUserId = extractAdminUserId(request);
        List<MenuDTO> menus = adminService.listMenusByAdmin(adminUserId);
        return Result.success(menus);
    }

    private Long extractAdminUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Claims claims = jwtService.validateToken(token);
            return claims.get("userId", Long.class);
        }
        throw new com.ecommerce.common.core.exception.BusinessException(
                com.ecommerce.common.core.result.ResultCode.UNAUTHORIZED, "Token is required");
    }
}
