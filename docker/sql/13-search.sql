-- Search enhancement tables
USE ecommerce_product;

-- Search history table
CREATE TABLE IF NOT EXISTS t_search_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT COMMENT 'User ID, null for anonymous search',
    keyword VARCHAR(100) NOT NULL COMMENT 'Search keyword',
    search_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Search time',
    INDEX idx_user_id (user_id),
    INDEX idx_keyword (keyword)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索历史';

-- Hot search table
CREATE TABLE IF NOT EXISTS t_hot_search (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword VARCHAR(100) NOT NULL COMMENT 'Hot search keyword',
    search_count INT NOT NULL DEFAULT 0 COMMENT 'Search count',
    is_manual TINYINT NOT NULL DEFAULT 0 COMMENT '1=manually pinned to top',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Sort order, larger first',
    status TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 0=disabled, 1=enabled',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete: 0=not deleted, 1=deleted'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热门搜索';
