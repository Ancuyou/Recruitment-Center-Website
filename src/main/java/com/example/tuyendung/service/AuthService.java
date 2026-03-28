package com.example.tuyendung.service;

import com.example.tuyendung.dto.request.DangKyUngVienRequest;
import com.example.tuyendung.dto.request.DangKyNhaTuyenDungRequest;
import com.example.tuyendung.dto.request.DangNhapRequest;
import com.example.tuyendung.dto.response.AuthResponse;
import com.example.tuyendung.dto.response.UserInfoResponse;

public interface AuthService {

    AuthResponse dangKyUngVien(DangKyUngVienRequest request);

    AuthResponse dangKyNhaTuyenDung(DangKyNhaTuyenDungRequest request);

    AuthResponse dangNhap(DangNhapRequest request);

    AuthResponse refreshToken(String refreshToken);

    UserInfoResponse getThongTinUser(Long taiKhoanId);

    void logout(String token);

    void forgotPassword(com.example.tuyendung.dto.request.ForgotPasswordRequest request);

    void resetPassword(com.example.tuyendung.dto.request.ResetPasswordRequest request);

    void verifyEmail(String token);
}