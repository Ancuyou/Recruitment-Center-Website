package com.example.tuyendung.controller;

import com.example.tuyendung.common.ApiResponse;
import com.example.tuyendung.common.Constants;
import com.example.tuyendung.dto.request.ChiTietCvRequest;
import com.example.tuyendung.dto.response.ChiTietCvResponse;
import com.example.tuyendung.security.CustomUserDetails;
import com.example.tuyendung.service.ChiTietCvService;
import com.example.tuyendung.service.HoSoCvService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller cho chi tiết CV (học vấn, kinh nghiệm, chứng chỉ)
 * Endpoint: /api/cvs/{cvId}/hoc-van-kn
 * SOLID Principles:
 * - Single Responsibility: Chỉ xử lý HTTP requests/responses
 * - Dependency Inversion: Inject ChiTietCvService interface
 */
@Slf4j
@RestController
@RequestMapping("/api/cvs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChiTietCvController {

    private final ChiTietCvService chiTietCvService;
    private final HoSoCvService hoSoCvService;

    /** C8: Thêm học vấn/KN (POST) */
    @PostMapping("/{cvId}/hoc-van-kn")
    @PreAuthorize(Constants.ROLE_UV_EXPR)
    public ResponseEntity<ApiResponse<ChiTietCvResponse>> addChiTietCv(
            @PathVariable Long cvId,
            @Valid @RequestBody ChiTietCvRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("POST /api/cvs/{}/hoc-van-kn - Thêm chi tiết CV", cvId);
        hoSoCvService.getCvById(cvId, userDetails.getId()); // Verify CV ownership
        ChiTietCvResponse response = chiTietCvService.addChiTietCv(cvId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm chi tiết CV thành công", response));
    }

    /** C9: Danh sách học vấn/KN (GET) */
    @GetMapping("/{cvId}/hoc-van-kn")
    @PreAuthorize(Constants.ROLE_UV_EXPR)
    public ResponseEntity<ApiResponse<List<ChiTietCvResponse>>> getChiTietCvList(
            @PathVariable Long cvId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("GET /api/cvs/{}/hoc-van-kn - Danh sách chi tiết CV", cvId);
        hoSoCvService.getCvById(cvId, userDetails.getId()); // Verify CV ownership
        List<ChiTietCvResponse> chiTietList = chiTietCvService.getChiTietCvList(cvId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách chi tiết CV thành công", chiTietList));
    }

    /** C10: Cập nhật học vấn/KN (PUT) */
    @PutMapping("/{cvId}/hoc-van-kn/{id}")
    @PreAuthorize(Constants.ROLE_UV_EXPR)
    public ResponseEntity<ApiResponse<ChiTietCvResponse>> updateChiTietCv(
            @PathVariable Long cvId,
            @PathVariable Long id,
            @Valid @RequestBody ChiTietCvRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("PUT /api/cvs/{}/hoc-van-kn/{} - Cập nhật chi tiết CV", cvId, id);
        hoSoCvService.getCvById(cvId, userDetails.getId()); // Verify CV ownership
        ChiTietCvResponse response = chiTietCvService.updateChiTietCv(cvId, id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật chi tiết CV thành công", response));
    }

    /** C11: Xóa học vấn/KN (DELETE) */
    @DeleteMapping("/{cvId}/hoc-van-kn/{id}")
    @PreAuthorize(Constants.ROLE_UV_EXPR)
    public ResponseEntity<ApiResponse<Void>> deleteChiTietCv(
            @PathVariable Long cvId,
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("DELETE /api/cvs/{}/hoc-van-kn/{} - Xóa chi tiết CV", cvId, id);
        hoSoCvService.getCvById(cvId, userDetails.getId()); // Verify CV ownership
        chiTietCvService.deleteChiTietCv(cvId, id);
        return ResponseEntity.ok(ApiResponse.success("Xóa chi tiết CV thành công", null));
    }

    /** Lấy chi tiết theo loại bản ghi */
    @GetMapping("/{cvId}/hoc-van-kn/type/{loaiBanGhi}")
    @PreAuthorize(Constants.ROLE_UV_EXPR)
    public ResponseEntity<ApiResponse<List<ChiTietCvResponse>>> getChiTietCvByType(
            @PathVariable Long cvId,
            @PathVariable Integer loaiBanGhi,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("GET /api/cvs/{}/hoc-van-kn/type/{} - Danh sách chi tiết theo loại", cvId, loaiBanGhi);
        hoSoCvService.getCvById(cvId, userDetails.getId()); // Verify CV ownership
        List<ChiTietCvResponse> chiTietList = chiTietCvService.getChiTietCvByType(cvId, loaiBanGhi);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách chi tiết theo loại thành công", chiTietList));
    }
}
