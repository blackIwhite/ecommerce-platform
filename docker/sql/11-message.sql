-- Message center tables
USE ecommerce_user;

-- User message table
CREATE TABLE IF NOT EXISTS t_user_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT 'Receiver user ID',
    type TINYINT NOT NULL COMMENT 'Message type: 1=system, 2=order, 3=promotion, 4=aftersales',
    title VARCHAR(200) NOT NULL COMMENT 'Message title',
    content TEXT COMMENT 'Message content',
    reference_id BIGINT COMMENT 'Reference business ID (e.g. order id)',
    is_read TINYINT NOT NULL DEFAULT 0 COMMENT 'Read status: 0=unread, 1=read',
    read_time DATETIME COMMENT 'Time when message was read',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete: 0=not deleted, 1=deleted',
    INDEX idx_user_id (user_id),
    INDEX idx_user_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User message table';
