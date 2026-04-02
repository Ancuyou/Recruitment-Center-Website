package com.example.tuyendung.service;

import com.example.tuyendung.dto.request.DangKyNhaTuyenDungRequest;
import com.example.tuyendung.dto.request.DangKyUngVienRequest;
import com.example.tuyendung.dto.response.AuthResponse;

/**
 * Service xử lý quy trình đăng ký ứng viên và nhà tuyển dụng (SRP Refactoring - H4)
 */
public interface RegistrationService {
    
    /**
     * Đăng ký tài khoản cho ứng viên mới
     */
    AuthResponse dangKyUngVien(DangKyUngVienRequest request);

    /**
     * Đăng ký tài khoản cho nhà tuyển dụng mới (kèm theo thông tin công ty)
     */
    AuthResponse dangKyNhaTuyenDung(DangKyNhaTuyenDungRequest request);
}
