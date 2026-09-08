-- Order database tables
USE ecommerce_order;

-- Order table
CREATE TABLE IF NOT EXISTS t_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL COMMENT 'Order number',
    user_id BIGINT NOT NULL COMMENT 'User ID',
    total_amount DECIMAL(12, 2) NOT NULL COMMENT 'Total amount',
    status TINYINT NOT NULL DEFAULT 0 COMMENT 'Status: 0=pending_pay, 1=paid, 2=pending_ship, 3=shipped, 4=completed, 5=cancelled',
    receiver_name VARCHAR(50) NOT NULL COMMENT 'Receiver name',
    receiver_phone VARCHAR(20) NOT NULL COMMENT 'Receiver phone',
    receiver_address VARCHAR(500) NOT NULL COMMENT 'Receiver address',
    remark VARCHAR(500) COMMENT 'Remark',
    logistics_company VARCHAR(50) DEFAULT NULL COMMENT 'Logistics company',
    tracking_no VARCHAR(64) DEFAULT NULL COMMENT 'Tracking number',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete: 0=not deleted, 1=deleted',
    UNIQUE INDEX uk_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Order table';

-- Order item table
CREATE TABLE IF NOT EXISTS t_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT 'Order ID',
    spu_id BIGINT DEFAULT NULL COMMENT 'SPU ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    sku_name VARCHAR(200) NOT NULL COMMENT 'SKU name at order time',
    price DECIMAL(10, 2) NOT NULL COMMENT 'Unit price at order time',
    quantity INT NOT NULL COMMENT 'Quantity',
    total_price DECIMAL(12, 2) NOT NULL COMMENT 'Total price for this item',
    image VARCHAR(500) COMMENT 'SKU image',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete: 0=not deleted, 1=deleted',
    INDEX idx_order_id (order_id),
    INDEX idx_sku_id (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Order item table';

-- Order status log table
CREATE TABLE IF NOT EXISTS t_order_status_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT 'Order ID',
    from_status TINYINT COMMENT 'Previous status',
    to_status TINYINT NOT NULL COMMENT 'New status',
    operator VARCHAR(50) COMMENT 'Operator',
    remark VARCHAR(200) COMMENT 'Remark',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete: 0=not deleted, 1=deleted',
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Order status change log';

-- Cart item table
CREATE TABLE IF NOT EXISTS t_cart_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT 'User ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT 'Quantity',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete: 0=not deleted, 1=deleted',
    UNIQUE INDEX uk_user_sku (user_id, sku_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Cart item table';

-- Payment table
CREATE TABLE IF NOT EXISTS t_payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT 'Order ID',
    payment_no VARCHAR(64) NOT NULL COMMENT 'Payment number',
    amount DECIMAL(12, 2) NOT NULL COMMENT 'Payment amount',
    pay_method VARCHAR(20) NOT NULL DEFAULT 'MOCK' COMMENT 'Payment method: MOCK, ALIPAY, WECHAT',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0=pending, 1=success, 2=failed',
    pay_time DATETIME COMMENT 'Actual pay time',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete: 0=not deleted, 1=deleted',
    UNIQUE INDEX uk_payment_no (payment_no),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Payment table';
