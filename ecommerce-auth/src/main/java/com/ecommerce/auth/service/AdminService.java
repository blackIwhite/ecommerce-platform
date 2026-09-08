package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.*;
import com.ecommerce.common.core.page.PageResult;

import java.util.List;

public interface AdminService {

    AdminLoginResponse adminLogin(AdminLoginRequest request);

    AdminUserDTO getAdminInfo(Long adminUserId);

    PageResult<AdminUserDTO> listAdminUsers(int pageNum, int pageSize);

    void createAdmin(AdminUserCreateRequest request);

    void updateAdminStatus(Long id, Integer status);

    void resetPassword(Long id, String newPassword);

    void deleteAdmin(Long id);

    List<RoleDTO> listRoles();

    void createRole(RoleDTO dto);

    void updateRole(Long id, RoleDTO dto);

    void deleteRole(Long id);

    List<MenuDTO> listMenus();

    List<MenuDTO> listMenusByAdmin(Long adminUserId);

    List<PermissionDTO> listPermissions();
}
