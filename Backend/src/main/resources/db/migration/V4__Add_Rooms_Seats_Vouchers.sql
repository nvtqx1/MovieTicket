-- Upgrade theaters/showtimes to room-based scheduling and add ticketing support tables.

CREATE TABLE IF NOT EXISTS rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    theater_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    capacity INT NOT NULL,
    CONSTRAINT fk_room_theater FOREIGN KEY (theater_id) REFERENCES theaters(id),
    CONSTRAINT uq_room_theater_name UNIQUE (theater_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO rooms (theater_id, name, capacity)
SELECT t.id, 'Room A', t.capacity
FROM theaters t
WHERE NOT EXISTS (
    SELECT 1
    FROM rooms r
    WHERE r.theater_id = t.id
      AND r.name = 'Room A'
);

ALTER TABLE showtimes
ADD COLUMN room_id BIGINT NULL AFTER movie_id;

UPDATE showtimes s
JOIN rooms r ON r.theater_id = s.theater_id AND r.name = 'Room A'
SET s.room_id = r.id
WHERE s.room_id IS NULL;

ALTER TABLE showtimes
MODIFY COLUMN room_id BIGINT NOT NULL;

ALTER TABLE showtimes
ADD CONSTRAINT fk_showtime_room FOREIGN KEY (room_id) REFERENCES rooms(id);

SET @showtimes_theater_fk = (
    SELECT constraint_name
    FROM information_schema.key_column_usage
    WHERE table_schema = DATABASE()
      AND table_name = 'showtimes'
      AND column_name = 'theater_id'
      AND referenced_table_name = 'theaters'
    LIMIT 1
);

SET @drop_showtimes_theater_fk_sql = IF(
    @showtimes_theater_fk IS NOT NULL,
    CONCAT('ALTER TABLE showtimes DROP FOREIGN KEY ', @showtimes_theater_fk),
    'SELECT 1'
);
PREPARE drop_showtimes_theater_fk_stmt FROM @drop_showtimes_theater_fk_sql;
EXECUTE drop_showtimes_theater_fk_stmt;
DEALLOCATE PREPARE drop_showtimes_theater_fk_stmt;

ALTER TABLE showtimes
DROP COLUMN theater_id;

CREATE TABLE IF NOT EXISTS seat_types (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    price_multiplier DECIMAL(4, 2) NOT NULL DEFAULT 1.0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO seat_types (name, price_multiplier) VALUES
('NORMAL', 1.00),
('VIP', 1.50),
('COUPLE', 2.00),
('PATH', 0.00)
ON DUPLICATE KEY UPDATE price_multiplier = VALUES(price_multiplier);

ALTER TABLE seats
ADD COLUMN seat_type_id INT NOT NULL DEFAULT 1 AFTER seat_number,
ADD CONSTRAINT fk_seat_type FOREIGN KEY (seat_type_id) REFERENCES seat_types(id);

ALTER TABLE payments
CHANGE COLUMN payment_intent_id transaction_reference VARCHAR(255) NOT NULL,
ADD COLUMN provider VARCHAR(50) NOT NULL DEFAULT 'STRIPE' AFTER transaction_reference;

CREATE TABLE IF NOT EXISTS vouchers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    discount_percentage DECIMAL(5, 2) NOT NULL,
    max_discount_amount DECIMAL(10, 2),
    current_usage INT DEFAULT 0,
    max_usage INT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE reservations
ADD COLUMN voucher_id BIGINT NULL AFTER total_price,
ADD COLUMN checkin_time TIMESTAMP NULL AFTER qr_code_hash,
ADD CONSTRAINT fk_res_voucher FOREIGN KEY (voucher_id) REFERENCES vouchers(id);
