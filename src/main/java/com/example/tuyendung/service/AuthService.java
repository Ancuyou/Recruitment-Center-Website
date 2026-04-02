package com.example.tuyendung.service;

import com.example.tuyendung.dto.request.DangKyUngVienRequest;
import com.example.tuyendung.dto.request.DangKyNhaTuyenDungRequest;
import com.example.tuyendung.dto.request.DangNhapRequest;
import com.example.tuyendung.dto.response.AuthResponse;
import com.example.tuyendung.dto.response.UserInfoResponse;

public interface AuthService {

    AuthResponse dangNhap(DangNhapRequest request);

    AuthResponse refreshToken(String refreshToken);

    void logout(String token);
}