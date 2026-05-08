-- ================================================================
-- V1: Full Schema + Seed Data (consolidated from MySQL dump)
-- Database: movie_ticket
-- ================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ==========================================
-- 1. LOOKUP TABLES (no FK dependencies)
-- ==========================================

CREATE TABLE IF NOT EXISTS component_types (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS roles (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS seat_types (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    price_multiplier DECIMAL(4,2) NOT NULL DEFAULT 1.00,
    PRIMARY KEY (id),
    UNIQUE KEY name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ==========================================
-- 2. MASTER DATA
-- ==========================================

CREATE TABLE IF NOT EXISTS master_data (
    id BIGINT NOT NULL AUTO_INCREMENT,
    data_value VARCHAR(50) NOT NULL,
    component_type_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY unique_master_data (component_type_id, data_value),
    CONSTRAINT fk_master_component FOREIGN KEY (component_type_id) REFERENCES component_types (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ==========================================
-- 3. CORE BUSINESS TABLES
-- ==========================================

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role_id INT NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(20) DEFAULT NULL,
    is_banned TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY user_name (user_name),
    UNIQUE KEY email (email),
    UNIQUE KEY phone_number (phone_number),
    CONSTRAINT users_ibfk_1 FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS movies (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    release_year INT DEFAULT NULL,
    genre VARCHAR(100) DEFAULT NULL,
    poster_image_url VARCHAR(255) DEFAULT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_movies_title (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS theaters (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255) DEFAULT NULL,
    capacity INT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS rooms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    theater_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    capacity INT NOT NULL,
    matrix_rows INT DEFAULT NULL,
    matrix_cols INT DEFAULT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uq_room_theater_name (theater_id, name),
    KEY idx_rooms_is_deleted (is_deleted),
    CONSTRAINT fk_room_theater FOREIGN KEY (theater_id) REFERENCES theaters (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS vouchers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    description TEXT,
    discount_percentage DECIMAL(5,2) NOT NULL,
    max_discount_amount DECIMAL(10,2) DEFAULT NULL,
    current_usage INT DEFAULT 0,
    max_usage INT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ==========================================
-- 4. SHOWTIME & SEATS
-- ==========================================

CREATE TABLE IF NOT EXISTS showtimes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    movie_id BIGINT DEFAULT NULL,
    room_id BIGINT NOT NULL,
    show_date DATE NOT NULL,
    show_time TIME NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    total_seats INT NOT NULL,
    available_seats INT NOT NULL,
    is_flash_sale TINYINT(1) DEFAULT 0,
    PRIMARY KEY (id),
    KEY movie_id (movie_id),
    KEY idx_showtimes_date (show_date),
    KEY idx_showtimes_date_movie (show_date, movie_id),
    KEY fk_showtime_room (room_id),
    CONSTRAINT fk_showtime_room FOREIGN KEY (room_id) REFERENCES rooms (id),
    CONSTRAINT showtimes_ibfk_1 FOREIGN KEY (movie_id) REFERENCES movies (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ==========================================
-- 5. RESERVATIONS & PAYMENTS
-- ==========================================

CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    showtime_id BIGINT NOT NULL,
    reservation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status_id BIGINT NOT NULL DEFAULT 1,
    total_price DECIMAL(10,2) NOT NULL,
    voucher_id BIGINT DEFAULT NULL,
    paid TINYINT(1) NOT NULL DEFAULT 0,
    expires_at TIMESTAMP NULL DEFAULT NULL,
    qr_code_hash VARCHAR(255) DEFAULT NULL,
    checkin_time TIMESTAMP NULL DEFAULT NULL,
    PRIMARY KEY (id),
    KEY fk_res_user (user_id),
    KEY fk_res_showtime (showtime_id),
    KEY idx_res_status_expiry (status_id, expires_at),
    KEY fk_res_voucher (voucher_id),
    CONSTRAINT fk_res_showtime FOREIGN KEY (showtime_id) REFERENCES showtimes (id),
    CONSTRAINT fk_res_status FOREIGN KEY (status_id) REFERENCES master_data (id),
    CONSTRAINT fk_res_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_res_voucher FOREIGN KEY (voucher_id) REFERENCES vouchers (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS seats (
    id BIGINT NOT NULL AUTO_INCREMENT,
    showtime_id BIGINT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    seat_type_id INT NOT NULL DEFAULT 1,
    is_reserved TINYINT(1) NOT NULL DEFAULT 0,
    reservation_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY unique_seat (showtime_id, seat_number),
    KEY reservation_id (reservation_id),
    KEY idx_seats_showtime_status (showtime_id, is_reserved),
    KEY fk_seat_type (seat_type_id),
    CONSTRAINT fk_seat_type FOREIGN KEY (seat_type_id) REFERENCES seat_types (id),
    CONSTRAINT seats_ibfk_1 FOREIGN KEY (showtime_id) REFERENCES showtimes (id),
    CONSTRAINT seats_ibfk_2 FOREIGN KEY (reservation_id) REFERENCES reservations (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT NOT NULL,
    transaction_reference VARCHAR(255) NOT NULL,
    provider VARCHAR(50) NOT NULL DEFAULT 'STRIPE',
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    receipt_url VARCHAR(255) DEFAULT NULL,
    pdf_receipt_path VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL DEFAULT NULL,
    PRIMARY KEY (id),
    KEY reservation_id (reservation_id),
    CONSTRAINT payments_ibfk_1 FOREIGN KEY (reservation_id) REFERENCES reservations (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ==========================================
-- 6. REVIEWS & SOCIAL
-- ==========================================

CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    movie_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL,
    upvotes INT DEFAULT 0,
    downvotes INT DEFAULT 0,
    helpful_tags VARCHAR(255) DEFAULT NULL,
    status VARCHAR(20) DEFAULT 'APPROVED',
    PRIMARY KEY (id),
    KEY user_id (user_id),
    KEY movie_id (movie_id),
    CONSTRAINT reviews_ibfk_1 FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT reviews_ibfk_2 FOREIGN KEY (movie_id) REFERENCES movies (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS review_votes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    review_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    is_upvote TINYINT(1) NOT NULL,
    voted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY unique_vote (review_id, user_id),
    KEY user_id (user_id),
    CONSTRAINT review_votes_ibfk_1 FOREIGN KEY (review_id) REFERENCES reviews (id),
    CONSTRAINT review_votes_ibfk_2 FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS user_blocks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    blocked_user_id BIGINT NOT NULL,
    blocked_by_id BIGINT NOT NULL,
    reason VARCHAR(255) DEFAULT NULL,
    blocked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_admin_block TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY unique_block (blocked_user_id, blocked_by_id),
    KEY blocked_by_id (blocked_by_id),
    CONSTRAINT user_blocks_ibfk_1 FOREIGN KEY (blocked_user_id) REFERENCES users (id),
    CONSTRAINT user_blocks_ibfk_2 FOREIGN KEY (blocked_by_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ==========================================
-- 7. QUEUE TOKENS (Virtual Queue)
-- ==========================================

CREATE TABLE IF NOT EXISTS queue_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    showtime_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL,
    status ENUM('ACTIVE','USED','EXPIRED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY unique_token (token),
    KEY fk_queue_user (user_id),
    KEY fk_queue_showtime (showtime_id),
    KEY idx_queue_token_status (token, status),
    CONSTRAINT fk_queue_showtime FOREIGN KEY (showtime_id) REFERENCES showtimes (id),
    CONSTRAINT fk_queue_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================
-- 8. SEED DATA
-- ==========================================

INSERT INTO component_types (id, name) VALUES (1, 'RESERVATION_STATUS')
    ON DUPLICATE KEY UPDATE name = name;

INSERT INTO master_data (id, data_value, component_type_id) VALUES
    (1, 'CONFIRMED', 1), (2, 'PAID', 1), (3, 'CANCELED', 1), (4, 'LOCKED', 1)
    ON DUPLICATE KEY UPDATE data_value = data_value;

INSERT INTO roles (id, name) VALUES (1, 'ROLE_ADMIN'), (2, 'ROLE_USER')
    ON DUPLICATE KEY UPDATE name = name;

INSERT INTO seat_types (id, name, price_multiplier) VALUES
    (1, 'NORMAL', 1.00), (2, 'VIP', 1.50), (3, 'COUPLE', 2.00), (4, 'PATH', 0.00)
    ON DUPLICATE KEY UPDATE name = name;

INSERT INTO theaters (id, name, location, capacity) VALUES
    (1, 'Cineplex', 'Downtown', 150),
    (2, 'MovieMax', 'Uptown', 200),
    (3, 'FilmHouse', 'Westside', 100)
    ON DUPLICATE KEY UPDATE name = name;

INSERT INTO users (id, user_name, email, password, role_id, phone_number, date_of_birth, gender, is_banned) VALUES
    (1, 'admin', 'admin@moviereview.com', '$2a$12$v7OOmf67vtCyNVQBcqMhbuW6pWgd7i1Z0b35qUQ5S1jkNo8CRNZrG', 1, '0912345670', '1996-04-10', 'MALE', 0),
    (2, 'user', 'user@moviereview.com', '$2a$12$v7OOmf67vtCyNVQBcqMhbuW6pWgd7i1Z0b35qUQ5S1jkNo8CRNZrG', 2, '0912345671', '2004-04-10', 'FEMALE', 0),
    (3, 'hoang_nguyen', 'hoang@gmail.com', '$2a$12$v7OOmf67vtCyNVQBcqMhbuW6pWgd7i1Z0b35qUQ5S1jkNo8CRNZrG', 2, '0900000003', '2001-01-01', 'MALE', 0),
    (4, 'thu_thuy', 'thuy@gmail.com', '$2a$12$v7OOmf67vtCyNVQBcqMhbuW6pWgd7i1Z0b35qUQ5S1jkNo8CRNZrG', 2, '0900000004', '2007-05-20', 'FEMALE', 0),
    (5, 'minh_anh', 'minhanh@gmail.com', '$2a$12$v7OOmf67vtCyNVQBcqMhbuW6pWgd7i1Z0b35qUQ5S1jkNo8CRNZrG', 2, '0900000005', '1991-12-15', 'FEMALE', 0)
    ON DUPLICATE KEY UPDATE user_name = user_name;

INSERT INTO movies (id, title, description, release_year, genre, poster_image_url, is_deleted) VALUES
    (1, 'The Matrix', 'A computer hacker learns from mysterious rebels...', 1999, 'Sci-Fi', 'https://m.media-amazon.com/images/M/MV5BNzQzOTk3OTAtNDQ0Zi00ZTVkLWI0MTEtMDllZjNkYzNjNTc4L2ltYWdlXkEyXkFqcGdeQXVyNjU0OTQ0OTY@._V1_.jpg', 0),
    (2, 'Inception', 'A thief who steals corporate secrets through the use of dream-sharing...', 2010, 'Sci-Fi', 'https://m.media-amazon.com/images/M/MV5BMjAxMzY3NjcxNF5BMl5BanBnXkFtZTcwNTI5OTM0Mw@@._V1_.jpg', 0),
    (3, 'Pulp Fiction', 'The lives of two mob hitmen, a boxer, a gangster and his wife...', 1994, 'Crime', 'https://m.media-amazon.com/images/M/MV5BNGNhMDIzZTUtNTBlZi00MTRlLWFjM2ItYzViMjE3YzI5MjljXkEyXkFqcGdeQXVyNzkwMjQ5NzM@._V1_.jpg', 0),
    (4, 'The Dark Knight', 'When the menace known as the Joker wreaks havoc...', 2008, 'Action', 'https://m.media-amazon.com/images/M/MV5BMTMxNTMwODM0NF5BMl5BanBnXkFtZTcwODAyMTk2Mw@@._V1_.jpg', 0),
    (5, 'Fight Club', 'An insomniac office worker and a devil-may-care soapmaker...', 1999, 'Drama', 'https://m.media-amazon.com/images/M/MV5BMmEzNTkxYjQtZTc0MC00YTVjLTg5ZTEtZWMwOWVlYzY0NWIwXkEyXkFqcGdeQXVyNzkwMjQ5NzM@._V1_.jpg', 0),
    (6, 'Forrest Gump', 'The presidencies of Kennedy and Johnson...', 1994, 'Drama', 'https://m.media-amazon.com/images/M/MV5BNWIwODRlZTUtY2U3ZS00Yzg1LWJhNzYtMmZiYmEyNmU1NjMzXkEyXkFqcGdeQXVyMTQxNzMzNDI@._V1_.jpg', 0),
    (7, 'The Lord of the Rings: The Fellowship of the Ring', 'A meek Hobbit from the Shire...', 2001, 'Fantasy', 'https://m.media-amazon.com/images/M/MV5BN2EyZjM3NzUtNWUzMi00MTgxLWI0NTctMzY4M2VlOTdjZWRiXkEyXkFqcGdeQXVyNDUzOTQ5MjY@._V1_.jpg', 0),
    (8, 'Interstellar', 'A team of explorers travel through a wormhole...', 2014, 'Sci-Fi', 'https://m.media-amazon.com/images/M/MV5BZjdkOTU3MDktN2IxOS00OGEyLWFmMjktY2FiMmZkNWIyODZiXkEyXkFqcGdeQXVyMTMxODk2OTU@._V1_.jpg', 0),
    (9, 'The Godfather', 'The aging patriarch of an organized crime dynasty...', 1972, 'Crime', 'https://m.media-amazon.com/images/M/MV5BM2MyNjYxNmUtYTAwNi00MTYxLWJmNWYtYzZlODY3ZTk3OTFlXkEyXkFqcGdeQXVyNzkwMjQ5NzM@._V1_.jpg', 0),
    (10, 'Avengers: Endgame', 'After the devastating events of Avengers: Infinity War...', 2019, 'Action', 'https://m.media-amazon.com/images/M/MV5BMTc5MDE2ODcwNV5BMl5BanBnXkFtZTgwMzI2NzQ2NzM@._V1_.jpg', 0)
    ON DUPLICATE KEY UPDATE title = title;
