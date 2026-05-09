CREATE TABLE IF NOT EXISTS rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    theater_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    capacity INT NOT NULL,
    CONSTRAINT fk_rooms_theater FOREIGN KEY (theater_id) REFERENCES theaters(id)
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

SET @has_showtimes_room_id := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'showtimes'
      AND column_name = 'room_id'
);

SET @add_showtimes_room_id_sql := IF(
    @has_showtimes_room_id = 0,
    'ALTER TABLE showtimes ADD COLUMN room_id BIGINT NULL AFTER movie_id',
    'SELECT 1'
);

PREPARE add_showtimes_room_id_stmt FROM @add_showtimes_room_id_sql;
EXECUTE add_showtimes_room_id_stmt;
DEALLOCATE PREPARE add_showtimes_room_id_stmt;

SET @has_showtimes_theater_id := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'showtimes'
      AND column_name = 'theater_id'
);

SET @backfill_showtimes_room_id_sql := IF(
    @has_showtimes_theater_id = 1,
    'UPDATE showtimes s JOIN rooms r ON r.theater_id = s.theater_id SET s.room_id = r.id WHERE s.room_id IS NULL',
    'UPDATE showtimes s SET s.room_id = (SELECT MIN(r.id) FROM rooms r) WHERE s.room_id IS NULL'
);

PREPARE backfill_showtimes_room_id_stmt FROM @backfill_showtimes_room_id_sql;
EXECUTE backfill_showtimes_room_id_stmt;
DEALLOCATE PREPARE backfill_showtimes_room_id_stmt;

ALTER TABLE showtimes MODIFY COLUMN room_id BIGINT NOT NULL;

SET @has_fk_showtimes_room := (
    SELECT COUNT(*)
    FROM information_schema.referential_constraints
    WHERE constraint_schema = DATABASE()
      AND table_name = 'showtimes'
      AND constraint_name = 'fk_showtimes_room'
);

SET @add_fk_showtimes_room_sql := IF(
    @has_fk_showtimes_room = 0,
    'ALTER TABLE showtimes ADD CONSTRAINT fk_showtimes_room FOREIGN KEY (room_id) REFERENCES rooms(id)',
    'SELECT 1'
);

PREPARE add_fk_showtimes_room_stmt FROM @add_fk_showtimes_room_sql;
EXECUTE add_fk_showtimes_room_stmt;
DEALLOCATE PREPARE add_fk_showtimes_room_stmt;

CREATE TABLE IF NOT EXISTS seat_types (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    price_multiplier DECIMAL(4, 2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO seat_types (id, name, price_multiplier)
VALUES (1, 'STANDARD', 1.00)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    price_multiplier = VALUES(price_multiplier);

SET @has_seats_seat_type_id := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'seats'
      AND column_name = 'seat_type_id'
);

SET @add_seats_seat_type_id_sql := IF(
    @has_seats_seat_type_id = 0,
    'ALTER TABLE seats ADD COLUMN seat_type_id INT NULL AFTER seat_number',
    'SELECT 1'
);

PREPARE add_seats_seat_type_id_stmt FROM @add_seats_seat_type_id_sql;
EXECUTE add_seats_seat_type_id_stmt;
DEALLOCATE PREPARE add_seats_seat_type_id_stmt;

UPDATE seats
SET seat_type_id = 1
WHERE seat_type_id IS NULL;

ALTER TABLE seats MODIFY COLUMN seat_type_id INT NOT NULL;

SET @has_fk_seats_seat_type := (
    SELECT COUNT(*)
    FROM information_schema.referential_constraints
    WHERE constraint_schema = DATABASE()
      AND table_name = 'seats'
      AND constraint_name = 'fk_seats_seat_type'
);

SET @add_fk_seats_seat_type_sql := IF(
    @has_fk_seats_seat_type = 0,
    'ALTER TABLE seats ADD CONSTRAINT fk_seats_seat_type FOREIGN KEY (seat_type_id) REFERENCES seat_types(id)',
    'SELECT 1'
);

PREPARE add_fk_seats_seat_type_stmt FROM @add_fk_seats_seat_type_sql;
EXECUTE add_fk_seats_seat_type_stmt;
DEALLOCATE PREPARE add_fk_seats_seat_type_stmt;

CREATE TABLE IF NOT EXISTS vouchers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    discount_percentage DECIMAL(5, 2) NOT NULL,
    max_discount_amount DECIMAL(10, 2),
    current_usage INT,
    max_usage INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET @has_fk_reservations_voucher := (
    SELECT COUNT(*)
    FROM information_schema.referential_constraints
    WHERE constraint_schema = DATABASE()
      AND table_name = 'reservations'
      AND constraint_name = 'fk_reservations_voucher'
);

SET @add_fk_reservations_voucher_sql := IF(
    @has_fk_reservations_voucher = 0,
    'ALTER TABLE reservations ADD CONSTRAINT fk_reservations_voucher FOREIGN KEY (voucher_id) REFERENCES vouchers(id)',
    'SELECT 1'
);

PREPARE add_fk_reservations_voucher_stmt FROM @add_fk_reservations_voucher_sql;
EXECUTE add_fk_reservations_voucher_stmt;
DEALLOCATE PREPARE add_fk_reservations_voucher_stmt;
