-- ================================================================
-- V2: Bảng room_seats - Ghế cố định theo Phòng chiếu
-- Mỗi phòng có 1 bộ ghế cố định (A1, A2, B1...).
-- Khi tạo Showtime, ghế sẽ được copy từ đây sang bảng seats.
-- ================================================================

CREATE TABLE IF NOT EXISTS room_seats (
    id BIGINT NOT NULL AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    seat_type_id INT NOT NULL DEFAULT 1,
    row_index INT NOT NULL,
    col_index INT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_room_seat (room_id, seat_number),
    KEY fk_rs_room (room_id),
    KEY fk_rs_seat_type (seat_type_id),
    CONSTRAINT fk_rs_room FOREIGN KEY (room_id) REFERENCES rooms (id),
    CONSTRAINT fk_rs_seat_type FOREIGN KEY (seat_type_id) REFERENCES seat_types (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
