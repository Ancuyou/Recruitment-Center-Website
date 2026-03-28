-- ================================================================
-- MIGRATION MODULE B (Simplified — no nganh_nghe / khu_vuc tables)
-- Ngành nghề và khu vực dùng enum, lưu string trong junction tables đơn giản
-- ================================================================

USE he_thong_tuyen_dung;

-- 1. ct_cty_nganh: CongTy ↔ NganhNgheEnum (string)
CREATE TABLE IF NOT EXISTS ct_cty_nganh (
    cong_ty_id  BIGINT NOT NULL,
    nganh_nghe  VARCHAR(50) NOT NULL,
    PRIMARY KEY (cong_ty_id, nganh_nghe),
    FOREIGN KEY (cong_ty_id) REFERENCES cong_ty(id) ON DELETE CASCADE,
    INDEX idx_nganh_nghe_cty (nganh_nghe)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. ct_kv_tin: TinTuyenDung ↔ KhuVucEnum (string)
CREATE TABLE IF NOT EXISTS ct_kv_tin (
    tin_tuyen_dung_id   BIGINT NOT NULL,
    khu_vuc             VARCHAR(30) NOT NULL,
    PRIMARY KEY (tin_tuyen_dung_id, khu_vuc),
    FOREIGN KEY (tin_tuyen_dung_id) REFERENCES tin_tuyen_dung(id) ON DELETE CASCADE,
    INDEX idx_khu_vuc_tin (khu_vuc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Không cần bảng nganh_nghe, khu_vuc — dùng enum Java
