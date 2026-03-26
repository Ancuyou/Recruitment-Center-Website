-- ================================================================
-- PHẦN 2: DỮ LIỆU MẪU (SEED DATA)
-- ================================================================

USE he_thong_tuyen_dung;

INSERT INTO ky_nang (ten_ky_nang) VALUES
                                      ('Java'), ('Spring Boot'), ('MySQL'), ('ReactJS'), ('NodeJS'),
                                      ('Python'), ('Docker'), ('Git'), ('AWS'), ('Giao tiếp');

INSERT INTO tai_khoan (email, mat_khau_hash, vai_tro) VALUES
                                                          ('ungvien1@email.com', '$2a$10$hashed_password_1', 'UNG_VIEN'),
                                                          ('ungvien2@email.com', '$2a$10$hashed_password_2', 'UNG_VIEN'),
                                                          ('hr1@fpt.com', '$2a$10$hashed_password_3', 'NHA_TUYEN_DUNG'),
                                                          ('admin@system.com', '$2a$10$hashed_password_4', 'ADMIN');

INSERT INTO cong_ty (ten_cong_ty, ma_so_thue, nganh_nghe, website, mo_ta) VALUES
                                                                              ('Công ty TNHH Phần mềm FPT', '0102345678', 'Công nghệ thông tin', 'https://fpt.com', 'Tập đoàn công nghệ hàng đầu Việt Nam'),
                                                                              ('Tập đoàn VNG', '0108765432', 'Internet', 'https://vng.com.vn', 'Công ty game và internet hàng đầu');

INSERT INTO ung_vien (tai_khoan_id, ho_ten, so_dien_thoai, gioi_tinh) VALUES
                                                                          (1, 'Nguyễn Văn An', '0901234567', 'NAM'),
                                                                          (2, 'Trần Thị Bình', '0909876543', 'NU');

INSERT INTO nha_tuyen_dung (tai_khoan_id, cong_ty_id, ho_ten, chuc_vu) VALUES
    (3, 1, 'Lê Văn Cường', 'Trưởng phòng Nhân sự');

INSERT INTO tin_tuyen_dung (nha_tuyen_dung_id, cong_ty_id, tieu_de, mo_ta_cong_viec, yeu_cau_ung_vien, muc_luong_min, muc_luong_max, dia_diem, cap_bac_yeu_cau, hinh_thuc_lam_viec, han_nop) VALUES
                                                                                                                                                                                                 (1, 1, 'Lập trình viên Java Backend', 'Phát triển hệ thống backend cho các sản phẩm công nghệ', 'Yêu cầu Java, Spring Boot, MySQL. Có kinh nghiệm làm việc nhóm', 15000000, 25000000, 'Hà Nội', 'JUNIOR', 'HYBRID', '2026-04-30'),
                                                                                                                                                                                                 (1, 1, 'Lập trình viên Frontend React', 'Phát triển giao diện người dùng cho ứng dụng web', 'Yêu cầu ReactJS, JavaScript, CSS. Ưu tiên có kinh nghiệm TypeScript', 12000000, 20000000, 'Hà Nội', 'FRESHER', 'OFFICE', '2026-05-15');

INSERT INTO chi_tiet_ky_nang_tin (tin_tuyen_dung_id, ky_nang_id, muc_yeu_cau) VALUES
                                                                                  (1, 1, 4),
                                                                                  (1, 2, 3),
                                                                                  (1, 3, 3),
                                                                                  (2, 4, 4),
                                                                                  (2, 8, 3);

INSERT INTO ho_so_cv (ung_vien_id, tieu_de_cv, muc_tieu_nghe_nghiep, la_cv_chinh) VALUES
                                                                                      (1, 'CV Java Backend 2026', 'Mong muốn trở thành Lập trình viên Backend chuyên nghiệp', TRUE),
                                                                                      (2, 'CV Frontend Developer', 'Mong muốn phát triển sự nghiệp trong lĩnh vực Frontend', TRUE);

INSERT INTO chi_tiet_ky_nang_cv (ho_so_cv_id, ky_nang_id, muc_thanh_thao) VALUES
                                                                              (1, 1, 4),
                                                                              (1, 2, 4),
                                                                              (1, 3, 5),
                                                                              (2, 4, 4),
                                                                              (2, 8, 4);

INSERT INTO chi_tiet_cv (ho_so_cv_id, loai_ban_ghi, ten_to_chuc, chuyen_nganh_hoac_vi_tri, ngay_bat_dau, ngay_ket_thuc, mo_ta_chi_tiet) VALUES
                                                                                                                                            (1, 1, 'Đại học Bách Khoa Hà Nội', 'Kỹ thuật phần mềm', '2020-09-01', '2024-06-30', 'GPA: 3.5/4.0'),
                                                                                                                                            (1, 2, 'Công ty TNHH Phần mềm FPT', 'Thực tập sinh Java', '2024-07-01', NULL, 'Thực tập sinh backend development'),
                                                                                                                                            (2, 1, 'Đại học Khoa học Tự nhiên', 'Công nghệ thông tin', '2021-09-01', '2025-06-30', 'GPA: 3.2/4.0'),
                                                                                                                                            (2, 2, 'Công ty StartUp ABC', 'Thực tập sinh Frontend', '2024-06-01', NULL, 'Phát triển giao diện ReactJS');

INSERT INTO don_ung_tuyen (tin_tuyen_dung_id, ho_so_cv_id, thu_ngo, trang_thai_hien_tai) VALUES
    (1, 1, 'Tôi rất mong muốn được làm việc tại quý công ty', 1);

INSERT INTO lich_su_trang_thai (don_ung_tuyen_id, nguoi_thuc_hien_id, trang_thai_moi, ghi_chu) VALUES
    (1, 1, 1, 'Ứng viên nộp đơn');