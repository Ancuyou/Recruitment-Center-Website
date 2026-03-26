package com.example.tuyendung.controller;

import com.example.tuyendung.common.ApiResponse;
import com.example.tuyendung.dto.request.DangKyUngVienRequest;
import com.example.tuyendung.dto.request.DangKyNhaTuyenDungRequest;
import com.example.tuyendung.dto.request.DangNhapRequest;
import com.example.tuyendung.dto.response.AuthResponse;
import com.example.tuyendung.dto.response.UserInfoResponse;
import com.example.tuyendung.security.JwtTokenProvider;
import com.example.tuyendung.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/dang-ky-ung-vien")
    public ResponseEntity<ApiResponse<AuthResponse>> dangKyUngVien(
            @Valid @RequestBody DangKyUngVienRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Đăng ký thành công", authService.dangKyUngVien(request)));
    }

    @PostMapping("/dang-ky-nha-tuyen-dung")
    public ResponseEntity<ApiResponse<AuthResponse>> dangKyNhaTuyenDung(
            @Valid @RequestBody DangKyNhaTuyenDungRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Đăng ký thành công", authService.dangKyNhaTuyenDung(request)));
    }

    @PostMapping("/dang-nhap")
    public ResponseEntity<ApiResponse<AuthResponse>> dangNhap(
            @Valid @RequestBody DangNhapRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", authService.dangNhap(request)));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestParam String refreshToken) {
        return ResponseEntity.ok(ApiResponse.success(authService.refreshToken(refreshToken)));
    }

    @GetMapping("/thong-tin/{taiKhoanId}")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getThongTinUser(
            @PathVariable Long taiKhoanId) {
        return ResponseEntity.ok(ApiResponse.success(authService.getThongTinUser(taiKhoanId)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getThongTinCaNhan(
            @RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long taiKhoanId = jwtTokenProvider.getUserIdFromToken(token);
        return ResponseEntity.ok(ApiResponse.success(authService.getThongTinUser(taiKhoanId)));
    }
}