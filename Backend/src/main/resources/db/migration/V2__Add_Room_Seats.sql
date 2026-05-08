-- ==========================================================
-- 1. LOẠI DANH MỤC & DỮ LIỆU GỐC (COMPONENT TYPES & MASTER DATA)
-- ==========================================================

-- Các loại danh mục dùng để tra cứu giá trị
-- Thêm các bảng này trước vì chúng được tham chiếu bởi các bảng khác
INSERT INTO component_types (id, name)
VALUES (1, 'RESERVATION_STATUS') ON DUPLICATE KEY
UPDATE name = 'RESERVATION_STATUS';
-- Tránh lỗi trùng lặp

-- Các trạng thái đặt vé - được sử dụng trong bảng reservations
-- 1=Đã xác nhận (trạng thái ban đầu), 2=Đã thanh toán, 3=Đã hủy
INSERT INTO master_data (id, data_value, component_type_id)
VALUES (1, 'CONFIRMED', 1) ON DUPLICATE KEY
UPDATE data_value = 'CONFIRMED';
INSERT INTO master_data (id, data_value, component_type_id)
VALUES (2, 'PAID', 1) ON DUPLICATE KEY
UPDATE data_value = 'PAID';
INSERT INTO master_data (id, data_value, component_type_id)
VALUES (3, 'CANCELED', 1) ON DUPLICATE KEY
UPDATE data_value = 'CANCELED';

-- BỔ SUNG CHO TICKETRUSH: Trạng thái giữ chỗ tạm thời (dành cho Flash Sale)
INSERT INTO master_data (id, data_value, component_type_id)
VALUES (4, 'LOCKED', 1) ON DUPLICATE KEY
UPDATE data_value = 'LOCKED';

-- Cập nhật bất kỳ đơn đặt vé nào hiện có (nếu có) sang sử dụng ID trạng thái mới
UPDATE reservations
SET status_id = 1
WHERE status_id IS NULL;

-- ==========================================================
-- 2. PHÂN QUYỀN & NGƯỜI DÙNG (ROLES & USERS)
-- ==========================================================

-- Các quyền (roles) dùng cho Spring Security
-- Giữ ID cố định vì chúng được tham chiếu bởi tài khoản người dùng
INSERT INTO roles (id, name)
VALUES (1, 'ROLE_ADMIN') -- Quyền Quản trị viên
    ON DUPLICATE KEY
UPDATE name = 'ROLE_ADMIN';
INSERT INTO roles (id, name)
VALUES (2, 'ROLE_USER') -- Quyền Người dùng thông thường
    ON DUPLICATE KEY
UPDATE name = 'ROLE_USER';

-- Tạo tài khoản admin nếu chưa tồn tại
-- Lưu ý: Đã sửa định dạng bcrypt để hoạt động tương thích với Spring Security
-- BỔ SUNG: tuổi (age) và giới tính (gender) cho admin
INSERT INTO users (user_name, email, password, role_id, phone_number, date_of_birth, gender)
SELECT 'admin',
       'admin@moviereview.com',
       '$2a$12$v7OOmf67vtCyNVQBcqMhbuW6pWgd7i1Z0b35qUQ5S1jkNo8CRNZrG', -- mật khẩu đã hash là 'password'
       (SELECT id FROM roles WHERE name = 'ROLE_ADMIN'),
       '0912345670',
       '1996-04-10',
       'MALE' WHERE NOT EXISTS (SELECT 1 FROM users WHERE user_name = 'admin');

-- Tài khoản user thử nghiệm - dùng để test ứng dụng
-- BỔ SUNG: tuổi (age) và giới tính (gender) cho user
INSERT INTO users (user_name, email, password, role_id, phone_number, date_of_birth, gender)
SELECT 'user',
       'user@moviereview.com',
       '$2a$12$v7OOmf67vtCyNVQBcqMhbuW6pWgd7i1Z0b35qUQ5S1jkNo8CRNZrG', -- cùng mật khẩu
       (SELECT id FROM roles WHERE name = 'ROLE_USER'),
       '0912345671',
       '2004-04-10',
       'FEMALE' WHERE NOT EXISTS (SELECT 1 FROM users WHERE user_name = 'user');

-- Thông tin đăng nhập để kiểm thử:
-- username: admin, password: password
-- username: user, password: password

-- BỔ SUNG: Thêm khách hàng mẫu để test các báo cáo thống kê
INSERT INTO users (user_name, email, password, role_id, phone_number, date_of_birth, gender)
VALUES ('hoang_nguyen', 'hoang@gmail.com', '$2a$12$v7OOmf67vtCyNVQBcqMhbuW6pWgd7i1Z0b35qUQ5S1jkNo8CRNZrG', 2,
        '0900000003', '2001-01-01', 'MALE'),
       ('thu_thuy', 'thuy@gmail.com', '$2a$12$v7OOmf67vtCyNVQBcqMhbuW6pWgd7i1Z0b35qUQ5S1jkNo8CRNZrG', 2, '0900000004',
        '2007-05-20', 'FEMALE'),
       ('minh_anh', 'minhanh@gmail.com', '$2a$12$v7OOmf67vtCyNVQBcqMhbuW6pWgd7i1Z0b35qUQ5S1jkNo8CRNZrG', 2,
        '0900000005', '1991-12-15', 'FEMALE') ON DUPLICATE KEY
UPDATE phone_number =
VALUES (phone_number);

-- ==========================================================
-- 3. RẠP & PHIM (THEATERS & MOVIES)
-- ==========================================================

-- Các rạp mẫu - sử dụng ID cố định để dễ dàng tham chiếu
-- todo: Thêm nhiều rạp hơn với các sức chứa khác nhau
INSERT INTO theaters (id, name, location, capacity)
SELECT 1,
       'Cineplex',
       'Downtown',
       150 -- Rạp cỡ trung
    WHERE NOT EXISTS (SELECT 1 FROM theaters WHERE id = 1);

INSERT INTO theaters (id, name, location, capacity)
SELECT 2,
       'MovieMax',
       'Uptown',
       200 -- Rạp cỡ lớn
    WHERE NOT EXISTS (SELECT 1 FROM theaters WHERE id = 2);

INSERT INTO theaters (id, name, location, capacity)
SELECT 3,
       'FilmHouse',
       'Westside',
       100 -- Rạp tư nhân cỡ nhỏ
    WHERE NOT EXISTS (SELECT 1 FROM theaters WHERE id = 3);

-- Đặt lại giá trị tự tăng (auto-increment) để các rạp thêm mới bắt đầu từ id=4
ALTER TABLE theaters AUTO_INCREMENT = 4;

-- Các phim mẫu kèm mô tả và đường dẫn ảnh poster thực tế
INSERT INTO movies (id, title, genre, release_year, description, poster_image_url)
VALUES (1, 'The Matrix', 'Sci-Fi', 1999, 'A computer hacker learns from mysterious rebels...',
        'https://m.media-amazon.com/images/M/MV5BNzQzOTk3OTAtNDQ0Zi00ZTVkLWI0MTEtMDllZjNkYzNjNTc4L2ltYWdlXkEyXkFqcGdeQXVyNjU0OTQ0OTY@._V1_.jpg'),
       (2, 'Inception', 'Sci-Fi', 2010, 'A thief who steals corporate secrets through the use of dream-sharing...',
        'https://m.media-amazon.com/images/M/MV5BMjAxMzY3NjcxNF5BMl5BanBnXkFtZTcwNTI5OTM0Mw@@._V1_.jpg'),
       (3, 'Pulp Fiction', 'Crime', 1994, 'The lives of two mob hitmen, a boxer, a gangster and his wife...',
        'https://m.media-amazon.com/images/M/MV5BNGNhMDIzZTUtNTBlZi00MTRlLWFjM2ItYzViMjE3YzI5MjljXkEyXkFqcGdeQXVyNzkwMjQ5NzM@._V1_.jpg'),
       (4, 'The Dark Knight', 'Action', 2008, 'When the menace known as the Joker wreaks havoc...',
        'https://m.media-amazon.com/images/M/MV5BMTMxNTMwODM0NF5BMl5BanBnXkFtZTcwODAyMTk2Mw@@._V1_.jpg'),
       (5, 'Fight Club', 'Drama', 1999, 'An insomniac office worker and a devil-may-care soapmaker...',
        'https://m.media-amazon.com/images/M/MV5BMmEzNTkxYjQtZTc0MC00YTVjLTg5ZTEtZWMwOWVlYzY0NWIwXkEyXkFqcGdeQXVyNzkwMjQ5NzM@._V1_.jpg'),
       (6, 'Forrest Gump', 'Drama', 1994, 'The presidencies of Kennedy and Johnson...',
        'https://m.media-amazon.com/images/M/MV5BNWIwODRlZTUtY2U3ZS00Yzg1LWJhNzYtMmZiYmEyNmU1NjMzXkEyXkFqcGdeQXVyMTQxNzMzNDI@._V1_.jpg'),
       (7, 'The Lord of the Rings: The Fellowship of the Ring', 'Fantasy', 2001, 'A meek Hobbit from the Shire...',
        'https://m.media-amazon.com/images/M/MV5BN2EyZjM3NzUtNWUzMi00MTgxLWI0NTctMzY4M2VlOTdjZWRiXkEyXkFqcGdeQXVyNDUzOTQ5MjY@._V1_.jpg'),
       (8, 'Interstellar', 'Sci-Fi', 2014, 'A team of explorers travel through a wormhole...',
        'https://m.media-amazon.com/images/M/MV5BZjdkOTU3MDktN2IxOS00OGEyLWFmMjktY2FiMmZkNWIyODZiXkEyXkFqcGdeQXVyMTMxODk2OTU@._V1_.jpg'),
       (9, 'The Godfather', 'Crime', 1972, 'The aging patriarch of an organized crime dynasty...',
        'https://m.media-amazon.com/images/M/MV5BM2MyNjYxNmUtYTAwNi00MTYxLWJmNWYtYzZlODY3ZTk3OTFlXkEyXkFqcGdeQXVyNzkwMjQ5NzM@._V1_.jpg'),
       (10, 'Avengers: Endgame', 'Action', 2019, 'After the devastating events of Avengers: Infinity War...',
        'https://m.media-amazon.com/images/M/MV5BMTc5MDE2ODcwNV5BMl5BanBnXkFtZTgwMzI2NzQ2NzM@._V1_.jpg') ON DUPLICATE KEY
UPDATE title =
VALUES (title);

-- Đặt lại chuỗi tự tăng sau khi đã gán ID thủ công
ALTER TABLE movies AUTO_INCREMENT = 11;

-- ==========================================================
-- 4. SUẤT CHIẾU (SHOWTIMES)
-- ==========================================================

-- Các suất chiếu mẫu cho 14 ngày tới
-- Dọn dẹp các suất chiếu cũ trước - để tránh lưu trữ dữ liệu rác
DELETE
FROM showtimes
WHERE show_date < CURDATE();
-- Xóa các ngày trong quá khứ

-- GIỮ NGUYÊN DANH SÁCH SUẤT CHIẾU
-- BỔ SUNG: Cột is_flash_sale (mặc định false, bật true cho một số suất để test)
INSERT INTO showtimes (id, movie_id, theater_id, show_date, show_time, total_seats, available_seats, price,
                       is_flash_sale)
VALUES (1, 1, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '18:00:00', 150, 150, 12.99, TRUE),
       (2, 1, 2, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '19:30:00', 200, 200, 13.99, FALSE),
       (3, 1, 3, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '20:00:00', 100, 100, 11.99, FALSE),
       (4, 2, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '20:30:00', 150, 150, 12.99, FALSE),
       (5, 2, 2, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '21:00:00', 200, 200, 13.99, FALSE),
       (6, 2, 3, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '17:30:00', 100, 100, 11.99, FALSE),
       (7, 3, 1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '19:00:00', 150, 150, 12.99, FALSE),
       (8, 3, 2, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '20:30:00', 200, 200, 13.99, FALSE),
       (9, 3, 3, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '18:00:00', 100, 100, 11.99, FALSE),
       (10, 2, 1, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '16:30:00', 150, 150, 11.99, FALSE),
       (11, 2, 1, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '20:00:00', 150, 150, 14.99, FALSE),
       (12, 2, 3, DATE_ADD(CURDATE(), INTERVAL 6 DAY), '18:30:00', 100, 100, 12.99, FALSE),
       (13, 4, 1, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '19:30:00', 150, 150, 13.99, TRUE),
       (14, 4, 2, DATE_ADD(CURDATE(), INTERVAL 5 DAY), '20:00:00', 200, 200, 14.99, FALSE),
       (15, 5, 2, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '21:30:00', 200, 200, 13.99, FALSE),
       (16, 5, 3, DATE_ADD(CURDATE(), INTERVAL 5 DAY), '19:00:00', 100, 100, 12.99, FALSE),
       (17, 6, 1, DATE_ADD(CURDATE(), INTERVAL 5 DAY), '17:00:00', 150, 150, 11.99, FALSE),
       (18, 6, 2, DATE_ADD(CURDATE(), INTERVAL 6 DAY), '18:30:00', 200, 200, 12.99, FALSE),
       (19, 7, 1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '16:00:00', 150, 150, 12.99, FALSE),
       (20, 7, 2, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '17:30:00', 200, 200, 13.99, FALSE),
       (21, 8, 2, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '19:00:00', 200, 200, 14.99, FALSE),
       (22, 9, 1, DATE_ADD(CURDATE(), INTERVAL 5 DAY), '18:00:00', 150, 150, 13.99, FALSE),
       (23, 7, 3, DATE_ADD(CURDATE(), INTERVAL 6 DAY), '17:30:00', 100, 100, 10.99, FALSE),
       (24, 8, 1, DATE_ADD(CURDATE(), INTERVAL 7 DAY), '20:00:00', 150, 150, 12.99, FALSE),
       (25, 9, 2, DATE_ADD(CURDATE(), INTERVAL 8 DAY), '19:00:00', 200, 200, 13.99, FALSE),
       (26, 10, 3, DATE_ADD(CURDATE(), INTERVAL 9 DAY), '18:30:00', 100, 100, 14.99, FALSE) ON DUPLICATE KEY
UPDATE is_flash_sale =
VALUES (is_flash_sale);

-- Đặt lại chuỗi tự tăng sau khi đã gán ID thủ công
ALTER TABLE showtimes AUTO_INCREMENT = 27;

-- ==========================================================
-- 5. SƠ ĐỒ GHẾ (SEATS)
-- ==========================================================

-- Tạo ghế cho tất cả các suất chiếu
-- Đầu tiên, dọn dẹp các ghế hiện có để tránh trùng lặp
DELETE
FROM seats
WHERE showtime_id IN (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26);

-- Tạo ghế cho suất chiếu ID 26 (Avengers: Endgame tại rạp FilmHouse)
-- FilmHouse (theater_id = 3) có 100 ghế (lưới 10x10)
INSERT INTO seats (showtime_id, seat_number, is_reserved)
VALUES
-- Hàng A
(26, 'A1', false),
(26, 'A2', false),
(26, 'A3', false),
(26, 'A4', false),
(26, 'A5', false),
(26, 'A6', false),
(26, 'A7', false),
(26, 'A8', false),
(26, 'A9', false),
(26, 'A10', false),
-- Hàng B
(26, 'B1', false),
(26, 'B2', false),
(26, 'B3', false),
(26, 'B4', false),
(26, 'B5', false),
(26, 'B6', false),
(26, 'B7', false),
(26, 'B8', false),
(26, 'B9', false),
(26, 'B10', false),
-- Hàng C
(26, 'C1', false),
(26, 'C2', false),
(26, 'C3', false),
(26, 'C4', false),
(26, 'C5', false),
(26, 'C6', false),
(26, 'C7', false),
(26, 'C8', false),
(26, 'C9', false),
(26, 'C10', false),
-- Hàng D
(26, 'D1', false),
(26, 'D2', false),
(26, 'D3', false),
(26, 'D4', false),
(26, 'D5', false),
(26, 'D6', false),
(26, 'D7', false),
(26, 'D8', false),
(26, 'D9', false),
(26, 'D10', false),
-- Hàng E
(26, 'E1', false),
(26, 'E2', false),
(26, 'E3', false),
(26, 'E4', false),
(26, 'E5', false),
(26, 'E6', false),
(26, 'E7', false),
(26, 'E8', false),
(26, 'E9', false),
(26, 'E10', false),
-- Hàng F
(26, 'F1', false),
(26, 'F2', false),
(26, 'F3', false),
(26, 'F4', false),
(26, 'F5', false),
(26, 'F6', false),
(26, 'F7', false),
(26, 'F8', false),
(26, 'F9', false),
(26, 'F10', false),
-- Hàng G
(26, 'G1', false),
(26, 'G2', false),
(26, 'G3', false),
(26, 'G4', false),
(26, 'G5', false),
(26, 'G6', false),
(26, 'G7', false),
(26, 'G8', false),
(26, 'G9', false),
(26, 'G10', false),
-- Hàng H
(26, 'H1', false),
(26, 'H2', false),
(26, 'H3', false),
(26, 'H4', false),
(26, 'H5', false),
(26, 'H6', false),
(26, 'H7', false),
(26, 'H8', false),
(26, 'H9', false),
(26, 'H10', false),
-- Hàng I
(26, 'I1', false),
(26, 'I2', false),
(26, 'I3', false),
(26, 'I4', false),
(26, 'I5', false),
(26, 'I6', false),
(26, 'I7', false),
(26, 'I8', false),
(26, 'I9', false),
(26, 'I10', false),
-- Hàng J
(26, 'J1', false),
(26, 'J2', false),
(26, 'J3', false),
(26, 'J4', false),
(26, 'J5', false),
(26, 'J6', false),
(26, 'J7', false),
(26, 'J8', false),
(26, 'J9', false),
(26, 'J10', false);

-- Tạo ghế cho các suất chiếu khác
-- Để ngắn gọn, chúng ta sẽ chỉ tạo một vài ghế cho mỗi suất chiếu (A1-A5)
-- Trong kịch bản thực tế, bạn sẽ cần tạo tất cả các ghế cho mỗi suất chiếu

-- Tạo ghế cho các suất chiếu tại rạp Cineplex (theater_id = 1)
INSERT INTO seats (showtime_id, seat_number, is_reserved)
VALUES (1, 'A1', false),
       (1, 'A2', false),
       (1, 'A3', false),
       (1, 'A4', false),
       (1, 'A5', false),
       (4, 'A1', false),
       (4, 'A2', false),
       (4, 'A3', false),
       (4, 'A4', false),
       (4, 'A5', false),
       (7, 'A1', false),
       (7, 'A2', false),
       (7, 'A3', false),
       (7, 'A4', false),
       (7, 'A5', false),
       (10, 'A1', false),
       (10, 'A2', false),
       (10, 'A3', false),
       (10, 'A4', false),
       (10, 'A5', false),
       (11, 'A1', false),
       (11, 'A2', false),
       (11, 'A3', false),
       (11, 'A4', false),
       (11, 'A5', false),
       (13, 'A1', false),
       (13, 'A2', false),
       (13, 'A3', false),
       (13, 'A4', false),
       (13, 'A5', false),
       (17, 'A1', false),
       (17, 'A2', false),
       (17, 'A3', false),
       (17, 'A4', false),
       (17, 'A5', false),
       (19, 'A1', false),
       (19, 'A2', false),
       (19, 'A3', false),
       (19, 'A4', false),
       (19, 'A5', false),
       (22, 'A1', false),
       (22, 'A2', false),
       (22, 'A3', false),
       (22, 'A4', false),
       (22, 'A5', false),
       (24, 'A1', false),
       (24, 'A2', false),
       (24, 'A3', false),
       (24, 'A4', false),
       (24, 'A5', false);

-- Tạo ghế cho các suất chiếu tại rạp MovieMax (theater_id = 2)
INSERT INTO seats (showtime_id, seat_number, is_reserved)
VALUES (2, 'A1', false),
       (2, 'A2', false),
       (2, 'A3', false),
       (2, 'A4', false),
       (2, 'A5', false),
       (5, 'A1', false),
       (5, 'A2', false),
       (5, 'A3', false),
       (5, 'A4', false),
       (5, 'A5', false),
       (8, 'A1', false),
       (8, 'A2', false),
       (8, 'A3', false),
       (8, 'A4', false),
       (8, 'A5', false),
       (14, 'A1', false),
       (14, 'A2', false),
       (14, 'A3', false),
       (14, 'A4', false),
       (14, 'A5', false),
       (15, 'A1', false),
       (15, 'A2', false),
       (15, 'A3', false),
       (15, 'A4', false),
       (15, 'A5', false),
       (18, 'A1', false),
       (18, 'A2', false),
       (18, 'A3', false),
       (18, 'A4', false),
       (18, 'A5', false),
       (20, 'A1', false),
       (20, 'A2', false),
       (20, 'A3', false),
       (20, 'A4', false),
       (20, 'A5', false),
       (21, 'A1', false),
       (21, 'A2', false),
       (21, 'A3', false),
       (21, 'A4', false),
       (21, 'A5', false),
       (25, 'A1', false),
       (25, 'A2', false),
       (25, 'A3', false),
       (25, 'A4', false),
       (25, 'A5', false);

-- Tạo ghế cho các suất chiếu tại rạp FilmHouse (theater_id = 3)
INSERT INTO seats (showtime_id, seat_number, is_reserved)
VALUES (3, 'A1', false),
       (3, 'A2', false),
       (3, 'A3', false),
       (3, 'A4', false),
       (3, 'A5', false),
       (6, 'A1', false),
       (6, 'A2', false),
       (6, 'A3', false),
       (6, 'A4', false),
       (6, 'A5', false),
       (9, 'A1', false),
       (9, 'A2', false),
       (9, 'A3', false),
       (9, 'A4', false),
       (9, 'A5', false),
       (12, 'A1', false),
       (12, 'A2', false),
       (12, 'A3', false),
       (12, 'A4', false),
       (12, 'A5', false),
       (16, 'A1', false),
       (16, 'A2', false),
       (16, 'A3', false),
       (16, 'A4', false),
       (16, 'A5', false),
       (23, 'A1', false),
       (23, 'A2', false),
       (23, 'A3', false),
       (23, 'A4', false),
       (23, 'A5', false);

-- ==========================================================
-- 6. BỔ SUNG: ĐÁNH GIÁ & LƯỢT THÍCH (REVIEWS & VOTES)
-- ==========================================================

-- Thêm các bài đánh giá mẫu
INSERT INTO reviews (user_id, movie_id, rating, comment, upvotes, downvotes)
VALUES (2, 1, 5, 'Phim quá đỉnh, kỹ xảo đi trước thời đại!', 10, 1),
       (3, 1, 4, 'Cốt truyện hơi hack não nhưng rất cuốn.', 5, 0),
       (2, 2, 5, 'Christopher Nolan chưa bao giờ làm tôi thất vọng.', 20, 2) ON DUPLICATE KEY
UPDATE comment =
VALUES (comment);

-- Thêm tương tác bình chọn mẫu (để test logic chống vote trùng)
INSERT INTO review_votes (review_id, user_id, is_upvote)
VALUES (1, 3, TRUE), -- User 3 thích review của User 2
       (1, 4, TRUE), -- User 4 thích review của User 2
       (1, 5, FALSE) -- User 5 không thích review này
    ON DUPLICATE KEY
UPDATE is_upvote =
VALUES (is_upvote);

-- Chuyển trạng thái suất chiếu ID 1 và 13 thành Flash Sale
UPDATE showtimes
SET is_flash_sale = TRUE
WHERE id IN (1, 13);