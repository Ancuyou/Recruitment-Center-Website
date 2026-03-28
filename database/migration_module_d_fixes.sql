-- =====================================================================
-- DATABASE MIGRATION - MODULE D
-- Date: 2026-03-28
-- Purpose: Fix race condition + expand LoaiThongBao enum
-- =====================================================================

-- =====================================================================
-- D1 FIX: Add UNIQUE constraint to prevent duplicate applications
-- =====================================================================
-- Issue: Race condition giữa check trùng và insert
-- Solution: Enforce UNIQUE constraint ở database level
-- Status: Race condition window → lỗi ở database → service handle gracefully

ALTER TABLE don_ung_tuyen
ADD CONSTRAINT uk_don_ung_tuyen_unique_application 
UNIQUE KEY (tin_tuyen_dung_id, tai_khoan_id);

-- Note: Nếu table đã có data test, có thể xảy ra constraint violation
-- → run sau khi xóa test data hoặc merge trên branch riêng

-- =====================================================================
-- D15 FIX: Expand LoaiThongBao enum (migration for existing data)
-- =====================================================================
-- Trước: UNG_TUYEN, PHONG_VAN, HE_THONG (3 giá trị)
-- Sau:   UNG_TUYEN_TAO_MOI, UNG_TUYEN_TU_CHOI, ..., HE_THONG (9 giá trị)

-- Step 1: Thêm các giá trị mới vào enum
ALTER TABLE thong_bao
MODIFY COLUMN loai_thong_bao 
ENUM(
    'UNG_TUYEN_TAO_MOI',
    'UNG_TUYEN_TU_CHOI',
    'UNG_TUYEN_VE_PHONG_VAN',
    'UNG_TUYEN_OFFER',
    'PHONG_VAN_TAO_MOI',
    'PHONG_VAN_CAP_NHAT',
    'PHONG_VAN_HUY',
    'PHONG_VAN_REMINDER',
    'HE_THONG'
) NOT NULL DEFAULT 'HE_THONG';

-- Step 2: Migrate existing data nếu cần
-- Nếu có hệ thống notification tự động, cần update:
-- - UNG_TUYEN → UNG_TUYEN_TAO_MOI (ứng viên mới nộp)
-- - PHONG_VAN → PHONG_VAN_TAO_MOI (HR tạo lịch mới)
-- - HE_THONG → HE_THONG (giữ nguyên)

UPDATE thong_bao 
SET loai_thong_bao = 'UNG_TUYEN_TAO_MOI' 
WHERE loai_thong_bao = 'UNG_TUYEN' AND loai_thong_bao IN (SELECT loai_thong_bao FROM thong_bao);

UPDATE thong_bao 
SET loai_thong_bao = 'PHONG_VAN_TAO_MOI' 
WHERE loai_thong_bao = 'PHONG_VAN' AND loai_thong_bao IN (SELECT loai_thong_bao FROM thong_bao);

-- =====================================================================
-- INDEX OPTIMIZATION
-- =====================================================================
-- Thêm index để tối ưu query getMyNotifications (D15)
CREATE INDEX idx_thong_bao_tai_khoan_ngay_tao 
ON thong_bao(tai_khoan_id, ngay_tao DESC);

-- Thêm index để tối ưu query getStatusHistory (D8)
CREATE INDEX idx_lich_su_trang_thai_don_thoi_gian 
ON lich_su_trang_thai(don_ung_tuyen_id, thoi_gian_chuyen DESC);

-- =====================================================================
-- VERIFICATION QUERIES (kiểm tra sau khi chạy migration)
-- =====================================================================
/*
-- Check UNIQUE constraint
SHOW KEYS FROM don_ung_tuyen WHERE Key_name = 'uk_don_ung_tuyen_unique_application';

-- Check enum values
SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'thong_bao' AND COLUMN_NAME = 'loai_thong_bao';

-- Check indexes
SHOW KEYS FROM thong_bao WHERE Key_name LIKE 'idx_%';
SHOW KEYS FROM lich_su_trang_thai WHERE Key_name LIKE 'idx_%';
*/

-- =====================================================================
-- ROLLBACK COMMANDS (nếu cần revert)
-- =====================================================================
/*
ALTER TABLE don_ung_tuyen
DROP CONSTRAINT uk_don_ung_tuyen_unique_application;

ALTER TABLE thong_bao
MODIFY COLUMN loai_thong_bao 
ENUM('UNG_TUYEN', 'PHONG_VAN', 'HE_THONG') NOT NULL DEFAULT 'HE_THONG';

DROP INDEX idx_thong_bao_tai_khoan_ngay_tao ON thong_bao;
DROP INDEX idx_lich_su_trang_thai_don_thoi_gian ON lich_su_trang_thai;
*/
