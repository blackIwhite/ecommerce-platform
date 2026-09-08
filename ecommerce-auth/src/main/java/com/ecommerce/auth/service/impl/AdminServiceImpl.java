package com.ecommerce.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.auth.config.AuthProperties;
import com.ecommerce.auth.dto.*;
import com.ecommerce.auth.entity.*;
import com.ecommerce.auth.mapper.*;
import com.ecommerce.auth.service.AdminService;
import com.ecommerce.common.core.constant.CommonConstants;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.ResultCode;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminUserMapper adminUserMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final MenuMapper menuMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final AuthProperties authProperties;

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Override
    public AdminLoginResponse adminLogin(AdminLoginRequest request) {
        AdminUser adminUser = adminUserMapper.selectOne(
                new LambdaQueryWrapper<AdminUser>()
                        .eq(AdminUser::getUsername, request.getUsername())
        );

        if (adminUser == null) {
            throw new BusinessException(ResultCode.ADMIN_USER_NOT_FOUND);
        }

        if (adminUser.getStatus() != null && adminUser.getStatus() == CommonConstants.STATUS_DISABLED) {
            throw new BusinessException(ResultCode.ADMIN_USER_DISABLED);
        }

        if (!PASSWORD_ENCODER.matches(request.getPassword(), adminUser.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "Invalid password");
        }

        // Update last login time
        adminUser.setLastLoginTime(LocalDateTime.now());
        adminUserMapper.updateById(adminUser);

        // Load roles and permissions
        List<String> roleKeys = getRoleKeysByAdminUserId(adminUser.getId());
        List<String> permissionCodes = getPermissionCodesByAdminUserId(adminUser.getId());

        // Generate tokens with admin claims
        String accessToken = generateAdminAccessToken(adminUser, roleKeys);
        String refreshToken = generateAdminRefreshToken(adminUser);

        log.info("Admin logged in: adminUserId={}, username={}", adminUser.getId(), adminUser.getUsername());

        return AdminLoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .id(adminUser.getId())
                .username(adminUser.getUsername())
                .realName(adminUser.getRealName())
                .avatar(adminUser.getAvatar())
                .roles(roleKeys)
                .permissions(permissionCodes)
                .build();
    }

    @Override
    public AdminUserDTO getAdminInfo(Long adminUserId) {
        AdminUser adminUser = adminUserMapper.selectById(adminUserId);
        if (adminUser == null) {
            throw new BusinessException(ResultCode.ADMIN_USER_NOT_FOUND);
        }

        List<String> roleKeys = getRoleKeysByAdminUserId(adminUserId);

        return AdminUserDTO.builder()
                .id(adminUser.getId())
                .username(adminUser.getUsername())
                .realName(adminUser.getRealName())
                .phone(adminUser.getPhone())
                .email(adminUser.getEmail())
                .avatar(adminUser.getAvatar())
                .status(adminUser.getStatus())
                .roles(roleKeys)
                .createTime(adminUser.getCreateTime())
                .build();
    }

    @Override
    public PageResult<AdminUserDTO> listAdminUsers(int pageNum, int pageSize) {
        Page<AdminUser> page = new Page<>(pageNum, pageSize);
        Page<AdminUser> result = adminUserMapper.selectPage(page,
                new LambdaQueryWrapper<AdminUser>()
                        .orderByDesc(AdminUser::getCreateTime)
        );

        List<AdminUserDTO> dtoList = result.getRecords().stream()
                .map(user -> {
                    List<String> roleKeys = getRoleKeysByAdminUserId(user.getId());
                    return AdminUserDTO.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .realName(user.getRealName())
                            .phone(user.getPhone())
                            .email(user.getEmail())
                            .avatar(user.getAvatar())
                            .status(user.getStatus())
                            .roles(roleKeys)
                            .createTime(user.getCreateTime())
                            .build();
                })
                .collect(Collectors.toList());

        return PageResult.of(dtoList, result.getTotal(), pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createAdmin(AdminUserCreateRequest request) {
        // Check username uniqueness
        Long count = adminUserMapper.selectCount(
                new LambdaQueryWrapper<AdminUser>()
                        .eq(AdminUser::getUsername, request.getUsername())
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.ADMIN_USERNAME_EXISTS);
        }

        AdminUser adminUser = new AdminUser();
        adminUser.setUsername(request.getUsername());
        adminUser.setPassword(PASSWORD_ENCODER.encode(request.getPassword()));
        adminUser.setRealName(request.getRealName());
        adminUser.setPhone(request.getPhone());
        adminUser.setEmail(request.getEmail());
        adminUser.setStatus(CommonConstants.STATUS_ENABLED);
        adminUser.setDeleted(CommonConstants.NOT_DELETED);

        adminUserMapper.insert(adminUser);

        // Assign roles
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            for (Long roleId : request.getRoleIds()) {
                AdminUserRole userRole = new AdminUserRole();
                userRole.setAdminUserId(adminUser.getId());
                userRole.setRoleId(roleId);
                adminUserRoleMapper.insert(userRole);
            }
        }

        log.info("Admin user created: adminUserId={}, username={}", adminUser.getId(), adminUser.getUsername());
    }

    @Override
    public void updateAdminStatus(Long id, Integer status) {
        AdminUser adminUser = adminUserMapper.selectById(id);
        if (adminUser == null) {
            throw new BusinessException(ResultCode.ADMIN_USER_NOT_FOUND);
        }

        adminUser.setStatus(status);
        adminUserMapper.updateById(adminUser);

        log.info("Admin status updated: adminUserId={}, status={}", id, status);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        AdminUser adminUser = adminUserMapper.selectById(id);
        if (adminUser == null) {
            throw new BusinessException(ResultCode.ADMIN_USER_NOT_FOUND);
        }

        adminUser.setPassword(PASSWORD_ENCODER.encode(newPassword));
        adminUserMapper.updateById(adminUser);

        log.info("Admin password reset: adminUserId={}", id);
    }

    @Override
    public void deleteAdmin(Long id) {
        AdminUser adminUser = adminUserMapper.selectById(id);
        if (adminUser == null) {
            throw new BusinessException(ResultCode.ADMIN_USER_NOT_FOUND);
        }

        adminUserMapper.deleteById(id);

        // Clean up role relations
        adminUserRoleMapper.delete(
                new LambdaQueryWrapper<AdminUserRole>()
                        .eq(AdminUserRole::getAdminUserId, id)
        );

        log.info("Admin user deleted: adminUserId={}", id);
    }

    @Override
    public List<RoleDTO> listRoles() {
        List<Role> roles = roleMapper.selectList(
                new LambdaQueryWrapper<Role>().orderByAsc(Role::getSort)
        );

        return roles.stream().map(role -> {
            List<Long> permIds = getPermissionIdsByRoleId(role.getId());
            List<Long> menuIds = getMenuIdsByRoleId(role.getId());
            return RoleDTO.builder()
                    .id(role.getId())
                    .roleName(role.getRoleName())
                    .roleKey(role.getRoleKey())
                    .description(role.getDescription())
                    .status(role.getStatus())
                    .sort(role.getSort())
                    .permissionIds(permIds)
                    .menuIds(menuIds)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRole(RoleDTO dto) {
        // Check role key uniqueness
        Long count = roleMapper.selectCount(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getRoleKey, dto.getRoleKey())
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.ROLE_KEY_EXISTS);
        }

        Role role = new Role();
        role.setRoleName(dto.getRoleName());
        role.setRoleKey(dto.getRoleKey());
        role.setDescription(dto.getDescription());
        role.setStatus(dto.getStatus() != null ? dto.getStatus() : CommonConstants.STATUS_ENABLED);
        role.setSort(dto.getSort() != null ? dto.getSort() : 0);
        role.setDeleted(CommonConstants.NOT_DELETED);

        roleMapper.insert(role);

        // Assign permissions
        saveRolePermissions(role.getId(), dto.getPermissionIds());

        // Assign menus
        saveRoleMenus(role.getId(), dto.getMenuIds());

        log.info("Role created: roleId={}, roleKey={}", role.getId(), role.getRoleKey());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Long id, RoleDTO dto) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.ROLE_NOT_FOUND);
        }

        // Check role key uniqueness (excluding self)
        if (dto.getRoleKey() != null && !dto.getRoleKey().equals(role.getRoleKey())) {
            Long count = roleMapper.selectCount(
                    new LambdaQueryWrapper<Role>()
                            .eq(Role::getRoleKey, dto.getRoleKey())
            );
            if (count > 0) {
                throw new BusinessException(ResultCode.ROLE_KEY_EXISTS);
            }
        }

        if (dto.getRoleName() != null) {
            role.setRoleName(dto.getRoleName());
        }
        if (dto.getRoleKey() != null) {
            role.setRoleKey(dto.getRoleKey());
        }
        if (dto.getDescription() != null) {
            role.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            role.setStatus(dto.getStatus());
        }
        if (dto.getSort() != null) {
            role.setSort(dto.getSort());
        }

        roleMapper.updateById(role);

        // Update permissions
        if (dto.getPermissionIds() != null) {
            rolePermissionMapper.delete(
                    new LambdaQueryWrapper<RolePermission>()
                            .eq(RolePermission::getRoleId, id)
            );
            saveRolePermissions(id, dto.getPermissionIds());
        }

        // Update menus
        if (dto.getMenuIds() != null) {
            roleMenuMapper.delete(
                    new LambdaQueryWrapper<RoleMenu>()
                            .eq(RoleMenu::getRoleId, id)
            );
            saveRoleMenus(id, dto.getMenuIds());
        }

        log.info("Role updated: roleId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.ROLE_NOT_FOUND);
        }

        roleMapper.deleteById(id);

        // Clean up relations
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleId, id)
        );
        roleMenuMapper.delete(
                new LambdaQueryWrapper<RoleMenu>()
                        .eq(RoleMenu::getRoleId, id)
        );

        log.info("Role deleted: roleId={}", id);
    }

    @Override
    public List<MenuDTO> listMenus() {
        List<Menu> menus = menuMapper.selectList(
                new LambdaQueryWrapper<Menu>()
                        .orderByAsc(Menu::getSort)
        );

        return buildMenuTree(menus, 0L);
    }

    @Override
    public List<MenuDTO> listMenusByAdmin(Long adminUserId) {
        // Check if user has super_admin role
        List<String> roleKeys = getRoleKeysByAdminUserId(adminUserId);
        if (roleKeys.contains("super_admin")) {
            return listMenus();
        }

        // Get menu IDs assigned to user's roles
        List<Long> roleIds = getRoleIdsByAdminUserId(adminUserId);
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<RoleMenu> roleMenus = roleMenuMapper.selectList(
                new LambdaQueryWrapper<RoleMenu>()
                        .in(RoleMenu::getRoleId, roleIds)
        );

        Set<Long> menuIds = roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .collect(Collectors.toSet());

        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Menu> menus = menuMapper.selectList(
                new LambdaQueryWrapper<Menu>()
                        .in(Menu::getId, menuIds)
                        .eq(Menu::getStatus, CommonConstants.STATUS_ENABLED)
                        .orderByAsc(Menu::getSort)
        );

        return buildMenuTree(menus, 0L);
    }

    @Override
    public List<PermissionDTO> listPermissions() {
        List<Permission> permissions = permissionMapper.selectList(null);
        return permissions.stream()
                .map(p -> PermissionDTO.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .code(p.getCode())
                        .module(p.getModule())
                        .description(p.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    // ========== Private helper methods ==========

    private String generateAdminAccessToken(AdminUser adminUser, List<String> roleKeys) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + authProperties.getAccessExpiration());

        SecretKey key = Keys.hmacShaKeyFor(authProperties.getSecret().getBytes(StandardCharsets.UTF_8));

        String roleKey = roleKeys.isEmpty() ? "user" : roleKeys.get(0);

        return Jwts.builder()
                .subject(String.valueOf(adminUser.getId()))
                .claim("userId", adminUser.getId())
                .claim("username", adminUser.getUsername())
                .claim("isAdmin", true)
                .claim("roleKey", roleKey)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    private String generateAdminRefreshToken(AdminUser adminUser) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + authProperties.getRefreshExpiration());

        SecretKey key = Keys.hmacShaKeyFor(authProperties.getSecret().getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(String.valueOf(adminUser.getId()))
                .claim("userId", adminUser.getId())
                .claim("isAdmin", true)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    private List<String> getRoleKeysByAdminUserId(Long adminUserId) {
        List<AdminUserRole> userRoles = adminUserRoleMapper.selectList(
                new LambdaQueryWrapper<AdminUserRole>()
                        .eq(AdminUserRole::getAdminUserId, adminUserId)
        );
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> roleIds = userRoles.stream()
                .map(AdminUserRole::getRoleId)
                .collect(Collectors.toList());

        List<Role> roles = roleMapper.selectList(
                new LambdaQueryWrapper<Role>()
                        .in(Role::getId, roleIds)
                        .eq(Role::getStatus, CommonConstants.STATUS_ENABLED)
        );

        return roles.stream()
                .map(Role::getRoleKey)
                .collect(Collectors.toList());
    }

    private List<Long> getRoleIdsByAdminUserId(Long adminUserId) {
        List<AdminUserRole> userRoles = adminUserRoleMapper.selectList(
                new LambdaQueryWrapper<AdminUserRole>()
                        .eq(AdminUserRole::getAdminUserId, adminUserId)
        );
        return userRoles.stream()
                .map(AdminUserRole::getRoleId)
                .collect(Collectors.toList());
    }

    private List<String> getPermissionCodesByAdminUserId(Long adminUserId) {
        List<Long> roleIds = getRoleIdsByAdminUserId(adminUserId);
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Check if super_admin
        List<Role> roles = roleMapper.selectList(
                new LambdaQueryWrapper<Role>()
                        .in(Role::getId, roleIds)
                        .eq(Role::getStatus, CommonConstants.STATUS_ENABLED)
        );
        boolean isSuperAdmin = roles.stream()
                .anyMatch(r -> "super_admin".equals(r.getRoleKey()));

        if (isSuperAdmin) {
            // Super admin gets all permissions
            List<Permission> allPermissions = permissionMapper.selectList(null);
            return allPermissions.stream()
                    .map(Permission::getCode)
                    .collect(Collectors.toList());
        }

        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>()
                        .in(RolePermission::getRoleId, roleIds)
        );

        Set<Long> permIds = rolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toSet());

        if (permIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Permission> permissions = permissionMapper.selectList(
                new LambdaQueryWrapper<Permission>()
                        .in(Permission::getId, permIds)
        );

        return permissions.stream()
                .map(Permission::getCode)
                .collect(Collectors.toList());
    }

    private List<Long> getPermissionIdsByRoleId(Long roleId) {
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleId, roleId)
        );
        return rolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());
    }

    private List<Long> getMenuIdsByRoleId(Long roleId) {
        List<RoleMenu> roleMenus = roleMenuMapper.selectList(
                new LambdaQueryWrapper<RoleMenu>()
                        .eq(RoleMenu::getRoleId, roleId)
        );
        return roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .collect(Collectors.toList());
    }

    private void saveRolePermissions(Long roleId, List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }
        for (Long permId : permissionIds) {
            RolePermission rp = new RolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permId);
            rolePermissionMapper.insert(rp);
        }
    }

    private void saveRoleMenus(Long roleId, List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        for (Long menuId : menuIds) {
            RoleMenu rm = new RoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            roleMenuMapper.insert(rm);
        }
    }

    private List<MenuDTO> buildMenuTree(List<Menu> menus, Long parentId) {
        return menus.stream()
                .filter(m -> parentId.equals(m.getParentId()))
                .map(m -> {
                    MenuDTO dto = MenuDTO.builder()
                            .id(m.getId())
                            .parentId(m.getParentId())
                            .name(m.getName())
                            .path(m.getPath())
                            .component(m.getComponent())
                            .icon(m.getIcon())
                            .sort(m.getSort())
                            .type(m.getType())
                            .permission(m.getPermission())
                            .visible(m.getVisible())
                            .status(m.getStatus())
                            .children(buildMenuTree(menus, m.getId()))
                            .build();
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
