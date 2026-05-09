SET @has_transaction_reference := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'payments'
      AND column_name = 'transaction_reference'
);

SET @has_payment_intent_id := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'payments'
      AND column_name = 'payment_intent_id'
);

SET @rename_transaction_column_sql := IF(
    @has_transaction_reference = 0 AND @has_payment_intent_id = 1,
    'ALTER TABLE payments RENAME COLUMN payment_intent_id TO transaction_reference',
    'SELECT 1'
);

PREPARE rename_transaction_column_stmt FROM @rename_transaction_column_sql;
EXECUTE rename_transaction_column_stmt;
DEALLOCATE PREPARE rename_transaction_column_stmt;

SET @has_transaction_reference := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'payments'
      AND column_name = 'transaction_reference'
);

SET @add_transaction_reference_sql := IF(
    @has_transaction_reference = 0,
    'ALTER TABLE payments ADD COLUMN transaction_reference VARCHAR(255) NOT NULL DEFAULT ''UNKNOWN_TXN'' AFTER reservation_id',
    'SELECT 1'
);

PREPARE add_transaction_reference_stmt FROM @add_transaction_reference_sql;
EXECUTE add_transaction_reference_stmt;
DEALLOCATE PREPARE add_transaction_reference_stmt;

SET @has_provider := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'payments'
      AND column_name = 'provider'
);

SET @add_provider_column_sql := IF(
    @has_provider = 0,
    'ALTER TABLE payments ADD COLUMN provider VARCHAR(50) NOT NULL DEFAULT ''UNKNOWN'' AFTER transaction_reference',
    'SELECT 1'
);

PREPARE add_provider_column_stmt FROM @add_provider_column_sql;
EXECUTE add_provider_column_stmt;
DEALLOCATE PREPARE add_provider_column_stmt;
