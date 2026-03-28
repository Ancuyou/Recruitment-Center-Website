package com.example.tuyendung.controller;

import com.example.tuyendung.common.ApiResponse;
import com.example.tuyendung.dto.request.DangKyUngVienRequest;
import com.example.tuyendung.dto.request.DangKyNhaTuyenDungRequest;
import com.example.tuyendung.dto.request.DangNhapRequest;
import com.example.tuyendung.dto.request.ForgotPasswordRequest;
import com.example.tuyendung.dto.request.ResetPasswordRequest;
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

    // A4: Đăng xuất
    @PostMapping("/dang-xuat")
    public ResponseEntity<ApiResponse<Void>> dangXuat(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.replace("Bearer ", "");
            authService.logout(token);
        }
        return ResponseEntity.ok(ApiResponse.success("Đã đăng xuất thành công", null));
    }

    // A6: Quên mật khẩu
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Hướng dẫn đặt lại mật khẩu đã được gửi đến email", null));
    }

    // A7: Đặt lại mật khẩu
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công", null));
    }

    // A8: Xác thực email
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Xác thực email thành công, bạn có thể đăng nhập.", null));
    }
}