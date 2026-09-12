-- Shipping fee template tables
USE ecommerce_product;

CREATE TABLE IF NOT EXISTS t_shipping_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    charge_type TINYINT NOT NULL DEFAULT 1 COMMENT '计费方式: 1=按件 2=按重量(kg) 3=按体积(m³)',
    default_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '默认运费',
    free_threshold DECIMAL(10,2) DEFAULT NULL COMMENT '满额包邮阈值, NULL表示不包邮',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运费模板';

CREATE TABLE IF NOT EXISTS t_shipping_template_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL COMMENT '模板ID',
    region_codes TEXT NOT NULL COMMENT '适用地区编码, 逗号分隔, ALL=全国',
    region_names VARCHAR(500) NOT NULL DEFAULT '' COMMENT '适用地区名称',
    start_threshold DECIMAL(10,2) NOT NULL DEFAULT 1.00 COMMENT '起始计量(件/kg/m³)',
    start_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '起始运费',
    additional_threshold DECIMAL(10,2) NOT NULL DEFAULT 1.00 COMMENT '每增加计量',
    additional_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '每增加运费',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_template_id (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运费模板规则';
