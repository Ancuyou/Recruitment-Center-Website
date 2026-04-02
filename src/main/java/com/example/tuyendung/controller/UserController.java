package com.example.tuyendung.controller;

import com.example.tuyendung.common.ApiResponse;
import com.example.tuyendung.common.Constants;
import com.example.tuyendung.dto.request.ChangePasswordRequest;
import com.example.tuyendung.dto.request.UpdateCandidateProfileRequest;
import com.example.tuyendung.dto.request.UpdateRecruiterProfileRequest;
import com.example.tuyendung.dto.request.UploadAvatarRequest;
import com.example.tuyendung.dto.response.UserInfoResponse;
import com.example.tuyendung.security.CustomUserDetails;
import com.example.tuyendung.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * A9: Lấy thông tin tài khoản hiện tại (canonical endpoint — thay thế /api/auth/me đã bỏ).
     * Yêu cầu đăng nhập; trả về profile theo role (UV / NTD).
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Thông tin hồ sơ",
                userService.getUserProfile(userDetails.getId())));
    }

    // A10: Update Candidate Profile
    @PreAuthorize(Constants.ROLE_UV_EXPR)
    @PutMapping("/profile/candidate")
    public ResponseEntity<ApiResponse<UserInfoResponse>> updateCandidateProfile(
            @Valid @RequestBody UpdateCandidateProfileRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ ứng viên thành công", 
                userService.updateCandidateProfile(userDetails.getId(), request)));
    }

    // A10: Update Recruiter Profile
    @PreAuthorize(Constants.ROLE_NTD_EXPR)
    @PutMapping("/profile/recruiter")
    public ResponseEntity<ApiResponse<UserInfoResponse>> updateRecruiterProfile(
            @Valid @RequestBody UpdateRecruiterProfileRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ nhà tuyển dụng thành công", 
                userService.updateRecruiterProfile(userDetails.getId(), request)));
    }

    // A11: Change Password
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.changePassword(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công", null));
    }

    // A12: Upload Avatar
    @PostMapping("/upload-avatar")
    public ResponseEntity<ApiResponse<UserInfoResponse>> uploadAvatar(
            @Valid @RequestBody UploadAvatarRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật ảnh đại diện thành công", 
                userService.updateAvatar(userDetails.getId(), request)));
    }
}
