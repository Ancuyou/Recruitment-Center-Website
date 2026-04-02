package com.example.tuyendung.controller;

import com.example.tuyendung.common.ApiResponse;
import com.example.tuyendung.common.Constants;
import com.example.tuyendung.dto.request.DangKyUngVienRequest;
import com.example.tuyendung.dto.request.DangKyNhaTuyenDungRequest;
import com.example.tuyendung.dto.request.DangNhapRequest;
import com.example.tuyendung.dto.request.ForgotPasswordRequest;
import com.example.tuyendung.dto.request.ResetPasswordRequest;
import com.example.tuyendung.dto.response.AuthResponse;
import com.example.tuyendung.dto.response.UserInfoResponse;
import com.example.tuyendung.security.CustomUserDetails;
import com.example.tuyendung.service.AccountWorkflowService;
import com.example.tuyendung.service.AuthService;
import com.example.tuyendung.service.RegistrationService;
import com.example.tuyendung.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller cho xác thực người dùng (Module A)
 *
 * SOLID:
 * - SRP: Chỉ xử lý auth endpoints, delegate logic xuống AuthService
 * - DIP: Phụ thuộc AuthService interface, không phụ thuộc impl
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RegistrationService registrationService;
    private final AccountWorkflowService accountWorkflowService;
    private final UserService userService;

    /** A1: Đăng ký ứng viên */
    @PostMapping("/dang-ky-ung-vien")
    public ResponseEntity<ApiResponse<AuthResponse>> dangKyUngVien(
            @Valid @RequestBody DangKyUngVienRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản.",
                registrationService.dangKyUngVien(request)));
    }

    /** A2: Đăng ký nhà tuyển dụng */
    @PostMapping("/dang-ky-nha-tuyen-dung")
    public ResponseEntity<ApiResponse<AuthResponse>> dangKyNhaTuyenDung(
            @Valid @RequestBody DangKyNhaTuyenDungRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản.",
                registrationService.dangKyNhaTuyenDung(request)));
    }

    /** A3: Đăng nhập */
    @PostMapping("/dang-nhap")
    public ResponseEntity<ApiResponse<AuthResponse>> dangNhap(
            @Valid @RequestBody DangNhapRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", authService.dangNhap(request)));
    }

    /** A5: Refresh token */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestParam String refreshToken) {
        return ResponseEntity.ok(ApiResponse.success(authService.refreshToken(refreshToken)));
    }

    /**
     * Lấy thông tin theo ID — yêu cầu ADMIN để tránh IDOR
     * (endpoint này không nên để public vì sẽ leak thông tin bất kỳ user)
     * NOTE: Lấy thông tin người dùng hiện tại dùng GET /api/users/profile
     */
    @GetMapping("/thong-tin/{taiKhoanId}")
    @PreAuthorize(Constants.ROLE_ADMIN_EXPR)
    public ResponseEntity<ApiResponse<UserInfoResponse>> getThongTinUser(
            @PathVariable Long taiKhoanId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserProfile(taiKhoanId)));
    }

    /** A4: Đăng xuất */
    @PostMapping("/dang-xuat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> dangXuat(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // JWT stateless: client xóa token phía client. Log server-side.
        authService.logout(String.valueOf(userDetails.getId()));
        return ResponseEntity.ok(ApiResponse.success("Đã đăng xuất thành công", null));
    }

    /** A6: Quên mật khẩu */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        accountWorkflowService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Hướng dẫn đặt lại mật khẩu đã được gửi đến email", null));
    }

    /** A7: Đặt lại mật khẩu */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        accountWorkflowService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công", null));
    }

    /** A8: Xác thực email */
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @RequestParam String token) {
        accountWorkflowService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Xác thực email thành công, bạn có thể đăng nhập.", null));
    }
}