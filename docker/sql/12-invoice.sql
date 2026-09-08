USE ecommerce_order;

CREATE TABLE IF NOT EXISTS t_invoice (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  type TINYINT NOT NULL DEFAULT 1 COMMENT '1=个人,2=企业',
  title VARCHAR(200) NOT NULL COMMENT '发票抬头',
  tax_no VARCHAR(50) COMMENT '税号(企业必填)',
  amount DECIMAL(10,2) NOT NULL,
  email VARCHAR(100) COMMENT '接收邮箱',
  phone VARCHAR(20) COMMENT '联系电话',
  status TINYINT DEFAULT 0 COMMENT '0=待开票,1=已开票,2=已拒绝',
  invoice_no VARCHAR(50) COMMENT '发票号码',
  invoice_url VARCHAR(500) COMMENT '电子发票PDF地址',
  remark VARCHAR(500),
  reject_reason VARCHAR(500),
  apply_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  issue_time DATETIME,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0,
  INDEX idx_order_id (order_id),
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发票表';
