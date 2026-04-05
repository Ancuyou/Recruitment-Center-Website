package com.example.tuyendung.controller;

import com.example.tuyendung.common.ApiResponse;
import com.example.tuyendung.common.Constants;
import com.example.tuyendung.dto.request.HoSoCvRequest;
import com.example.tuyendung.dto.response.HoSoCvResponse;
import com.example.tuyendung.entity.UngVien;
import com.example.tuyendung.exception.BaseBusinessException;
import com.example.tuyendung.exception.ErrorCode;
import com.example.tuyendung.repository.UngVienRepository;
import com.example.tuyendung.security.CustomUserDetails;
import com.example.tuyendung.service.HoSoCvService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller cho CV (Module C)
 * Endpoint: /api/cvs
 * SOLID Principles:
 * - Single Responsibility: Chỉ xử lý HTTP requests/responses
 * - Dependency Inversion: Inject HoSoCvService interface
 */
@Slf4j
@RestController
@RequestMapping("/api/cvs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HoSoCvController {

    private final HoSoCvService hoSoCvService;
    private final UngVienRepository ungVienRepository;

    private Long resolveUngVienId(Long taiKhoanId) {
        UngVien ungVien = ungVienRepository.findByTaiKhoanId(taiKhoanId)
                .orElseThrow(() -> new BaseBusinessException(
                        ErrorCode.CANDIDATE_NOT_FOUND,
                        "Không tìm thấy ứng viên cho tài khoản ID: " + taiKhoanId));
        return ungVien.getId();
    }

    /** C1: Tạo CV mới (POST) */
    @PostMapping
    @PreAuthorize(Constants.ROLE_UV_EXPR)
    public ResponseEntity<ApiResponse<HoSoCvResponse>> createCv(
            @Valid @RequestBody HoSoCvRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("POST /api/cvs - Tạo CV mới");
        Long ungVienId = resolveUngVienId(userDetails.getId());
        HoSoCvResponse response = hoSoCvService.createCv(ungVienId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo CV thành công", response));
    }

    /** C2: Danh sách CV (GET) */
    @GetMapping
    @PreAuthorize(Constants.ROLE_UV_EXPR)
    public ResponseEntity<ApiResponse<List<HoSoCvResponse>>> getCvList(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("GET /api/cvs - Danh sách CV");
        Long ungVienId = resolveUngVienId(userDetails.getId());
        List<HoSoCvResponse> cvList = hoSoCvService.getCvsByUngVienId(ungVienId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách CV thành công", cvList));
    }

    /** C3: Chi tiết CV (GET) */
    @GetMapping("/{id}")
    @PreAuthorize(Constants.ROLE_UV_EXPR)
    public ResponseEntity<ApiResponse<HoSoCvResponse>> getCvDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("GET /api/cvs/{} - Chi tiết CV", id);
        Long ungVienId = resolveUngVienId(userDetails.getId());
        HoSoCvResponse response = hoSoCvService.getCvById(id, ungVienId);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết CV thành công", response));
    }

    /** C4: Cập nhật CV (PUT) */
    @PutMapping("/{id}")
    @PreAuthorize(Constants.ROLE_UV_EXPR)
    public ResponseEntity<ApiResponse<HoSoCvResponse>> updateCv(
            @PathVariable Long id,
            @Valid @RequestBody HoSoCvRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("PUT /api/cvs/{} - Cập nhật CV", id);
        Long ungVienId = resolveUngVienId(userDetails.getId());
        HoSoCvResponse response = hoSoCvService.updateCv(id, ungVienId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật CV thành công", response));
    }

    /** C5: Xóa mềm CV (DELETE) */
    @DeleteMapping("/{id}")
    @PreAuthorize(Constants.ROLE_UV_EXPR)
    public ResponseEntity<ApiResponse<Void>> deleteCv(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("DELETE /api/cvs/{} - Xóa mềm CV", id);
        Long ungVienId = resolveUngVienId(userDetails.getId());
        hoSoCvService.softDeleteCv(id, ungVienId);
        return ResponseEntity.ok(ApiResponse.success("Xóa CV thành công", null));
    }

    /** C6: Đặt CV chính (POST) */
    @PostMapping("/{id}/set-default")
    @PreAuthorize(Constants.ROLE_UV_EXPR)
    public ResponseEntity<ApiResponse<Void>> setDefaultCv(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("POST /api/cvs/{}/set-default - Đặt CV chính", id);
        Long ungVienId = resolveUngVienId(userDetails.getId());
        hoSoCvService.setDefaultCv(ungVienId, id);
        return ResponseEntity.ok(ApiResponse.success("Đặt CV chính thành công", null));
    }

    /**
     * C7: Upload file PDF CV thực tế (POST multipart/form-data)
     * [H8] Nhận file vật lý từ user thay vì nọn String URL giả lập từ Client.
     */
    @PostMapping(value = "/{id}/upload-file", consumes = "multipart/form-data")
    @PreAuthorize(Constants.ROLE_UV_EXPR)
    public ResponseEntity<ApiResponse<HoSoCvResponse>> uploadCvFile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {
        log.info("POST /api/cvs/{}/upload-file - Upload file PDF thực tế, size={}", id, file.getSize());
        Long ungVienId = resolveUngVienId(userDetails.getId());
        HoSoCvResponse response = hoSoCvService.uploadCvFile(
                id,
                ungVienId,
                file.getBytes(),
                file.getContentType(),
                file.getOriginalFilename());
        return ResponseEntity.ok(ApiResponse.success("Upload file thành công", response));
    }
}
