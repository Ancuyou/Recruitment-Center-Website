-- ================================================================
-- PHẦN 3: VIEWS, PROCEDURES & TRIGGERS
-- ================================================================

USE he_thong_tuyen_dung;

-- Recreate objects safely
DROP VIEW IF EXISTS v_matching_ung_vien_voi_cong_viec;
DROP PROCEDURE IF EXISTS sp_tao_don_ung_tuyen;
DROP TRIGGER IF EXISTS trg_cap_nhat_trang_thai_don;

-- 1) View: matching ung vien voi cong viec theo ky nang
CREATE VIEW v_matching_ung_vien_voi_cong_viec AS
SELECT
    uv.id AS ung_vien_id,
    uv.ho_ten,
    uv.so_dien_thoai,
    tt.id AS tin_tuyen_dung_id,
    tt.tieu_de AS tieu_de_tin,
    tt.cap_bac_yeu_cau,
    tt.hinh_thuc_lam_viec,
    COUNT(DISTINCT ctcv.ky_nang_id) AS so_ky_nang_khop,
    AVG(ctcv.muc_thanh_thao) AS diem_thanh_thao_tb,
    GROUP_CONCAT(DISTINCT kn.ten_ky_nang ORDER BY kn.ten_ky_nang SEPARATOR ', ') AS ten_cac_ky_nang_khop
FROM ung_vien uv
JOIN ho_so_cv hscv ON uv.id = hscv.ung_vien_id
JOIN chi_tiet_ky_nang_cv ctcv ON hscv.id = ctcv.ho_so_cv_id
JOIN ct_ky_nang_tin ctkt ON ctcv.ky_nang_id = ctkt.ky_nang_id
JOIN tin_tuyen_dung tt ON ctkt.tin_tuyendung_id = tt.id
JOIN ky_nang kn ON ctcv.ky_nang_id = kn.id
WHERE hscv.da_xoa = FALSE
  AND ctcv.da_xoa = FALSE
  AND ctkt.da_xoa = FALSE
  AND ctcv.muc_thanh_thao >= ctkt.yeucau
  AND tt.trang_thai = 'MO'
GROUP BY uv.id, tt.id
HAVING so_ky_nang_khop >= 1
ORDER BY so_ky_nang_khop DESC, diem_thanh_thao_tb DESC;

-- 2) Stored procedure: tao don ung tuyen + ghi lich su
DELIMITER $$
CREATE PROCEDURE sp_tao_don_ung_tuyen(
    IN p_tin_tuyen_dung_id BIGINT,
    IN p_ho_so_cv_id BIGINT,
    IN p_thu_ngo TEXT,
    IN p_ban_sao_cv_url VARCHAR(255),
    IN p_nguoi_thuc_hien_id BIGINT
)
BEGIN
    DECLARE v_don_ung_tuyen_id BIGINT;

    INSERT INTO don_ung_tuyen (tin_tuyen_dung_id, ho_so_cv_id, thu_ngo, ban_sao_cv_url, trang_thai_hien_tai)
    VALUES (p_tin_tuyen_dung_id, p_ho_so_cv_id, p_thu_ngo, p_ban_sao_cv_url, 'MOI');

    SET v_don_ung_tuyen_id = LAST_INSERT_ID();

    INSERT INTO lich_su_trang_thai (don_ung_tuyen_id, nguoi_thuc_hien_id, trang_thai_cu, trang_thai_moi, ghi_chu)
    VALUES (v_don_ung_tuyen_id, p_nguoi_thuc_hien_id, NULL, 1, 'Ung vien nop don');

    SELECT v_don_ung_tuyen_id AS don_ung_tuyen_id;
END$$
DELIMITER ;

-- 3) Trigger: log khi thay doi trang thai don
DELIMITER $$
CREATE TRIGGER trg_cap_nhat_trang_thai_don
AFTER UPDATE ON don_ung_tuyen
FOR EACH ROW
BEGIN
    IF OLD.trang_thai_hien_tai <> NEW.trang_thai_hien_tai THEN
        INSERT INTO lich_su_trang_thai (
            don_ung_tuyen_id,
            nguoi_thuc_hien_id,
            trang_thai_cu,
            trang_thai_moi,
            ghi_chu
        ) VALUES (
            NEW.id,
            1,
            CASE OLD.trang_thai_hien_tai
                WHEN 'MOI' THEN 1
                WHEN 'REVIEW' THEN 2
                WHEN 'PHONG_VAN' THEN 3
                WHEN 'OFFER' THEN 4
                WHEN 'TU_CHOI' THEN 5
                ELSE NULL
            END,
            CASE NEW.trang_thai_hien_tai
                WHEN 'MOI' THEN 1
                WHEN 'REVIEW' THEN 2
                WHEN 'PHONG_VAN' THEN 3
                WHEN 'OFFER' THEN 4
                WHEN 'TU_CHOI' THEN 5
                ELSE NULL
            END,
            'Cap nhat trang thai tu dong'
        );
    END IF;
END$$

DELIMITER ;