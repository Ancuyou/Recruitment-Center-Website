package com.example.tuyendung.controller;

import com.example.tuyendung.common.ApiResponse;
import com.example.tuyendung.common.JwtTokenExtractor;
import com.example.tuyendung.dto.request.HoSoCvRequest;
import com.example.tuyendung.dto.response.HoSoCvResponse;
import com.example.tuyendung.service.HoSoCvService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // Dependency Injection
    private final HoSoCvService hoSoCvService;
    private final JwtTokenExtractor jwtTokenExtractor;

    /**
     * C1: Tạo CV mới (POST)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<HoSoCvResponse>> createCv(
            @Valid @RequestBody HoSoCvRequest request,
            @RequestHeader("Authorization") String authorization) {
        log.info("POST /api/cvs - Tạo CV mới");

        Long ungVienId = jwtTokenExtractor.extractUserIdFromToken(authorization);
        HoSoCvResponse response = hoSoCvService.createCv(ungVienId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo CV thành công", response));
    }

    /**
     * C2: Danh sách CV (GET)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<HoSoCvResponse>>> getCvList(
            @RequestHeader("Authorization") String authorization) {
        log.info("GET /api/cvs - Danh sách CV");

        Long ungVienId = jwtTokenExtractor.extractUserIdFromToken(authorization);
        List<HoSoCvResponse> cvList = hoSoCvService.getCvsByUngVienId(ungVienId);

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách CV thành công", cvList));
    }

    /**
     * C3: Chi tiết CV (GET)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HoSoCvResponse>> getCvDetail(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorization) {
        log.info("GET /api/cvs/{} - Chi tiết CV", id);

        Long ungVienId = jwtTokenExtractor.extractUserIdFromToken(authorization);
        HoSoCvResponse response = hoSoCvService.getCvById(id, ungVienId);

        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết CV thành công", response));
    }

    /**
     * C4: Cập nhật CV (PUT)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HoSoCvResponse>> updateCv(
            @PathVariable Long id,
            @Valid @RequestBody HoSoCvRequest request,
            @RequestHeader("Authorization") String authorization) {
        log.info("PUT /api/cvs/{} - Cập nhật CV", id);

        Long ungVienId = jwtTokenExtractor.extractUserIdFromToken(authorization);
        HoSoCvResponse response = hoSoCvService.updateCv(id, ungVienId, request);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật CV thành công", response));
    }

    /**
     * C5: Xóa mềm CV (DELETE)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCv(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorization) {
        log.info("DELETE /api/cvs/{} - Xóa mềm CV", id);

        Long ungVienId = jwtTokenExtractor.extractUserIdFromToken(authorization);
        hoSoCvService.softDeleteCv(id, ungVienId);

        return ResponseEntity.ok(ApiResponse.success("Xóa CV thành công", null));
    }

    /**
     * C6: Đặt CV chính (POST)
     */
    @PostMapping("/{id}/set-default")
    public ResponseEntity<ApiResponse<Void>> setDefaultCv(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorization) {
        log.info("POST /api/cvs/{}/set-default - Đặt CV chính", id);

        Long ungVienId = jwtTokenExtractor.extractUserIdFromToken(authorization);
        hoSoCvService.setDefaultCv(ungVienId, id);

        return ResponseEntity.ok(ApiResponse.success("Đặt CV chính thành công", null));
    }

    /**
     * C7: Upload file PDF CV (POST)
     */
    @PostMapping("/{id}/upload-file")
    public ResponseEntity<ApiResponse<HoSoCvResponse>> uploadCvFile(
            @PathVariable Long id,
            @RequestParam String fileUrl,
            @RequestHeader("Authorization") String authorization) {
        log.info("POST /api/cvs/{}/upload-file - Upload file PDF", id);

        Long ungVienId = jwtTokenExtractor.extractUserIdFromToken(authorization);
        HoSoCvResponse response = hoSoCvService.uploadCvFile(id, ungVienId, fileUrl);

        return ResponseEntity.ok(ApiResponse.success("Upload file thành công", response));
    }

}
