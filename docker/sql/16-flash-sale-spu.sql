-- Add missing spu_id column to t_flash_sale_item (entity has spuId but DDL was missing the column)
ALTER TABLE t_flash_sale_item ADD COLUMN spu_id BIGINT NOT NULL DEFAULT 0 COMMENT 'SPU ID' AFTER promotion_id;
