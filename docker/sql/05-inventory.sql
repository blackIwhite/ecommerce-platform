-- Inventory database tables
USE ecommerce_inventory;

-- Inventory table
CREATE TABLE IF NOT EXISTS t_inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    available_stock INT NOT NULL DEFAULT 0 COMMENT 'Available stock',
    locked_stock INT NOT NULL DEFAULT 0 COMMENT 'Locked stock (reserved for orders)',
    version INT NOT NULL DEFAULT 0 COMMENT 'Optimistic lock version',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete: 0=not deleted, 1=deleted',
    UNIQUE INDEX uk_sku_id (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Inventory table';

-- Inventory log table
CREATE TABLE IF NOT EXISTS t_inventory_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    order_id BIGINT COMMENT 'Related order ID',
    type VARCHAR(20) NOT NULL COMMENT 'Operation type: LOCK, UNLOCK, DEDUCT',
    quantity INT NOT NULL COMMENT 'Quantity changed',
    before_stock INT NOT NULL COMMENT 'Stock before operation',
    after_stock INT NOT NULL COMMENT 'Stock after operation',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete: 0=not deleted, 1=deleted',
    INDEX idx_sku_id (sku_id),
    INDEX idx_order_id (order_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Inventory operation log';
