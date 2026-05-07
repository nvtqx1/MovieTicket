-- =============================================================
-- V7: Ban User + Chuẩn hóa thêm
-- =============================================================

-- Task 1.3: Thêm cột is_banned cho User (Soft-ban)
ALTER TABLE users ADD COLUMN is_banned BOOLEAN NOT NULL DEFAULT FALSE;
