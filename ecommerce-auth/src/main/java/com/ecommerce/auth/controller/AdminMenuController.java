package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.MenuDTO;
import com.ecommerce.auth.dto.PermissionDTO;
import com.ecommerce.auth.service.AdminService;
import com.ecommerce.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth/admin/menus")
@RequiredArgsConstructor
public class AdminMenuController {

    private final AdminService adminService;

    @GetMapping
    public Result<List<MenuDTO>> listMenus() {
        List<MenuDTO> menus = adminService.listMenus();
        return Result.success(menus);
    }

    @GetMapping("/permissions")
    public Result<List<PermissionDTO>> listPermissions() {
        List<PermissionDTO> permissions = adminService.listPermissions();
        return Result.success(permissions);
    }
}
