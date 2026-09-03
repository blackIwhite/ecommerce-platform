-- Product database tables
USE ecommerce_product;

-- Brand table
CREATE TABLE IF NOT EXISTS t_brand (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT 'Brand name',
    logo VARCHAR(500) COMMENT 'Brand logo URL',
    description VARCHAR(500) COMMENT 'Brand description',
    status TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 0=disabled, 1=enabled',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete: 0=not deleted, 1=deleted'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Brand table';

-- Category table
CREATE TABLE IF NOT EXISTS t_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT 'Category name',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT 'Parent category ID, 0 for root',
    level TINYINT NOT NULL DEFAULT 1 COMMENT 'Category level: 1, 2, 3',
    sort INT NOT NULL DEFAULT 0 COMMENT 'Sort order',
    icon VARCHAR(500) COMMENT 'Category icon URL',
    status TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 0=disabled, 1=enabled',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete: 0=not deleted, 1=deleted'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Category table';

-- SPU table
CREATE TABLE IF NOT EXISTS t_spu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT 'SPU name',
    category_id BIGINT NOT NULL COMMENT 'Category ID',
    brand_id BIGINT COMMENT 'Brand ID',
    description TEXT COMMENT 'SPU description',
    images TEXT COMMENT 'Image URLs, JSON array',
    status TINYINT NOT NULL DEFAULT 0 COMMENT 'Status: 0=draft, 1=on-shelf, 2=off-shelf',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete: 0=not deleted, 1=deleted',
    INDEX idx_category_id (category_id),
    INDEX idx_brand_id (brand_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SPU table';

-- SKU table
CREATE TABLE IF NOT EXISTS t_sku (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    spu_id BIGINT NOT NULL COMMENT 'SPU ID',
    sku_name VARCHAR(200) NOT NULL COMMENT 'SKU name',
    price DECIMAL(10, 2) NOT NULL COMMENT 'Price',
    stock INT NOT NULL DEFAULT 0 COMMENT 'Stock quantity',
    image VARCHAR(500) COMMENT 'SKU image URL',
    specs TEXT COMMENT 'Specification details, JSON string',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete: 0=not deleted, 1=deleted',
    INDEX idx_spu_id (spu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SKU table';
