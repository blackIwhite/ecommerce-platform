USE ecommerce_user;

CREATE TABLE IF NOT EXISTS t_user_browse_history (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  spu_id BIGINT NOT NULL,
  browse_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  duration INT DEFAULT 0 COMMENT '浏览时长(秒)',
  INDEX idx_user_id (user_id),
  INDEX idx_spu_id (spu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='浏览历史';

CREATE TABLE IF NOT EXISTS t_user_behavior_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  action VARCHAR(50) NOT NULL COMMENT 'browse/cart/favorite/share/search/purchase',
  target_type VARCHAR(50) COMMENT 'spu/sku/order/coupon',
  target_id BIGINT,
  extra_data VARCHAR(500) COMMENT 'JSON扩展数据',
  ip VARCHAR(50),
  user_agent VARCHAR(500),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_action (user_id, action),
  INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为日志';
