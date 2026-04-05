package com.example.tuyendung.controller;

import com.example.tuyendung.common.ApiResponse;
import com.example.tuyendung.common.Constants;
import com.example.tuyendung.dto.request.AdminUserCreateRequest;
import com.example.tuyendung.dto.request.AdminUserUpdateRequest;
import com.example.tuyendung.dto.response.AdminUserResponse;
import com.example.tuyendung.entity.enums.VaiTroTaiKhoan;
import com.example.tuyendung.security.CustomUserDetails;
import com.example.tuyendung.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize(Constants.ROLE_ADMIN_EXPR)
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) VaiTroTaiKhoan vaiTro,
            @RequestParam(required = false) Boolean laKichHoat
    ) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách người dùng thành công",
                adminUserService.getUsers(keyword, vaiTro, laKichHoat)));
    }

    @GetMapping("/{taiKhoanId}")
    public ResponseEntity<ApiResponse<AdminUserResponse>> getUserById(@PathVariable Long taiKhoanId) {
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin người dùng thành công",
                adminUserService.getUserById(taiKhoanId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminUserResponse>> createUser(
            @Valid @RequestBody AdminUserCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo người dùng thành công", adminUserService.createUser(request)));
    }

    @PutMapping("/{taiKhoanId}")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateUser(
            @PathVariable Long taiKhoanId,
            @Valid @RequestBody AdminUserUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật người dùng thành công",
                adminUserService.updateUser(taiKhoanId, request)));
    }

    @PatchMapping("/{taiKhoanId}/lock")
    public ResponseEntity<ApiResponse<AdminUserResponse>> lockUser(
            @PathVariable Long taiKhoanId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success("Khóa tài khoản thành công",
                adminUserService.lockUser(taiKhoanId, userDetails.getId())));
    }

    @PatchMapping("/{taiKhoanId}/unlock")
    public ResponseEntity<ApiResponse<AdminUserResponse>> unlockUser(@PathVariable Long taiKhoanId) {
        return ResponseEntity.ok(ApiResponse.success("Mở khóa tài khoản thành công",
                adminUserService.unlockUser(taiKhoanId)));
    }

    @DeleteMapping("/{taiKhoanId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long taiKhoanId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        adminUserService.deleteUser(taiKhoanId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Xóa người dùng thành công", null));
    }
}
