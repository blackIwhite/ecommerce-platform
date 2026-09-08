-- CMS (Content Management System) tables, stored in the product database
USE ecommerce_product;

-- Article category table
CREATE TABLE IF NOT EXISTS t_article_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT 'Category name',
    code VARCHAR(50) NOT NULL COMMENT 'Category code',
    parent_id BIGINT DEFAULT 0 COMMENT 'Parent category ID, 0 for root',
    sort_order INT DEFAULT 0 COMMENT 'Sort order',
    status TINYINT DEFAULT 1 COMMENT 'Status: 0=disabled, 1=enabled',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT 'Logical delete: 0=not deleted, 1=deleted',
    INDEX idx_parent_id (parent_id),
    INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Article category table';

-- Article table
CREATE TABLE IF NOT EXISTS t_article (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT 'Article title',
    slug VARCHAR(200) COMMENT 'URL friendly identifier',
    content TEXT COMMENT 'Article content',
    summary VARCHAR(500) COMMENT 'Article summary',
    cover_image VARCHAR(500) COMMENT 'Cover image URL',
    category_id BIGINT COMMENT 'Article category ID',
    author VARCHAR(100) COMMENT 'Author name',
    status TINYINT DEFAULT 0 COMMENT '0=draft,1=published,2=archived',
    sort_order INT DEFAULT 0 COMMENT 'Sort order',
    view_count INT DEFAULT 0 COMMENT 'View count',
    publish_time DATETIME COMMENT 'Publish time',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT 'Logical delete: 0=not deleted, 1=deleted',
    INDEX idx_category_id (category_id),
    INDEX idx_status (status),
    INDEX idx_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Article table';

INSERT INTO t_article_category (name, code, sort_order) VALUES
('平台公告', 'announcement', 1),
('帮助中心', 'help', 2),
('行业资讯', 'news', 3);
