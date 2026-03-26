-- ================================================================
-- PHẦN 1: KHỞI TẠO DATABASE & CÁC BẢNG (DDL)
-- ================================================================

DROP DATABASE IF EXISTS he_thong_tuyen_dung;
CREATE DATABASE he_thong_tuyen_dung CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE he_thong_tuyen_dung;

-- 1. tài_khoản
CREATE TABLE tai_khoan (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           email VARCHAR(255) UNIQUE NOT NULL,
                           mat_khau_hash VARCHAR(255) NOT NULL,
                           vai_tro ENUM('UNG_VIEN', 'NHA_TUYEN_DUNG', 'ADMIN') NOT NULL,
                           la_kich_hoat BOOLEAN DEFAULT TRUE,
                           ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           INDEX idx_email (email),
                           INDEX idx_vai_tro (vai_tro)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. ung_vien
CREATE TABLE ung_vien (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          tai_khoan_id BIGINT UNIQUE NOT NULL,
                          ho_ten VARCHAR(150) NOT NULL,
                          so_dien_thoai VARCHAR(20) UNIQUE,
                          ngay_sinh DATE,
                          gioi_tinh ENUM('NAM', 'NU', 'KHAC'),
                          anh_dai_dien VARCHAR(255),
                          ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          FOREIGN KEY (tai_khoan_id) REFERENCES tai_khoan(id) ON DELETE CASCADE,
                          INDEX idx_so_dien_thoai (so_dien_thoai)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. nha_tuyen_dung
CREATE TABLE nha_tuyen_dung (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                tai_khoan_id BIGINT UNIQUE NOT NULL,
                                cong_ty_id BIGINT NOT NULL,
                                ho_ten VARCHAR(150) NOT NULL,
                                chuc_vu VARCHAR(100),
                                so_dien_thoai VARCHAR(20),
                                ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                FOREIGN KEY (tai_khoan_id) REFERENCES tai_khoan(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. công_ty
CREATE TABLE cong_ty (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         ten_cong_ty VARCHAR(255) NOT NULL,
                         ma_so_thue VARCHAR(50) UNIQUE,
                         logo_url VARCHAR(255),
                         nganh_nghe VARCHAR(100),
                         website VARCHAR(255),
                         mo_ta TEXT,
                         ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         INDEX idx_ma_so_thue (ma_so_thue),
                         INDEX idx_nganh_nghe (nganh_nghe)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Thêm FK sau khi tạo bảng công_ty (nha_tuyen_dung → công_ty)
ALTER TABLE nha_tuyen_dung
    ADD CONSTRAINT fk_nha_tuyen_dung_cong_ty
        FOREIGN KEY (cong_ty_id) REFERENCES cong_ty(id);
CREATE INDEX idx_cong_ty ON nha_tuyen_dung (cong_ty_id);

-- 5. tin_tuyen_dung
CREATE TABLE tin_tuyen_dung (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                nha_tuyen_dung_id BIGINT NOT NULL,
                                cong_ty_id BIGINT NOT NULL,
                                tieu_de VARCHAR(255) NOT NULL,
                                mo_ta_cong_viec TEXT NOT NULL,
                                yeu_cau_ung_vien TEXT NOT NULL,
                                muc_luong_min DECIMAL(10,2),
                                muc_luong_max DECIMAL(10,2),
                                dia_diem VARCHAR(100),
                                cap_bac_yeu_cau ENUM('FRESHER', 'JUNIOR', 'SENIOR', 'LEAD') NOT NULL DEFAULT 'JUNIOR',
                                hinh_thuc_lam_viec ENUM('ONLINE', 'OFFICE', 'HYBRID') NOT NULL DEFAULT 'OFFICE',
                                han_nop DATE NOT NULL,
                                trang_thai TINYINT DEFAULT 1 COMMENT '0: Nháp, 1: Mở, 2: Đóng',
                                ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                FOREIGN KEY (nha_tuyen_dung_id) REFERENCES nha_tuyen_dung(id),
                                FOREIGN KEY (cong_ty_id) REFERENCES cong_ty(id),
                                INDEX idx_nha_tuyen_dung (nha_tuyen_dung_id),
                                INDEX idx_cong_ty_tin (cong_ty_id),
                                INDEX idx_trang_thai (trang_thai),
                                INDEX idx_han_nop (han_nop),
                                INDEX idx_cap_bac (cap_bac_yeu_cau),
                                INDEX idx_hinh_thuc (hinh_thuc_lam_viec)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. kỹ_năng
CREATE TABLE ky_nang (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         ten_ky_nang VARCHAR(100) UNIQUE NOT NULL,
                         ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         INDEX idx_ten_ky_nang (ten_ky_nang)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. chi_tiết_kỹ_năng_tin
CREATE TABLE chi_tiet_ky_nang_tin (
                                      tin_tuyen_dung_id BIGINT NOT NULL,
                                      ky_nang_id BIGINT NOT NULL,
                                      muc_yeu_cau TINYINT NOT NULL CHECK (muc_yeu_cau BETWEEN 1 AND 5),
                                      ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      PRIMARY KEY (tin_tuyen_dung_id, ky_nang_id),
                                      FOREIGN KEY (tin_tuyen_dung_id) REFERENCES tin_tuyen_dung(id) ON DELETE CASCADE,
                                      FOREIGN KEY (ky_nang_id) REFERENCES ky_nang(id) ON DELETE CASCADE,
                                      INDEX idx_ky_nang_tin (ky_nang_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. hồ_sơ_cv
CREATE TABLE ho_so_cv (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          ung_vien_id BIGINT NOT NULL,
                          tieu_de_cv VARCHAR(100) NOT NULL,
                          muc_tieu_nghe_nghiep TEXT,
                          file_cv_url VARCHAR(255),
                          la_cv_chinh BOOLEAN DEFAULT FALSE,
                          da_xoa BOOLEAN DEFAULT FALSE,
                          ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          FOREIGN KEY (ung_vien_id) REFERENCES ung_vien(id) ON DELETE CASCADE,
                          INDEX idx_ung_vien_cv (ung_vien_id),
                          INDEX idx_la_cv_chinh (la_cv_chinh)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. chi_tiết_kỹ_năng_cv
CREATE TABLE chi_tiet_ky_nang_cv (
                                     ho_so_cv_id BIGINT NOT NULL,
                                     ky_nang_id BIGINT NOT NULL,
                                     muc_thanh_thao TINYINT NOT NULL CHECK (muc_thanh_thao BETWEEN 1 AND 5),
                                     ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     PRIMARY KEY (ho_so_cv_id, ky_nang_id),
                                     FOREIGN KEY (ho_so_cv_id) REFERENCES ho_so_cv(id) ON DELETE CASCADE,
                                     FOREIGN KEY (ky_nang_id) REFERENCES ky_nang(id) ON DELETE CASCADE,
                                     INDEX idx_ky_nang_cv (ky_nang_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. chi_tiết_cv
CREATE TABLE chi_tiet_cv (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             ho_so_cv_id BIGINT NOT NULL,
                             loai_ban_ghi TINYINT NOT NULL COMMENT '1: Học vấn, 2: Kinh nghiệm',
                             ten_to_chuc VARCHAR(200) NOT NULL,
                             chuyen_nganh_hoac_vi_tri VARCHAR(200),
                             ngay_bat_dau DATE NOT NULL,
                             ngay_ket_thuc DATE,
                             mo_ta_chi_tiet TEXT,
                             ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             FOREIGN KEY (ho_so_cv_id) REFERENCES ho_so_cv(id) ON DELETE CASCADE,
                             INDEX idx_ho_so_cv_ct (ho_so_cv_id),
                             INDEX idx_loai_ban_ghi (loai_ban_ghi)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. đơn_ứng_tuyển
CREATE TABLE don_ung_tuyen (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               tin_tuyen_dung_id BIGINT NOT NULL,
                               ho_so_cv_id BIGINT NOT NULL,
                               ban_sao_cv_url VARCHAR(255),
                               thu_ngo TEXT,
                               trang_thai_hien_tai TINYINT DEFAULT 1 COMMENT '1: Mới, 2: Review, 3: Phỏng vấn, 4: Offer, 5: Từ chối',
                               ngay_nop TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (tin_tuyen_dung_id) REFERENCES tin_tuyen_dung(id),
                               FOREIGN KEY (ho_so_cv_id) REFERENCES ho_so_cv(id),
                               INDEX idx_tin_tuyen_dung_don (tin_tuyen_dung_id),
                               INDEX idx_ho_so_cv_don (ho_so_cv_id),
                               INDEX idx_trang_thai_don (trang_thai_hien_tai),
                               INDEX idx_ngay_nop (ngay_nop)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. lịch_sử_trạng_thái
CREATE TABLE lich_su_trang_thai (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    don_ung_tuyen_id BIGINT NOT NULL,
                                    nguoi_thuc_hien_id BIGINT NOT NULL,
                                    trang_thai_cu TINYINT,
                                    trang_thai_moi TINYINT NOT NULL,
                                    ghi_chu TEXT,
                                    thoi_gian_chuyen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    FOREIGN KEY (don_ung_tuyen_id) REFERENCES don_ung_tuyen(id) ON DELETE CASCADE,
                                    FOREIGN KEY (nguoi_thuc_hien_id) REFERENCES tai_khoan(id),
                                    INDEX idx_don_ung_tuyen_ls (don_ung_tuyen_id),
                                    INDEX idx_nguoi_thuc_hien_ls (nguoi_thuc_hien_id),
                                    INDEX idx_thoi_gian_chuyen (thoi_gian_chuyen)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. lịch_phỏng_vấn
CREATE TABLE lich_phong_van (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                don_ung_tuyen_id BIGINT NOT NULL,
                                nguoi_phong_van_id BIGINT NOT NULL,
                                tieu_de_vong VARCHAR(150) NOT NULL,
                                thoi_gian_bat_dau TIMESTAMP NOT NULL,
                                thoi_gian_ket_thuc TIMESTAMP NOT NULL,
                                hinh_thuc ENUM('ONLINE', 'OFFLINE') NOT NULL,
                                dia_diem_hoac_link VARCHAR(255),
                                trang_thai_phong_van ENUM('CHO_PHONG_VAN', 'HOAN_THANH', 'HUY') DEFAULT 'CHO_PHONG_VAN',
                                ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                FOREIGN KEY (don_ung_tuyen_id) REFERENCES don_ung_tuyen(id) ON DELETE CASCADE,
                                FOREIGN KEY (nguoi_phong_van_id) REFERENCES nha_tuyen_dung(id),
                                INDEX idx_don_ung_tuyen_pv (don_ung_tuyen_id),
                                INDEX idx_nguoi_phong_van_pv (nguoi_phong_van_id),
                                INDEX idx_thoi_gian_bat_dau (thoi_gian_bat_dau)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;