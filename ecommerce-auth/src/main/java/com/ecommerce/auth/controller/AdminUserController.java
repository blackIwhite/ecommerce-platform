package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.AdminUserCreateRequest;
import com.ecommerce.auth.dto.AdminUserDTO;
import com.ecommerce.auth.dto.PasswordResetRequest;
import com.ecommerce.auth.dto.StatusUpdateRequest;
import com.ecommerce.auth.service.AdminService;
import com.ecommerce.common.core.annotation.AuditLog;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminService adminService;

    @GetMapping
    public Result<PageResult<AdminUserDTO>> listAdminUsers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<AdminUserDTO> pageResult = adminService.listAdminUsers(pageNum, pageSize);
        return Result.success(pageResult);
    }

    @AuditLog(module = "RBAC", operation = "Create Admin", description = "Create admin user")
    @PostMapping
    public Result<Void> createAdmin(@RequestBody @Valid AdminUserCreateRequest request) {
        adminService.createAdmin(request);
        return Result.success();
    }

    @AuditLog(module = "RBAC", operation = "Update Admin Status", description = "Update admin status")
    @PutMapping("/{id}/status")
    public Result<Void> updateAdminStatus(@PathVariable Long id, @RequestBody @Valid StatusUpdateRequest request) {
        adminService.updateAdminStatus(id, request.getStatus());
        return Result.success();
    }

    @AuditLog(module = "RBAC", operation = "Reset Password", description = "Reset admin password")
    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody @Valid PasswordResetRequest request) {
        adminService.resetPassword(id, request.getNewPassword());
        return Result.success();
    }

    @AuditLog(module = "RBAC", operation = "Delete Admin", description = "Delete admin user")
    @DeleteMapping("/{id}")
    public Result<Void> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return Result.success();
    }
}
