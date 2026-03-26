-- ================================================================
-- PHẦN 3: VIEWS, PROCEDURES & TRIGGERS
-- ================================================================

USE he_thong_tuyen_dung;

-- 1. VIEW: MATCHING ỨNG VIÊN VỚI CÔNG VIỆC
CREATE OR REPLACE VIEW v_matching_ung_vien_voi_cong_viec AS
SELECT
    uv.id AS ung_vien_id,
    uv.ho_ten,
    uv.so_dien_thoai,
    tt.id AS tin_tuyen_dung_id,
    tt.tieu_de AS tieu_de_tin,
    tt.cap_bac_yeu_cau,
    tt.hinh_thuc_lam_viec,
    COUNT(ctcv.ky_nang_id) AS so_ky_nang_khop,
    AVG(ctcv.muc_thanh_thao) AS diem_thanh_thao_tb,
    GROUP_CONCAT(kn.ten_ky_nang SEPARATOR ', ') AS ten_cac_ky_nang_khop
FROM ung_vien uv
JOIN ho_so_cv hscv ON uv.id = hscv.ung_vien_id
JOIN chi_tiet_ky_nang_cv ctcv ON hscv.id = ctcv.ho_so_cv_id
JOIN chi_tiet_ky_nang_tin ctct ON ctcv.ky_nang_id = ctct.ky_nang_id
JOIN tin_tuyen_dung tt ON ctct.tin_tuyen_dung_id = tt.id
JOIN ky_nang kn ON ctcv.ky_nang_id = kn.id
WHERE ctcv.muc_thanh_thao >= ctct.muc_yeu_cau
  AND tt.trang_thai = 1
  AND hscv.da_xoa = FALSE
GROUP BY uv.id, tt.id
HAVING so_ky_nang_khop >= 2
ORDER BY so_ky_nang_khop DESC, diem_thanh_thao_tb DESC;

-- 2. STORED PROCEDURE: TẠO ĐƠN ỨNG TUYỂN
DELIMITER $$

CREATE PROCEDURE sp_tao_don_ung_tuyen(
    IN p_tin_tuyen_dung_id BIGINT,
    IN p_ho_so_cv_id BIGINT,
    IN p_thu_ngo TEXT,
    IN p_ban_sao_cv_url VARCHAR(255)
)
BEGIN
    DECLARE v_don_ung_tuyen_id BIGINT;

    -- Tạo đơn ứng tuyển
    INSERT INTO don_ung_tuyen (tin_tuyen_dung_id, ho_so_cv_id, thu_ngo, ban_sao_cv_url)
    VALUES (p_tin_tuyen_dung_id, p_ho_so_cv_id, p_thu_ngo, p_ban_sao_cv_url);

    SET v_don_ung_tuyen_id = LAST_INSERT_ID();

    -- Ghi log trạng thái
    INSERT INTO lich_su_trang_thai (don_ung_tuyen_id, nguoi_thuc_hien_id, trang_thai_moi, ghi_chu)
    VALUES (v_don_ung_tuyen_id, 1, 1, 'Ứng viên nộp đơn');

    SELECT v_don_ung_tuyen_id AS don_ung_tuyen_id;
END$$

DELIMITER ;

-- 3. TRIGGER: TỰ ĐỘNG GHI LOG KHI THAY ĐỔI TRẠNG THÁI ĐƠN
DELIMITER $$

CREATE TRIGGER trg_cap_nhat_trang_thai_don
AFTER UPDATE ON don_ung_tuyen
FOR EACH ROW
BEGIN
    IF OLD.trang_thai_hien_tai != NEW.trang_thai_hien_tai THEN
        INSERT INTO lich_su_trang_thai (don_ung_tuyen_id, nguoi_thuc_hien_id, trang_thai_cu, trang_thai_moi, ghi_chu)
        VALUES (NEW.id, 1, OLD.trang_thai_hien_tai, NEW.trang_thai_hien_tai, 'Cập nhật trạng thái tự động');
    END IF;
END$$

DELIMITER ;