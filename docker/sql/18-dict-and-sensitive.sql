-- Data dictionary tables
USE ecommerce_user;

CREATE TABLE IF NOT EXISTS t_dict_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type_code VARCHAR(100) NOT NULL COMMENT '字典类型编码',
    type_name VARCHAR(200) NOT NULL COMMENT '字典类型名称',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE INDEX uk_type_code (type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典类型';

CREATE TABLE IF NOT EXISTS t_dict_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type_code VARCHAR(100) NOT NULL COMMENT '字典类型编码',
    item_value VARCHAR(200) NOT NULL COMMENT '字典项值',
    item_label VARCHAR(200) NOT NULL COMMENT '字典项标签',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_type_code (type_code),
    UNIQUE INDEX uk_type_value (type_code, item_value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典项';

-- Sensitive word table
USE ecommerce_product;

CREATE TABLE IF NOT EXISTS t_sensitive_word (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    word VARCHAR(200) NOT NULL COMMENT '敏感词',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE INDEX uk_word (word)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词库';

-- Seed some common sensitive words
INSERT INTO t_sensitive_word (word) VALUES
('代开发票'), ('套现'), ('刷单'), ('假货'), ('山寨'),
('加微信'), ('加V信'), ('私下交易'), ('转账'), ('红包');
