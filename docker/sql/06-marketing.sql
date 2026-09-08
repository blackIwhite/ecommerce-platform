-- Marketing module tables

-- Coupon template
CREATE TABLE IF NOT EXISTS t_coupon_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT 'Coupon name',
    type TINYINT NOT NULL COMMENT 'Type: 1=fixed, 2=percentage',
    discount_value DECIMAL(10,2) NOT NULL COMMENT 'Discount value (amount or percentage)',
    min_purchase DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'Minimum purchase amount',
    max_discount DECIMAL(10,2) DEFAULT NULL COMMENT 'Max discount for percentage type',
    total_count INT NOT NULL COMMENT 'Total issued count, -1 for unlimited',
    claimed_count INT NOT NULL DEFAULT 0 COMMENT 'Already claimed count',
    per_limit INT NOT NULL DEFAULT 1 COMMENT 'Per-user claim limit',
    start_time DATETIME NOT NULL COMMENT 'Coupon valid start time',
    end_time DATETIME NOT NULL COMMENT 'Coupon valid end time',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=active, 0=disabled',
    description VARCHAR(500) DEFAULT NULL COMMENT 'Description',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_status (status),
    INDEX idx_time (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Coupon template';

-- User coupon instance
CREATE TABLE IF NOT EXISTS t_user_coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT 'User ID',
    template_id BIGINT NOT NULL COMMENT 'Coupon template ID',
    coupon_name VARCHAR(100) NOT NULL COMMENT 'Coupon name (snapshot)',
    type TINYINT NOT NULL COMMENT 'Type: 1=fixed, 2=percentage',
    discount_value DECIMAL(10,2) NOT NULL COMMENT 'Discount value',
    min_purchase DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'Minimum purchase amount',
    max_discount DECIMAL(10,2) DEFAULT NULL COMMENT 'Max discount for percentage type',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0=unused, 1=used, 2=expired',
    order_id BIGINT DEFAULT NULL COMMENT 'Associated order ID when used',
    use_time DATETIME DEFAULT NULL COMMENT 'Usage time',
    expire_time DATETIME NOT NULL COMMENT 'Expiration time for this instance',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_user_status (user_id, status),
    INDEX idx_template (template_id),
    INDEX idx_expire (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User coupon instance';

-- Promotion
CREATE TABLE IF NOT EXISTS t_promotion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT 'Promotion name',
    type TINYINT NOT NULL COMMENT 'Type: 1=full reduction, 2=limited discount, 3=flash sale',
    rules TEXT NOT NULL COMMENT 'Rules JSON',
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=active, 0=disabled',
    description VARCHAR(500) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_status_time (status, start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Promotion';

-- Flash sale item
CREATE TABLE IF NOT EXISTS t_flash_sale_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    promotion_id BIGINT NOT NULL COMMENT 'Promotion ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    flash_price DECIMAL(10,2) NOT NULL COMMENT 'Flash sale price',
    total_stock INT NOT NULL COMMENT 'Total flash sale stock',
    available_stock INT NOT NULL COMMENT 'Available stock',
    limit_per_user INT NOT NULL DEFAULT 1 COMMENT 'Per-user purchase limit',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_promotion (promotion_id),
    INDEX idx_sku (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Flash sale item';

-- Points account
CREATE TABLE IF NOT EXISTS t_points_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT 'User ID',
    total_points INT NOT NULL DEFAULT 0 COMMENT 'Total earned points',
    available_points INT NOT NULL DEFAULT 0 COMMENT 'Available points',
    used_points INT NOT NULL DEFAULT 0 COMMENT 'Used points',
    expired_points INT NOT NULL DEFAULT 0 COMMENT 'Expired points',
    level TINYINT NOT NULL DEFAULT 1 COMMENT 'Level: 1=normal, 2=silver, 3=gold, 4=diamond',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE INDEX uk_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Points account';

-- Points log
CREATE TABLE IF NOT EXISTS t_points_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT 'User ID',
    type TINYINT NOT NULL COMMENT 'Type: 1=earn, 2=spend, 3=expire',
    points INT NOT NULL COMMENT 'Points amount',
    balance INT NOT NULL COMMENT 'Balance after this transaction',
    source VARCHAR(50) DEFAULT NULL COMMENT 'Source: order, review, admin, etc.',
    reference_id BIGINT DEFAULT NULL COMMENT 'Reference ID (order ID, etc.)',
    description VARCHAR(200) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_user_type (user_id, type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Points log';

-- Points rule
CREATE TABLE IF NOT EXISTS t_points_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_key VARCHAR(50) NOT NULL COMMENT 'Rule key',
    rule_value VARCHAR(500) NOT NULL COMMENT 'Rule value (JSON)',
    description VARCHAR(200) DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=active, 0=disabled',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE INDEX uk_key (rule_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Points rule';

-- Default points rules
INSERT IGNORE INTO t_points_rule (rule_key, rule_value, description, status) VALUES
('earn_ratio', '{"ratio":1}', '每消费1元获得积分数', 1),
('redeem_ratio', '{"ratio":100}', '多少积分抵扣1元', 1),
('order_earn', '{"enabled":true}', '下单是否获得积分', 1),
('review_earn', '{"enabled":true,"points":10}', '评价获得积分', 1);
