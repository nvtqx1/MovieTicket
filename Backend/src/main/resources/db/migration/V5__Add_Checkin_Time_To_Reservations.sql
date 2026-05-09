SET @has_checkin_time := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'reservations'
      AND column_name = 'checkin_time'
);

SET @add_checkin_time_sql := IF(
    @has_checkin_time = 0,
    'ALTER TABLE reservations ADD COLUMN checkin_time TIMESTAMP NULL AFTER qr_code_hash',
    'SELECT 1'
);

PREPARE add_checkin_time_stmt FROM @add_checkin_time_sql;
EXECUTE add_checkin_time_stmt;
DEALLOCATE PREPARE add_checkin_time_stmt;
