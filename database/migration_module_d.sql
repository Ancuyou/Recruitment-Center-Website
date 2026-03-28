-- ================================================================
-- MIGRATION MODULE D: Bảng Thong Bao (Notification)
-- ================================================================

USE he_thong_tuyen_dung;

CREATE TABLE IF NOT EXISTS thong_bao (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tai_khoan_id BIGINT NOT NULL,
    tieu_de VARCHAR(255) NOT NULL,
    noi_dung TEXT NOT NULL,
    loai_thong_bao ENUM('UNG_TUYEN', 'PHONG_VAN', 'HE_THONG') NOT NULL DEFAULT 'HE_THONG',
    lien_ket VARCHAR(255),
    da_doc BOOLEAN DEFAULT FALSE,
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tai_khoan_id) REFERENCES tai_khoan(id) ON DELETE CASCADE,
    INDEX idx_tai_khoan_tb (tai_khoan_id),
    INDEX idx_da_doc (da_doc),
    INDEX idx_ngay_tao (ngay_tao)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
