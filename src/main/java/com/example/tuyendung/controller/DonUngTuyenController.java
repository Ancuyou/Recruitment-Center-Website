package com.example.tuyendung.controller;

import com.example.tuyendung.common.ApiResponse;
import com.example.tuyendung.common.Constants;
import com.example.tuyendung.dto.request.DonUngTuyenRequest;
import com.example.tuyendung.dto.request.TrangThaiDonRequest;
import com.example.tuyendung.dto.response.DonUngTuyenResponse;
import com.example.tuyendung.dto.response.LichSuTrangThaiResponse;
import com.example.tuyendung.security.CustomUserDetails;
import com.example.tuyendung.service.DonUngTuyenService;
import com.example.tuyendung.service.LichSuTrangThaiService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;



/**
 * Controller xử lý nghiệp vụ đơn ứng tuyển – Module D1-D8.
 *
 * Tuân theo:
 * - SRP: chỉ điều phối HTTP request, không chứa business logic.
 * - DIP: phụ thuộc interface DonUngTuyenService, không phụ thuộc impl.
 * - Clean Code: mỗi method annotate đầy đủ @PreAuthorize, log info, endpoint rõ ràng.
 */
@Slf4j
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class DonUngTuyenController {

    private final DonUngTuyenService donUngTuyenService;
    private final LichSuTrangThaiService lichSuTrangThaiService;

    /**
     * D1: Nộp đơn ứng tuyển (chỉ CANDIDATE).
     * POST /api/applications
     */
    @PreAuthorize(Constants.ROLE_UV_EXPR)
    @PostMapping
    public ResponseEntity<ApiResponse<DonUngTuyenResponse>> submitApplication(
            @Valid @RequestBody DonUngTuyenRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("D1: POST /api/applications - taiKhoanId={}", userDetails.getId());
        DonUngTuyenResponse response = donUngTuyenService.submitApplication(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Nộp đơn thành công", response));
    }

    /**
     * D2: Danh sách đơn đã nộp của ứng viên, phân trang.
     * GET /api/applications
     */
    @PreAuthorize(Constants.ROLE_UV_EXPR)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<DonUngTuyenResponse>>> getCandidateApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("D2: GET /api/applications - taiKhoanId={}", userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thành công",
                donUngTuyenService.getCandidateApplications(userDetails.getId(), page, size)));
    }

    /**
     * D3: Danh sách đơn nhận được của một tin tuyển dụng (chỉ RECRUITER).
     * GET /api/applications/recruiter?tinTuyenDungId=X
     */
    @PreAuthorize(Constants.ROLE_NTD_EXPR)
    @GetMapping("/recruiter")
    public ResponseEntity<ApiResponse<Page<DonUngTuyenResponse>>> getRecruiterApplications(
            @RequestParam Long tinTuyenDungId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("D3: GET /api/applications/recruiter?tinTuyenDungId={} - taiKhoanId={}", tinTuyenDungId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thành công",
                donUngTuyenService.getRecruiterApplications(tinTuyenDungId, userDetails.getId(), page, size)));
    }

    /**
     * D4: Chi tiết đơn ứng tuyển (CANDIDATE chủ đơn HOẶC RECRUITER chủ tin).
     * GET /api/applications/{id}
     *
     * Không giới hạn role nhưng yêu cầu đăng nhập;
     * service tự kiểm tra ownership theo vai trò.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DonUngTuyenResponse>> getApplicationDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("D4: GET /api/applications/{} - taiKhoanId={}", id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết thành công",
                donUngTuyenService.getApplicationDetail(id, userDetails.getId())));
    }

    /**
     * D5: Cập nhật trạng thái đơn (chỉ RECRUITER).
     * PATCH /api/applications/{id}/status
     */
    @PreAuthorize(Constants.ROLE_NTD_EXPR)
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<DonUngTuyenResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody TrangThaiDonRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("D5: PATCH /api/applications/{}/status - trangThai={}", id, request.getTrangThaiMoi());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công",
                donUngTuyenService.updateStatus(id, request, userDetails.getId())));
    }

    /**
     * D6: Từ chối đơn ứng tuyển (chỉ RECRUITER).
     * PATCH /api/applications/{id}/reject
     *
     * Không nhận body (ghi chú tuỳ chọn), service tự set TrangThaiDon.TU_CHOI.
     * Tách riêng khỏi D5 để endpoint semantics rõ ràng – không thể nhầm lẫn.
     */
    @PreAuthorize(Constants.ROLE_NTD_EXPR)
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<DonUngTuyenResponse>> rejectApplication(
            @PathVariable Long id,
            @RequestParam(required = false) String ghiChu,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("D6: PATCH /api/applications/{}/reject - taiKhoanId={}", id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Từ chối đơn thành công",
                donUngTuyenService.rejectApplication(id, ghiChu, userDetails.getId())));
    }

    /**
     * D7: Lấy URL ảnh snapshot CV của đơn (chỉ RECRUITER).
     * GET /api/applications/{id}/cv-snapshot
     */
    @PreAuthorize(Constants.ROLE_NTD_EXPR)
    @GetMapping("/{id}/cv-snapshot")
    public ResponseEntity<ApiResponse<String>> getCvSnapshotUrl(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("D7: GET /api/applications/{}/cv-snapshot - taiKhoanId={}", id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy ảnh CV thành công",
                donUngTuyenService.getCvSnapshotUrl(id, userDetails.getId())));
    }

    /**
     * D8: Lịch sử trạng thái của đơn (CANDIDATE hoặc RECRUITER liên quan).
     * GET /api/applications/{id}/history
     *
     * Yêu cầu đăng nhập; service kiểm tra ownership theo vai trò.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<Page<LichSuTrangThaiResponse>>> getStatusHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("D8: GET /api/applications/{}/history - page={}, size={}, taiKhoanId={}", 
                 id, page, size, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử thành công",
                lichSuTrangThaiService.getStatusHistory(id, userDetails.getId(), page, size)));
    }
}
