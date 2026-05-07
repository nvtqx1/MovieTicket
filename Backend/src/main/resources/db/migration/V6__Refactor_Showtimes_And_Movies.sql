-- Task 3.1: Soft Delete cho Movie
ALTER TABLE movies ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- Task 2.1: Chuẩn hóa lại bảng showtimes (Xóa cột room dư thừa)
ALTER TABLE showtimes DROP COLUMN room;
