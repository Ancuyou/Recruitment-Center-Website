-- ================================================================
-- PHẦN 2: DỮ LIỆU MẪU (SEED DATA)
-- ================================================================

USE he_thong_tuyen_dung;

-- 1) Tai khoan
-- Mat khau test cho cac tai khoan seed: 123
INSERT INTO tai_khoan (email, mat_khau_hash, vai_tro, la_kich_hoat) VALUES
('admin@system.local', '$2a$10$QeibVBculcttqVKJH3oArucqQ8gU0Z6hyOIW9OCCHwPTqwod0rBCW', 'ADMIN', TRUE),
('hr1@fsoft.local', '$2a$10$QeibVBculcttqVKJH3oArucqQ8gU0Z6hyOIW9OCCHwPTqwod0rBCW', 'NHA_TUYEN_DUNG', TRUE),
('hr2@vng.local', '$2a$10$QeibVBculcttqVKJH3oArucqQ8gU0Z6hyOIW9OCCHwPTqwod0rBCW', 'NHA_TUYEN_DUNG', TRUE),
('candidate1@mail.local', '$2a$10$QeibVBculcttqVKJH3oArucqQ8gU0Z6hyOIW9OCCHwPTqwod0rBCW', 'UNG_VIEN', TRUE),
('candidate2@mail.local', '$2a$10$QeibVBculcttqVKJH3oArucqQ8gU0Z6hyOIW9OCCHwPTqwod0rBCW', 'UNG_VIEN', TRUE);

-- 2) Cong ty
INSERT INTO cong_ty (ten_cong_ty, ma_so_thue, logo_url, website, mo_ta) VALUES
('FPT Software', '0101234567', 'https://cdn.local/logo-fpt.png', 'https://fptsoftware.com', 'Cong ty cong nghe thong tin.'),
('VNG Corporation', '0107654321', 'https://cdn.local/logo-vng.png', 'https://vng.com.vn', 'Cong ty san pham internet va game.');

INSERT INTO ct_cty_nganh (cong_ty_id, nganh_nghe) VALUES
(1, 'CONG_NGHE_THONG_TIN'),
(1, 'GIAO_DUC_DAO_TAO'),
(2, 'CONG_NGHE_THONG_TIN'),
(2, 'MARKETING_TRUYEN_THONG');

-- 3) Ung vien
INSERT INTO ung_vien (tai_khoan_id, ho_ten, so_dien_thoai, ngay_sinh, gioi_tinh, anh_dai_dien) VALUES
(4, 'Nguyen Van An', '0901000001', '2001-03-12', 'NAM', 'https://cdn.local/avatar-an.jpg'),
(5, 'Tran Thi Binh', '0901000002', '2002-07-21', 'NU', 'https://cdn.local/avatar-binh.jpg');

-- 4) Nha tuyen dung
INSERT INTO nha_tuyen_dung (tai_khoan_id, cong_ty_id, ho_ten, chuc_vu, so_dien_thoai) VALUES
(2, 1, 'Le Van Cuong', 'HR Manager', '0902000001'),
(3, 2, 'Pham Minh Chau', 'Talent Acquisition', '0902000002');

-- 5) Ky nang
INSERT INTO ky_nang (ten_ky_nang) VALUES
('Java'),
('Spring Boot'),
('MySQL'),
('ReactJS'),
('TypeScript'),
('Docker'),
('Git');

-- 6) Ho so CV
INSERT INTO ho_so_cv (ung_vien_id, tieu_de_cv, muc_tieu_nghe_nghiep, file_cv_url, la_cv_chinh, da_xoa) VALUES
(1, 'Backend Java CV', 'Tro thanh backend engineer.', 'http://localhost:8080/uploads/cv-files/bb0f47d0-db7c-4115-84ca-e979df0bc648-1775568096828.pdf', TRUE, FALSE),
(2, 'Frontend React CV', 'Theo duoi frontend web app.', 'http://localhost:8080/uploads/cv-files/bb0f47d0-db7c-4115-84ca-e979df0bc648-1775568096828.pdf', TRUE, FALSE);

-- 7) Tin tuyen dung
INSERT INTO tin_tuyen_dung (
    nha_tuyen_dung_id, cong_ty_id, tieu_de, mo_ta_cong_viec, yeu_cau_ung_vien,
    muc_luong_min, muc_luong_max, dia_diem, cap_bac_yeu_cau, hinh_thuc_lam_viec,
    han_nop, trang_thai
) VALUES
(1, 1, 'Java Backend Developer', 'Xay dung REST API va xu ly du lieu.', '2+ nam Java, Spring Boot, SQL.', 15000000, 30000000, 'Ha Noi', 'JUNIOR', 'HYBRID', '2026-12-31', 'MO'),
(2, 2, 'Frontend React Developer', 'Phat trien UI cho ung dung web.', 'Kinh nghiem ReactJS, TypeScript.', 12000000, 25000000, 'Ho Chi Minh', 'FRESHER', 'OFFICE', '2026-11-30', 'MO');

INSERT INTO ct_kv_tin (tin_tuyen_dung_id, khu_vuc) VALUES
(1, 'HA_NOI'),
(1, 'REMOTE'),
(2, 'HO_CHI_MINH');

-- 8) Chi tiet ky nang CV
INSERT INTO chi_tiet_ky_nang_cv (ho_so_cv_id, ky_nang_id, muc_thanh_thao, mo_ta, ngay_cap_nhat, da_xoa) VALUES
(1, 1, 4, 'Dung Java backend tot.', UNIX_TIMESTAMP() * 1000, FALSE),
(1, 2, 4, 'Su dung Spring Boot hang ngay.', UNIX_TIMESTAMP() * 1000, FALSE),
(1, 3, 3, 'SQL va tuning co ban.', UNIX_TIMESTAMP() * 1000, FALSE),
(2, 4, 4, 'Phat trien React production.', UNIX_TIMESTAMP() * 1000, FALSE),
(2, 5, 3, 'TypeScript muc kha.', UNIX_TIMESTAMP() * 1000, FALSE);

-- 9) Chi tiet ky nang tin
INSERT INTO ct_ky_nang_tin (tin_tuyendung_id, ky_nang_id, yeucau, mo_ta, da_xoa) VALUES
(1, 1, 4, 'Java core va OOP.', FALSE),
(1, 2, 3, 'Kinh nghiem Spring Boot.', FALSE),
(1, 3, 3, 'MySQL co ban den trung binh.', FALSE),
(2, 4, 4, 'ReactJS thanh thao.', FALSE),
(2, 5, 3, 'Viet code TypeScript ro rang.', FALSE);

-- 10) Chi tiet CV
INSERT INTO chi_tiet_cv (
    ho_so_cv_id, loai_ban_ghi, ten_to_chuc, chuyen_nganh_hoac_vi_tri,
    ngay_bat_dau, ngay_ket_thuc, mo_ta_chi_tiet
) VALUES
(1, 1, 'Dai hoc Bach Khoa Ha Noi', 'Ky thuat phan mem', '2019-09-01', '2023-06-30', 'GPA 3.4/4.0'),
(1, 2, 'ABC Software', 'Java Intern', '2023-07-01', '2024-06-01', 'Lam viec voi Spring Boot va MySQL'),
(2, 1, 'Dai hoc KHTN', 'Cong nghe thong tin', '2020-09-01', '2024-06-30', 'Tham gia du an front-end'),
(2, 2, 'XYZ Digital', 'Frontend Intern', '2024-07-01', NULL, 'ReactJS + TypeScript');

-- 11) Don ung tuyen + lich su + phong van
INSERT INTO don_ung_tuyen (tin_tuyen_dung_id, ho_so_cv_id, ban_sao_cv_url, thu_ngo, trang_thai_hien_tai) VALUES
(1, 1, 'http://localhost:8080/uploads/cv-files/bb0f47d0-db7c-4115-84ca-e979df0bc648-1775568096828.pdf', 'Toi mong muon dong hanh cung cong ty.', 'REVIEW'),
(2, 2, 'http://localhost:8080/uploads/cv-files/bb0f47d0-db7c-4115-84ca-e979df0bc648-1775568096828.pdf', 'Toi rat quan tam vi tri nay.', 'MOI');

INSERT INTO lich_su_trang_thai (don_ung_tuyen_id, nguoi_thuc_hien_id, trang_thai_cu, trang_thai_moi, ghi_chu) VALUES
(1, 2, 1, 2, 'HR da chuyen don sang REVIEW'),
(2, 3, NULL, 1, 'Ung vien vua nop don');

INSERT INTO lich_phong_van (
    don_ung_tuyen_id, nguoi_phong_van_id, tieu_de_vong,
    thoi_gian_bat_dau, thoi_gian_ket_thuc, hinh_thuc,
    dia_diem_hoac_link, trang_thai_phong_van
) VALUES
(1, 1, 'Vong 1 - Technical Interview', '2026-08-15 09:00:00', '2026-08-15 10:00:00', 'ONLINE', 'https://meet.local/room-001', 'CHO_PHONG_VAN');

-- 12) Thong bao
INSERT INTO thong_bao (tai_khoan_id, tieu_de, noi_dung, loai_thong_bao, lien_ket, da_doc) VALUES
(4, 'Don da duoc tiep nhan', 'Don ung tuyen cua ban da duoc tiep nhan.', 'UNG_TUYEN_TAO_MOI', '/candidate/applications/1', FALSE),
(2, 'Co don ung tuyen moi', 'Ban vua nhan duoc mot don ung tuyen.', 'UNG_TUYEN_TAO_MOI', '/recruiter/applications/1', FALSE),
(4, 'Lich phong van moi', 'Ban co lich phong van moi.', 'PHONG_VAN_TAO_MOI', '/candidate/interviews', FALSE);

-- 13) Pending tokens
INSERT INTO pending_registration (token, tai_khoan_id, ngay_het_han) VALUES
('verify_token_candidate2', 5, DATE_ADD(NOW(), INTERVAL 1 DAY));

INSERT INTO pending_password_reset (token, tai_khoan_id, ngay_het_han) VALUES
('reset_token_hr1', 2, DATE_ADD(NOW(), INTERVAL 2 HOUR));
