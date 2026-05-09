SET @has_voucher_id := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'reservations'
      AND column_name = 'voucher_id'
);

SET @add_voucher_id_sql := IF(
    @has_voucher_id = 0,
    'ALTER TABLE reservations ADD COLUMN voucher_id BIGINT NULL AFTER total_price',
    'SELECT 1'
);

PREPARE add_voucher_id_stmt FROM @add_voucher_id_sql;
EXECUTE add_voucher_id_stmt;
DEALLOCATE PREPARE add_voucher_id_stmt;
