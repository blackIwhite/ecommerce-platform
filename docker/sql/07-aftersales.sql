-- Aftersales module tables

-- Aftersales order
CREATE TABLE IF NOT EXISTS t_aftersales_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    aftersales_no VARCHAR(32) NOT NULL COMMENT 'Aftersales order number',
    order_id BIGINT NOT NULL COMMENT 'Original order ID',
    user_id BIGINT NOT NULL COMMENT 'User ID',
    type TINYINT NOT NULL COMMENT 'Type: 1=return refund, 2=exchange, 3=refund only',
    status TINYINT NOT NULL DEFAULT 0 COMMENT 'Status: 0=pending, 1=approved, 2=returning, 3=received, 4=refunding, 5=refunded, 6=completed, 7=rejected, 8=closed',
    reason VARCHAR(500) NOT NULL COMMENT 'Return reason',
    description VARCHAR(1000) DEFAULT NULL COMMENT 'Detailed description',
    images VARCHAR(2000) DEFAULT NULL COMMENT 'Image URLs (JSON array)',
    refund_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'Refund amount',
    exchange_address VARCHAR(500) DEFAULT NULL COMMENT 'Exchange shipping address',
    return_tracking_no VARCHAR(100) DEFAULT NULL COMMENT 'Return tracking number',
    return_company VARCHAR(100) DEFAULT NULL COMMENT 'Return logistics company',
    handler VARCHAR(100) DEFAULT NULL COMMENT 'Handler name (admin)',
    handle_remark VARCHAR(500) DEFAULT NULL COMMENT 'Handle remark',
    handle_time DATETIME DEFAULT NULL COMMENT 'Handle time',
    receive_time DATETIME DEFAULT NULL COMMENT 'Goods received time',
    refund_time DATETIME DEFAULT NULL COMMENT 'Refund time',
    complete_time DATETIME DEFAULT NULL COMMENT 'Complete time',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE INDEX uk_no (aftersales_no),
    INDEX idx_order (order_id),
    INDEX idx_user (user_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Aftersales order';

-- Aftersales item (which products are being returned)
CREATE TABLE IF NOT EXISTS t_aftersales_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    aftersales_id BIGINT NOT NULL COMMENT 'Aftersales order ID',
    order_item_id BIGINT NOT NULL COMMENT 'Original order item ID',
    spu_id BIGINT NOT NULL COMMENT 'SPU ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    product_name VARCHAR(200) NOT NULL COMMENT 'Product name',
    sku_name VARCHAR(500) DEFAULT NULL COMMENT 'SKU attributes',
    image VARCHAR(500) DEFAULT NULL COMMENT 'Product image',
    price DECIMAL(10,2) NOT NULL COMMENT 'Unit price',
    quantity INT NOT NULL DEFAULT 1 COMMENT 'Return quantity',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_aftersales (aftersales_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Aftersales item';

-- Aftersales status log
CREATE TABLE IF NOT EXISTS t_aftersales_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    aftersales_id BIGINT NOT NULL COMMENT 'Aftersales order ID',
    from_status TINYINT NOT NULL COMMENT 'Previous status',
    to_status TINYINT NOT NULL COMMENT 'New status',
    operator VARCHAR(100) DEFAULT NULL COMMENT 'Operator',
    remark VARCHAR(500) DEFAULT NULL COMMENT 'Remark',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_aftersales (aftersales_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Aftersales status log';
