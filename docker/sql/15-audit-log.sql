-- Centralized audit log table (written by ecommerce-user via RabbitMQ consumer, events come from all services)
USE ecommerce_user;

-- Audit log table
CREATE TABLE IF NOT EXISTS t_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(100) COMMENT 'Source service name (spring.application.name)',
    module VARCHAR(100) COMMENT 'Business module from @AuditLog annotation',
    operation VARCHAR(100) COMMENT 'Operation name from @AuditLog annotation',
    description VARCHAR(500) COMMENT 'Operation description from @AuditLog annotation',
    method VARCHAR(255) COMMENT 'Target class and method being invoked',
    request_params VARCHAR(2000) COMMENT 'Serialized request parameters (truncated to 2000 chars)',
    response_code INT COMMENT 'Response code: 200=success, 500=business exception',
    user_id VARCHAR(64) COMMENT 'Operator user ID (nullable for anonymous calls)',
    ip VARCHAR(64) COMMENT 'Client IP address',
    duration BIGINT COMMENT 'Execution duration in milliseconds',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Time when the audited operation happened',
    INDEX idx_service_name (service_name),
    INDEX idx_module (module),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Centralized audit log table';
