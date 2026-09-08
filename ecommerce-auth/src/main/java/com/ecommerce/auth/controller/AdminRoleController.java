package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.RoleDTO;
import com.ecommerce.auth.service.AdminService;
import com.ecommerce.common.core.annotation.AuditLog;
import com.ecommerce.common.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth/admin/roles")
@RequiredArgsConstructor
public class AdminRoleController {

    private final AdminService adminService;

    @GetMapping
    public Result<List<RoleDTO>> listRoles() {
        List<RoleDTO> roles = adminService.listRoles();
        return Result.success(roles);
    }

    @AuditLog(module = "RBAC", operation = "Create Role", description = "Create role")
    @PostMapping
    public Result<Void> createRole(@RequestBody @Valid RoleDTO dto) {
        adminService.createRole(dto);
        return Result.success();
    }

    @AuditLog(module = "RBAC", operation = "Update Role", description = "Update role")
    @PutMapping("/{id}")
    public Result<Void> updateRole(@PathVariable Long id, @RequestBody @Valid RoleDTO dto) {
        adminService.updateRole(id, dto);
        return Result.success();
    }

    @AuditLog(module = "RBAC", operation = "Delete Role", description = "Delete role")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        adminService.deleteRole(id);
        return Result.success();
    }
}
