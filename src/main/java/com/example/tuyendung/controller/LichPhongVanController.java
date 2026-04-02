package com.example.tuyendung.controller;

import com.example.tuyendung.common.ApiResponse;
import com.example.tuyendung.common.Constants;
import com.example.tuyendung.dto.request.LichPhongVanRequest;
import com.example.tuyendung.dto.request.RescheduleRequest;
import com.example.tuyendung.dto.response.LichPhongVanResponse;
import com.example.tuyendung.security.CustomUserDetails;
import com.example.tuyendung.service.LichPhongVanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý lịch phỏng vấn – Module D9-D14.
 *
 * Mapping theo spec:
 *  D9  POST   /api/interviews                     → Tạo lịch (HR)
 *  D10 GET    /api/interviews/{applicationId}/list → Lịch của một đơn (UV + HR)
 *  D11 GET    /api/interviews/{id}                 → Chi tiết lịch (UV + HR)
 *  D12 PUT    /api/interviews/{id}                 → Cập nhật lịch (HR)
 *  D13 DELETE /api/interviews/{id}                 → Hủy lịch (HR)
 *  D14 PATCH  /api/interviews/{id}/reschedule      → Đổi giờ lịch (HR)
 *
 * Lưu ý: D10 dùng path /api/interviews/{applicationId}/list để tránh
 *   xung đột route với D11 /api/interviews/{id}.
 *
 * SOLID:
 *  - SRP: chỉ điều phối HTTP request, không chứa business logic.
 *  - DIP: phụ thuộc interface LichPhongVanService.
 */
@Slf4j
@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class LichPhongVanController {

    private final LichPhongVanService lichPhongVanService;

    /**
     * D9: Tạo lịch phỏng vấn (chỉ RECRUITER).
     * POST /api/interviews
     */
    @PreAuthorize(Constants.ROLE_NTD_EXPR)
    @PostMapping
    public ResponseEntity<ApiResponse<LichPhongVanResponse>> createInterview(
            @Valid @RequestBody LichPhongVanRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("D9: POST /api/interviews - donUngTuyenId={}, taiKhoanId={}",
                request.getDonUngTuyenId(), userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo lịch phỏng vấn thành công",
                        lichPhongVanService.createInterview(request, userDetails.getId())));
    }

    /**
     * D10b: Danh sách lịch phỏng vấn HR đang quản lý (phân trang).
     * GET /api/interviews/my
     *
     * Giải quyết ISP: getMyInterviews() trong service có implementation nhưng
     * không có endpoint nào gọi → expose ra đây để loại bỏ dead code.
     * Không xung đột route vì "/my" khác với "/{applicationId}/list".
     */
    @PreAuthorize(Constants.ROLE_NTD_EXPR)
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<LichPhongVanResponse>>> getMyInterviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("D10b: GET /api/interviews/my - taiKhoanId={}", userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Danh sách lịch HR",
                lichPhongVanService.getMyInterviews(userDetails.getId(), page, size)));
    }

    /**
     * D10: Danh sách lịch phỏng vấn của một đơn ứng tuyển (UV + HR liên quan).
     * GET /api/interviews/{applicationId}/list
     *
     * Dùng /list suffix để tránh xung đột route với D11 GET /api/interviews/{id}.
     * Yêu cầu đăng nhập; service kiểm tra ownership theo vai trò.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{applicationId}/list")
    public ResponseEntity<ApiResponse<List<LichPhongVanResponse>>> getInterviewsByApplication(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("D10: GET /api/interviews/{}/list - taiKhoanId={}", applicationId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Danh sách lịch phỏng vấn",
                lichPhongVanService.getInterviewsByApplication(applicationId, userDetails.getId())));
    }

    /**
     * D11: Chi tiết một lịch phỏng vấn (UV + HR liên quan).
     * GET /api/interviews/{id}
     *
     * Yêu cầu đăng nhập; service kiểm tra ownership theo vai trò.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LichPhongVanResponse>> getInterviewDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("D11: GET /api/interviews/{} - taiKhoanId={}", id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Chi tiết lịch phỏng vấn",
                lichPhongVanService.getInterviewDetail(id, userDetails.getId())));
    }

    /**
     * D12: Cập nhật toàn bộ thông tin lịch phỏng vấn (chỉ RECRUITER).
     * PUT /api/interviews/{id}
     */
    @PreAuthorize(Constants.ROLE_NTD_EXPR)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LichPhongVanResponse>> updateInterview(
            @PathVariable Long id,
            @Valid @RequestBody LichPhongVanRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("D12: PUT /api/interviews/{} - taiKhoanId={}", id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật lịch thành công",
                lichPhongVanService.updateInterview(id, request, userDetails.getId())));
    }

    /**
     * D13: Hủy lịch phỏng vấn (chỉ RECRUITER).
     * DELETE /api/interviews/{id}
     */
    @PreAuthorize(Constants.ROLE_NTD_EXPR)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelInterview(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("D13: DELETE /api/interviews/{} - taiKhoanId={}", id, userDetails.getId());
        lichPhongVanService.cancelInterview(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Hủy lịch thành công", null));
    }

    /**
     * D14: Đổi giờ lịch phỏng vấn – chỉ cập nhật thời gian (điểm khác với D12 là không đổi nội dung/vòng).
     * PATCH /api/interviews/{id}/reschedule
     *
     * Dùng RescheduleRequest (chỉ có thoiGianBatDau, thoiGianKetThuc, diaDiemHoacLink)
     * thay vì LichPhongVanRequest để tránh client gửi nhầm tieuDeVong/hinhThuc bị ignore.
     */
    @PreAuthorize(Constants.ROLE_NTD_EXPR)
    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<ApiResponse<LichPhongVanResponse>> rescheduleInterview(
            @PathVariable Long id,
            @Valid @RequestBody RescheduleRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("D14: PATCH /api/interviews/{}/reschedule - taiKhoanId={}", id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Dời lịch thành công",
                lichPhongVanService.rescheduleInterview(id, request, userDetails.getId())));
    }
}
