-- RBAC tables for admin management
USE ecommerce_user;

-- Admin user table
CREATE TABLE IF NOT EXISTS t_admin_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL COMMENT 'Login username',
    password VARCHAR(100) NOT NULL COMMENT 'BCrypt encrypted password',
    real_name VARCHAR(50) COMMENT 'Real name',
    phone VARCHAR(20) COMMENT 'Phone number',
    email VARCHAR(100) COMMENT 'Email',
    avatar VARCHAR(500) COMMENT 'Avatar URL',
    status TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 0=disabled, 1=enabled',
    last_login_time DATETIME COMMENT 'Last login time',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete: 0=not deleted, 1=deleted',
    UNIQUE INDEX uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Admin user table';

-- Role table
CREATE TABLE IF NOT EXISTS t_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL COMMENT 'Role display name',
    role_key VARCHAR(50) NOT NULL COMMENT 'Role unique key',
    description VARCHAR(200) COMMENT 'Role description',
    status TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 0=disabled, 1=enabled',
    sort INT NOT NULL DEFAULT 0 COMMENT 'Display sort order',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete: 0=not deleted, 1=deleted',
    UNIQUE INDEX uk_role_key (role_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Role table';

-- Permission table
CREATE TABLE IF NOT EXISTS t_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT 'Permission display name',
    code VARCHAR(100) NOT NULL COMMENT 'Permission code e.g. system:user:list',
    module VARCHAR(50) COMMENT 'Module this permission belongs to',
    description VARCHAR(200) COMMENT 'Permission description',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete: 0=not deleted, 1=deleted',
    UNIQUE INDEX uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Permission table';

-- Menu table
CREATE TABLE IF NOT EXISTS t_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT 'Parent menu ID, 0 for root',
    name VARCHAR(50) NOT NULL COMMENT 'Menu display name',
    path VARCHAR(200) COMMENT 'Route path',
    component VARCHAR(200) COMMENT 'Frontend component path',
    icon VARCHAR(100) COMMENT 'Menu icon',
    sort INT NOT NULL DEFAULT 0 COMMENT 'Display sort order',
    type TINYINT NOT NULL COMMENT 'Menu type: 1=directory, 2=menu, 3=button',
    permission VARCHAR(100) COMMENT 'Permission code',
    visible TINYINT NOT NULL DEFAULT 1 COMMENT 'Visible: 0=hidden, 1=visible',
    status TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 0=disabled, 1=enabled',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete: 0=not deleted, 1=deleted',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Menu table';

-- Admin user - role relation table
CREATE TABLE IF NOT EXISTS t_admin_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_user_id BIGINT NOT NULL COMMENT 'Admin user ID',
    role_id BIGINT NOT NULL COMMENT 'Role ID',
    UNIQUE KEY uk_user_role (admin_user_id, role_id),
    INDEX idx_admin_user_id (admin_user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Admin user role relation table';

-- Role - permission relation table
CREATE TABLE IF NOT EXISTS t_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL COMMENT 'Role ID',
    permission_id BIGINT NOT NULL COMMENT 'Permission ID',
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Role permission relation table';

-- Role - menu relation table
CREATE TABLE IF NOT EXISTS t_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL COMMENT 'Role ID',
    menu_id BIGINT NOT NULL COMMENT 'Menu ID',
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Role menu relation table';

-- ============================================================
-- Seed data
-- ============================================================

-- Super admin role
INSERT INTO t_role (id, role_name, role_key, description, status, sort) VALUES
(1, 'Super Admin', 'super_admin', 'Super administrator with all permissions', 1, 0);

-- Basic permissions
INSERT INTO t_permission (id, name, code, module, description) VALUES
(1, 'View Admin Users', 'system:user:list', 'system', 'View admin user list'),
(2, 'Create Admin User', 'system:user:create', 'system', 'Create new admin user'),
(3, 'Update Admin User', 'system:user:update', 'system', 'Update admin user'),
(4, 'Delete Admin User', 'system:user:delete', 'system', 'Delete admin user'),
(5, 'View Roles', 'system:role:list', 'system', 'View role list'),
(6, 'Create Role', 'system:role:create', 'system', 'Create new role'),
(7, 'Update Role', 'system:role:update', 'system', 'Update role'),
(8, 'Delete Role', 'system:role:delete', 'system', 'Delete role'),
(9, 'View Menus', 'system:menu:list', 'system', 'View menu list'),
(10, 'View Permissions', 'system:permission:list', 'system', 'View permission list'),
(11, 'View Products', 'product:spu:list', 'product', 'View product list'),
(12, 'Create Product', 'product:spu:create', 'product', 'Create new product'),
(13, 'Update Product', 'product:spu:update', 'product', 'Update product'),
(14, 'Delete Product', 'product:spu:delete', 'product', 'Delete product'),
(15, 'View Orders', 'order:list', 'order', 'View order list'),
(16, 'Update Order', 'order:update', 'order', 'Update order'),
(17, 'View Categories', 'product:category:list', 'product', 'View category list'),
(18, 'Manage Categories', 'product:category:manage', 'product', 'Create/update/delete categories'),
(19, 'View Brands', 'product:brand:list', 'product', 'View brand list'),
(20, 'Manage Brands', 'product:brand:manage', 'product', 'Create/update/delete brands'),
(21, 'View Users', 'user:list', 'user', 'View frontend user list'),
(22, 'View Inventory', 'inventory:list', 'inventory', 'View inventory list'),
(23, 'View Aftersales', 'aftersales:list', 'aftersales', 'View aftersales list'),
(24, 'Handle Aftersales', 'aftersales:handle', 'aftersales', 'Handle aftersales requests');

-- Menu tree
-- Level 1 - Directories
INSERT INTO t_menu (id, parent_id, name, path, component, icon, sort, type, visible, status) VALUES
(1, 0, 'System', '/system', NULL, 'setting', 1, 1, 1, 1),
(2, 0, 'Product', '/product', NULL, 'shopping', 2, 1, 1, 1),
(3, 0, 'Order', '/order', NULL, 'document', 3, 1, 1, 1),
(4, 0, 'User', '/user', NULL, 'user', 4, 1, 1, 1),
(5, 0, 'Marketing', '/marketing', NULL, 'gift', 5, 1, 1, 1),
(6, 0, 'Aftersales', '/aftersales', NULL, 'service', 6, 1, 1, 1);

-- Level 2 - Menus
-- System menus
INSERT INTO t_menu (id, parent_id, name, path, component, icon, sort, type, permission, visible, status) VALUES
(101, 1, 'Admin Users', '/system/admin', 'system/admin/index', 'peoples', 1, 2, 'system:user:list', 1, 1),
(102, 1, 'Roles', '/system/role', 'system/role/index', 'lock', 2, 2, 'system:role:list', 1, 1),
(103, 1, 'Menus', '/system/menu', 'system/menu/index', 'tree-table', 3, 2, 'system:menu:list', 1, 1);

-- Product menus
INSERT INTO t_menu (id, parent_id, name, path, component, icon, sort, type, permission, visible, status) VALUES
(201, 2, 'Product List', '/product/spu', 'product/spu/index', 'goods', 1, 2, 'product:spu:list', 1, 1),
(202, 2, 'Categories', '/product/category', 'product/category/index', 'tree', 2, 2, 'product:category:list', 1, 1),
(203, 2, 'Brands', '/product/brand', 'product/brand/index', 'component', 3, 2, 'product:brand:list', 1, 1);

-- Order menus
INSERT INTO t_menu (id, parent_id, name, path, component, icon, sort, type, permission, visible, status) VALUES
(301, 3, 'Order List', '/order/list', 'order/list/index', 'list', 1, 2, 'order:list', 1, 1);

-- User menus
INSERT INTO t_menu (id, parent_id, name, path, component, icon, sort, type, permission, visible, status) VALUES
(401, 4, 'User List', '/user/list', 'user/list/index', 'user', 1, 2, 'user:list', 1, 1);

-- Aftersales menus
INSERT INTO t_menu (id, parent_id, name, path, component, icon, sort, type, permission, visible, status) VALUES
(601, 6, 'Aftersales List', '/aftersales/list', 'aftersales/list/index', 'clipboard', 1, 2, 'aftersales:list', 1, 1);

-- Level 3 - Buttons (admin user management)
INSERT INTO t_menu (id, parent_id, name, path, component, icon, sort, type, permission, visible, status) VALUES
(1011, 101, 'Create Admin', '', NULL, '', 1, 3, 'system:user:create', 1, 1),
(1012, 101, 'Update Admin', '', NULL, '', 2, 3, 'system:user:update', 1, 1),
(1013, 101, 'Delete Admin', '', NULL, '', 3, 3, 'system:user:delete', 1, 1);

-- Level 3 - Buttons (role management)
INSERT INTO t_menu (id, parent_id, name, path, component, icon, sort, type, permission, visible, status) VALUES
(1021, 102, 'Create Role', '', NULL, '', 1, 3, 'system:role:create', 1, 1),
(1022, 102, 'Update Role', '', NULL, '', 2, 3, 'system:role:update', 1, 1),
(1023, 102, 'Delete Role', '', NULL, '', 3, 3, 'system:role:delete', 1, 1);

-- Level 3 - Buttons (product management)
INSERT INTO t_menu (id, parent_id, name, path, component, icon, sort, type, permission, visible, status) VALUES
(2011, 201, 'Create Product', '', NULL, '', 1, 3, 'product:spu:create', 1, 1),
(2012, 201, 'Update Product', '', NULL, '', 2, 3, 'product:spu:update', 1, 1),
(2013, 201, 'Delete Product', '', NULL, '', 3, 3, 'product:spu:delete', 1, 1);

-- Level 3 - Buttons (category management)
INSERT INTO t_menu (id, parent_id, name, path, component, icon, sort, type, permission, visible, status) VALUES
(2021, 202, 'Manage Categories', '', NULL, '', 1, 3, 'product:category:manage', 1, 1);

-- Level 3 - Buttons (brand management)
INSERT INTO t_menu (id, parent_id, name, path, component, icon, sort, type, permission, visible, status) VALUES
(2031, 203, 'Manage Brands', '', NULL, '', 1, 3, 'product:brand:manage', 1, 1);

-- Assign all permissions to super_admin role
INSERT INTO t_role_permission (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8),
(1, 9), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15), (1, 16),
(1, 17), (1, 18), (1, 19), (1, 20), (1, 21), (1, 22), (1, 23), (1, 24);

-- Assign all menus to super_admin role
INSERT INTO t_role_menu (role_id, menu_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6),
(1, 101), (1, 102), (1, 103),
(1, 201), (1, 202), (1, 203),
(1, 301),
(1, 401),
(1, 601),
(1, 1011), (1, 1012), (1, 1013),
(1, 1021), (1, 1022), (1, 1023),
(1, 2011), (1, 2012), (1, 2013),
(1, 2021),
(1, 2031);

-- Create default admin user: admin / admin123 (BCrypt hash)
INSERT INTO t_admin_user (id, username, password, real_name, phone, email, status) VALUES
(1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'System Admin', '13800000000', 'admin@ecommerce.com', 1);

-- Assign super_admin role to admin user
INSERT INTO t_admin_user_role (admin_user_id, role_id) VALUES (1, 1);
