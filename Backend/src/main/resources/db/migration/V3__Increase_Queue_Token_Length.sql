-- Thay đổi độ dài cột token để có thể lưu vừa chuỗi JWT (được ký bằng HS512 có thể dài hơn 255 ký tự)
ALTER TABLE queue_tokens MODIFY token VARCHAR(512) NOT NULL;
