-- ================================================================
-- PHẦN 1: KHỞI TẠO DATABASE & CÁC BẢNG (DDL)
-- ================================================================

DROP DATABASE IF EXISTS he_thong_tuyen_dung;
CREATE DATABASE he_thong_tuyen_dung CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE he_thong_tuyen_dung;

-- 1) Tai khoan
CREATE TABLE tai_khoan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    mat_khau_hash VARCHAR(255) NOT NULL,
    vai_tro ENUM('UNG_VIEN', 'NHA_TUYEN_DUNG', 'ADMIN') NOT NULL,
    la_kich_hoat BOOLEAN DEFAULT TRUE,
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_vai_tro (vai_tro)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2) Cong ty
CREATE TABLE cong_ty (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ten_cong_ty VARCHAR(255) NOT NULL,
    ma_so_thue VARCHAR(50) UNIQUE,
    logo_url VARCHAR(255),
    website VARCHAR(255),
    mo_ta TEXT,
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_ma_so_thue (ma_so_thue)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3) Ung vien
CREATE TABLE ung_vien (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tai_khoan_id BIGINT NOT NULL UNIQUE,
    ho_ten VARCHAR(150) NOT NULL,
    so_dien_thoai VARCHAR(20) UNIQUE,
    ngay_sinh DATE,
    gioi_tinh ENUM('NAM', 'NU', 'KHAC'),
    anh_dai_dien VARCHAR(255),
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ung_vien_tai_khoan FOREIGN KEY (tai_khoan_id) REFERENCES tai_khoan(id) ON DELETE CASCADE,
    INDEX idx_so_dien_thoai (so_dien_thoai)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4) Nha tuyen dung
CREATE TABLE nha_tuyen_dung (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tai_khoan_id BIGINT NOT NULL UNIQUE,
    cong_ty_id BIGINT NOT NULL,
    ho_ten VARCHAR(150) NOT NULL,
    chuc_vu VARCHAR(100),
    so_dien_thoai VARCHAR(20),
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ntd_tai_khoan FOREIGN KEY (tai_khoan_id) REFERENCES tai_khoan(id) ON DELETE CASCADE,
    CONSTRAINT fk_ntd_cong_ty FOREIGN KEY (cong_ty_id) REFERENCES cong_ty(id),
    INDEX idx_nha_tuyen_dung_cong_ty (cong_ty_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5) Ky nang
CREATE TABLE ky_nang (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ten_ky_nang VARCHAR(100) NOT NULL UNIQUE,
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ten_ky_nang (ten_ky_nang)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6) Ho so CV
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
    CONSTRAINT fk_ho_so_cv_ung_vien FOREIGN KEY (ung_vien_id) REFERENCES ung_vien(id) ON DELETE CASCADE,
    INDEX idx_ung_vien_cv (ung_vien_id),
    INDEX idx_la_cv_chinh (la_cv_chinh),
    INDEX idx_ho_so_cv_da_xoa (da_xoa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7) Tin tuyen dung
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
    trang_thai ENUM('NHAP', 'MO', 'DONG') NOT NULL DEFAULT 'MO',
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_tin_tuyen_dung_ntd FOREIGN KEY (nha_tuyen_dung_id) REFERENCES nha_tuyen_dung(id),
    CONSTRAINT fk_tin_tuyen_dung_cong_ty FOREIGN KEY (cong_ty_id) REFERENCES cong_ty(id),
    INDEX idx_tin_nha_tuyen_dung (nha_tuyen_dung_id),
    INDEX idx_tin_cong_ty (cong_ty_id),
    INDEX idx_tin_trang_thai (trang_thai),
    INDEX idx_tin_han_nop (han_nop),
    INDEX idx_tin_cap_bac (cap_bac_yeu_cau),
    INDEX idx_tin_hinh_thuc (hinh_thuc_lam_viec)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8) Cong ty - nganh nghe (ElementCollection)
CREATE TABLE ct_cty_nganh (
    cong_ty_id BIGINT NOT NULL,
    nganh_nghe VARCHAR(50) NOT NULL,
    PRIMARY KEY (cong_ty_id, nganh_nghe),
    CONSTRAINT fk_ct_cty_nganh_cong_ty FOREIGN KEY (cong_ty_id) REFERENCES cong_ty(id) ON DELETE CASCADE,
    INDEX idx_nganh_nghe_cty (nganh_nghe)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9) Tin tuyen dung - khu vuc (ElementCollection)
CREATE TABLE ct_kv_tin (
    tin_tuyen_dung_id BIGINT NOT NULL,
    khu_vuc VARCHAR(30) NOT NULL,
    PRIMARY KEY (tin_tuyen_dung_id, khu_vuc),
    CONSTRAINT fk_ct_kv_tin_tin FOREIGN KEY (tin_tuyen_dung_id) REFERENCES tin_tuyen_dung(id) ON DELETE CASCADE,
    INDEX idx_khu_vuc_tin (khu_vuc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10) Chi tiet CV
CREATE TABLE chi_tiet_cv (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ho_so_cv_id BIGINT NOT NULL,
    loai_ban_ghi TINYINT NOT NULL COMMENT '1: HOC_VAN, 2: KINH_NGHIEM, 3: CHUNG_CHI',
    ten_to_chuc VARCHAR(200) NOT NULL,
    chuyen_nganh_hoac_vi_tri VARCHAR(200),
    ngay_bat_dau DATE NOT NULL,
    ngay_ket_thuc DATE,
    mo_ta_chi_tiet TEXT,
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chi_tiet_cv_ho_so_cv FOREIGN KEY (ho_so_cv_id) REFERENCES ho_so_cv(id) ON DELETE CASCADE,
    INDEX idx_chi_tiet_cv_ho_so (ho_so_cv_id),
    INDEX idx_chi_tiet_cv_loai (loai_ban_ghi)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11) Chi tiet ky nang CV (EmbeddedId)
CREATE TABLE chi_tiet_ky_nang_cv (
    ho_so_cv_id BIGINT NOT NULL,
    ky_nang_id BIGINT NOT NULL,
    muc_thanh_thao TINYINT NOT NULL CHECK (muc_thanh_thao BETWEEN 1 AND 5),
    mo_ta VARCHAR(500),
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat BIGINT,
    da_xoa BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (ho_so_cv_id, ky_nang_id),
    CONSTRAINT fk_ctkncv_ho_so_cv FOREIGN KEY (ho_so_cv_id) REFERENCES ho_so_cv(id) ON DELETE CASCADE,
    CONSTRAINT fk_ctkncv_ky_nang FOREIGN KEY (ky_nang_id) REFERENCES ky_nang(id) ON DELETE CASCADE,
    INDEX idx_ctkncv_ky_nang (ky_nang_id),
    INDEX idx_ctkncv_da_xoa (da_xoa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12) Chi tiet ky nang tin (entity table name/column names follow current mapping)
CREATE TABLE ct_ky_nang_tin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tin_tuyendung_id BIGINT NOT NULL,
    ky_nang_id BIGINT NOT NULL,
    yeucau TINYINT NOT NULL CHECK (yeucau BETWEEN 1 AND 5),
    mo_ta VARCHAR(500),
    ngay_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    da_xoa BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_ctknt_tin FOREIGN KEY (tin_tuyendung_id) REFERENCES tin_tuyen_dung(id) ON DELETE CASCADE,
    CONSTRAINT fk_ctknt_ky_nang FOREIGN KEY (ky_nang_id) REFERENCES ky_nang(id) ON DELETE CASCADE,
    CONSTRAINT uk_ctknt_tin_skill UNIQUE KEY (tin_tuyendung_id, ky_nang_id),
    INDEX idx_tin_tuyendung_id (tin_tuyendung_id),
    INDEX idx_ky_nang_id (ky_nang_id),
    INDEX idx_ctknt_da_xoa (da_xoa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13) Don ung tuyen
CREATE TABLE don_ung_tuyen (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tin_tuyen_dung_id BIGINT NOT NULL,
    ho_so_cv_id BIGINT NOT NULL,
    ban_sao_cv_url VARCHAR(255),
    thu_ngo TEXT,
    trang_thai_hien_tai ENUM('MOI', 'REVIEW', 'PHONG_VAN', 'OFFER', 'TU_CHOI') NOT NULL DEFAULT 'MOI',
    ngay_nop TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_don_tin FOREIGN KEY (tin_tuyen_dung_id) REFERENCES tin_tuyen_dung(id),
    CONSTRAINT fk_don_cv FOREIGN KEY (ho_so_cv_id) REFERENCES ho_so_cv(id),
    CONSTRAINT uk_don_ung_tuyen_unique_application UNIQUE KEY (tin_tuyen_dung_id, ho_so_cv_id),
    INDEX idx_don_tin (tin_tuyen_dung_id),
    INDEX idx_don_cv (ho_so_cv_id),
    INDEX idx_don_trang_thai (trang_thai_hien_tai),
    INDEX idx_don_ngay_nop (ngay_nop)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14) Lich su trang thai
CREATE TABLE lich_su_trang_thai (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    don_ung_tuyen_id BIGINT NOT NULL,
    nguoi_thuc_hien_id BIGINT NOT NULL,
    trang_thai_cu TINYINT,
    trang_thai_moi TINYINT NOT NULL,
    ghi_chu TEXT,
    thoi_gian_chuyen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lstt_don FOREIGN KEY (don_ung_tuyen_id) REFERENCES don_ung_tuyen(id) ON DELETE CASCADE,
    CONSTRAINT fk_lstt_nguoi_thuc_hien FOREIGN KEY (nguoi_thuc_hien_id) REFERENCES tai_khoan(id),
    INDEX idx_lstt_don (don_ung_tuyen_id),
    INDEX idx_lstt_nguoi_thuc_hien (nguoi_thuc_hien_id),
    INDEX idx_lstt_thoi_gian (thoi_gian_chuyen)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15) Lich phong van
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
    CONSTRAINT fk_lpv_don FOREIGN KEY (don_ung_tuyen_id) REFERENCES don_ung_tuyen(id) ON DELETE CASCADE,
    CONSTRAINT fk_lpv_nguoi_phong_van FOREIGN KEY (nguoi_phong_van_id) REFERENCES nha_tuyen_dung(id),
    INDEX idx_lpv_don (don_ung_tuyen_id),
    INDEX idx_lpv_nguoi_phong_van (nguoi_phong_van_id),
    INDEX idx_lpv_thoi_gian_bat_dau (thoi_gian_bat_dau)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16) Thong bao
CREATE TABLE thong_bao (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tai_khoan_id BIGINT NOT NULL,
    tieu_de VARCHAR(255) NOT NULL,
    noi_dung TEXT NOT NULL,
    loai_thong_bao ENUM(
        'UNG_TUYEN_TAO_MOI',
        'UNG_TUYEN_TU_CHOI',
        'UNG_TUYEN_VE_PHONG_VAN',
        'UNG_TUYEN_OFFER',
        'PHONG_VAN_TAO_MOI',
        'PHONG_VAN_CAP_NHAT',
        'PHONG_VAN_HUY',
        'PHONG_VAN_REMINDER',
        'HE_THONG'
    ) NOT NULL DEFAULT 'HE_THONG',
    lien_ket VARCHAR(255),
    da_doc BOOLEAN DEFAULT FALSE,
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_thong_bao_tai_khoan FOREIGN KEY (tai_khoan_id) REFERENCES tai_khoan(id) ON DELETE CASCADE,
    INDEX idx_tai_khoan_tb (tai_khoan_id),
    INDEX idx_da_doc (da_doc),
    INDEX idx_tb_ngay_tao (ngay_tao)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17) Pending registration
CREATE TABLE pending_registration (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(255) NOT NULL UNIQUE,
    tai_khoan_id BIGINT NOT NULL UNIQUE,
    ngay_het_han DATETIME NOT NULL,
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pending_registration_tai_khoan FOREIGN KEY (tai_khoan_id) REFERENCES tai_khoan(id) ON DELETE CASCADE,
    INDEX idx_pending_registration_token (token),
    INDEX idx_pending_registration_ngay_het_han (ngay_het_han)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 18) Pending password reset
CREATE TABLE pending_password_reset (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(255) NOT NULL UNIQUE,
    tai_khoan_id BIGINT NOT NULL UNIQUE,
    ngay_het_han DATETIME NOT NULL,
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pending_password_reset_tai_khoan FOREIGN KEY (tai_khoan_id) REFERENCES tai_khoan(id) ON DELETE CASCADE,
    INDEX idx_pending_password_reset_token (token),
    INDEX idx_pending_password_reset_ngay_het_han (ngay_het_han)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;