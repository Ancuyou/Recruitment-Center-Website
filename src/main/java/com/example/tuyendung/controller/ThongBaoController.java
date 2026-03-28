package com.example.tuyendung.controller;

import com.example.tuyendung.common.ApiResponse;
import com.example.tuyendung.dto.response.ThongBaoResponse;
import com.example.tuyendung.security.CustomUserDetails;
import com.example.tuyendung.service.ThongBaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class ThongBaoController {

    private final ThongBaoService thongBaoService;

    // D15: Get notifications
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ThongBaoResponse>>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Lấy thông báo thành công",
                thongBaoService.getMyNotifications(userDetails.getId(), page, size)));
    }

    // D16: Mark one as read
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        thongBaoService.markAsRead(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Đã ghi nhận đọc", null));
    }

    // D17: Count unread
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> countUnread(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Số lượng chưa đọc",
                thongBaoService.countUnread(userDetails.getId())));
    }

    // D18: Mark all as read
    @PatchMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        thongBaoService.markAllAsRead(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Đã đọc tất cả", null));
    }
}
