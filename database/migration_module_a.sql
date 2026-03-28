-- Cập nhật bảng tai_khoan để hỗ trợ Quên Mật Khẩu và Xác thực Email theo Module A
ALTER TABLE tai_khoan 
ADD COLUMN reset_token VARCHAR(255),
ADD COLUMN reset_token_expiry DATETIME,
ADD COLUMN verify_token VARCHAR(255);
