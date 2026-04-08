CREATE TABLE IF NOT EXISTS component_types (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS master_data (
    id INT AUTO_INCREMENT PRIMARY KEY,
    master_data_id INT NOT NULL,      -- ID nghiệp vụ (VD: 1, 2, 3)
    data_value VARCHAR(100) NOT NULL, -- Tên hiển thị (VD: 'PAID')
    component_type_id INT NOT NULL,   -- Liên kết với nhóm danh mục
    CONSTRAINT fk_component_type
    FOREIGN KEY (component_type_id) REFERENCES component_types(id),
    -- Đảm bảo không trùng lặp cặp (loại danh mục, id nghiệp vụ)
    UNIQUE KEY unique_master_data (component_type_id, master_data_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(50) NOT NULL UNIQUE, -- Tên đăng nhập
    email VARCHAR(100) NOT NULL,            -- Email liên hệ
    password VARCHAR(255) NOT NULL,         -- Mật khẩu (đã hash BCrypt)
    role_id INT NOT NULL,                   -- Khóa ngoại liên kết bảng roles
    age INT,                                -- Tuổi (Bổ sung cho TicketRush)
    gender VARCHAR(20),                     -- Giới tính (Bổ sung cho TicketRush)
    FOREIGN KEY (role_id) REFERENCES roles(id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS movies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,        -- Tên phim
    description TEXT,                   -- Nội dung tóm tắt
    release_year INT,                   -- Năm sản xuất
    genre VARCHAR(100),                 -- Thể loại
    poster_image_url VARCHAR(255)       -- Đường dẫn ảnh poster
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS theaters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,         -- Tên rạp (VD: Cineplex 1)
    location VARCHAR(255),              -- Địa chỉ rạp
    capacity INT NOT NULL               -- Sức chứa (Số ghế tối đa)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS showtimes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    movie_id BIGINT,                    -- Khóa ngoại liên kết bảng movies
    theater_id BIGINT,                  -- Khóa ngoại liên kết bảng theaters
    show_date DATE NOT NULL,            -- Ngày chiếu
    show_time TIME NOT NULL,            -- Giờ chiếu
    price DECIMAL(10, 2) NOT NULL,      -- Giá vé
    total_seats INT NOT NULL,           -- Tổng số ghế mở bán
    available_seats INT NOT NULL,       -- Số ghế hiện còn trống
    is_flash_sale BOOLEAN DEFAULT FALSE, -- Đánh giá có thuộc Flash Sale hay không (Bổ sung cho TicketRush)
    FOREIGN KEY (movie_id) REFERENCES movies(id),
    FOREIGN KEY (theater_id) REFERENCES theaters(id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,               -- Ai mua?
    showtime_id BIGINT NOT NULL,           -- Mua suất nào?
    reservation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Thời điểm bấm đặt
    -- Trạng thái: 1-LOCKED (Giữ chỗ), 2-PAID (Đã thanh toán), 3-CANCELED (Đã hủy)
    status_id INT NOT NULL DEFAULT 1,
    total_price DECIMAL(10, 2) NOT NULL,   -- Tổng tiền đơn hàng
    paid BOOLEAN NOT NULL DEFAULT FALSE,   -- Cờ đánh dấu đã thanh toán thành công hay chưa
    -- CHI TIẾT BỔ SUNG CHO TICKETRUSH:
    expires_at TIMESTAMP NULL,             -- Thời điểm hết hạn (thường là reservation_time + 10p)
    qr_code_hash VARCHAR(255) NULL,        -- Mã băm để sinh QR code sau khi thanh toán thành công
    -- Ràng buộc khóa ngoại
    CONSTRAINT fk_res_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_res_showtime FOREIGN KEY (showtime_id) REFERENCES showtimes(id),
    CONSTRAINT fk_res_status FOREIGN KEY (status_id) REFERENCES master_data(id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    showtime_id BIGINT NOT NULL,           -- Khóa ngoại tới suất chiếu
    seat_number VARCHAR(10) NOT NULL,      -- Số ghế (A1, B5,...)
    is_reserved BOOLEAN NOT NULL DEFAULT FALSE, -- Trạng thái đã đặt hay chưa
    reservation_id BIGINT,                 -- Khóa ngoại tới đơn hàng (nếu có)
    FOREIGN KEY (showtime_id) REFERENCES showtimes(id),
    FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    UNIQUE KEY unique_seat (showtime_id, seat_number) -- Chống tạo trùng ghế
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    movie_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL,
    upvotes INT DEFAULT 0,
    downvotes INT DEFAULT 0,
    helpful_tags VARCHAR(255),
    status VARCHAR(20) DEFAULT 'APPROVED',

    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (movie_id) REFERENCES movies(id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS review_votes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    review_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    is_upvote BOOLEAN NOT NULL,
    voted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (review_id) REFERENCES reviews(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY unique_vote (review_id, user_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS user_blocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    blocked_user_id BIGINT NOT NULL,        -- ID người bị chặn
    blocked_by_id BIGINT NOT NULL,          -- ID người thực hiện chặn
    reason VARCHAR(255),                    -- Lý do chặn
    blocked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_admin_block BOOLEAN NOT NULL DEFAULT FALSE, -- Chặn bởi Admin hay User
    FOREIGN KEY (blocked_user_id) REFERENCES users(id),
    FOREIGN KEY (blocked_by_id) REFERENCES users(id),
    UNIQUE KEY unique_block (blocked_user_id, blocked_by_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL,        -- Liên kết đơn hàng
    payment_intent_id VARCHAR(255) NOT NULL, -- ID giao dịch từ Stripe
    amount DECIMAL(10, 2) NOT NULL,        -- Số tiền thực thu
    status VARCHAR(50) NOT NULL,           -- Trạng thái thanh toán
    receipt_url VARCHAR(255),              -- Link biên lai online
    pdf_receipt_path VARCHAR(255),         -- Đường dẫn file PDF hóa đơn
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL DEFAULT NULL,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS queue_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,           -- Người dùng được phép vào mua
    showtime_id BIGINT NOT NULL,       -- Suất chiếu cụ thể được phép vào
    token VARCHAR(255) NOT NULL,       -- Chuỗi định danh duy nhất (UUID)
    -- Trạng thái để quản lý vòng đời token
    status ENUM('ACTIVE', 'USED', 'EXPIRED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- Ràng buộc khóa ngoại
    CONSTRAINT fk_queue_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_queue_showtime FOREIGN KEY (showtime_id) REFERENCES showtimes(id),
    -- Đảm bảo một token là duy nhất và tìm kiếm nhanh
    UNIQUE KEY unique_token (token)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

    -- [INDEXES BỔ SUNG CHO TICKETRUSH]

    -- 1. Tối ưu cho Worker quét vé hết hạn
CREATE INDEX idx_res_status_expiry ON reservations(status_id, expires_at);

    -- 2. Lấy danh sách ghế của một suất chiếu (kèm trạng thái trống/đã đặt)
CREATE INDEX idx_seats_showtime_status ON seats(showtime_id, is_reserved);

    -- 3. Tối ưu cho việc hiển thị suất chiếu theo ngày
CREATE INDEX idx_showtimes_date ON showtimes(show_date);

    -- 4. Lọc suất chiếu theo ngày và phim
CREATE INDEX idx_showtimes_date_movie ON showtimes(show_date, movie_id);

    -- 5.  Tìm kiếm phim theo tên (nếu bạn có tính năng Search)
CREATE INDEX idx_movies_title ON movies(title);

    -- 6. Tối ưu cho việc kiểm tra Token hàng chờ
CREATE INDEX idx_queue_token_status ON queue_tokens(token, status);


