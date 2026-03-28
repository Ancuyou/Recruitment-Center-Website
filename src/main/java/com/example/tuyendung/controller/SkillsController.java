package com.example.tuyendung.controller;

import com.example.tuyendung.common.ApiResponse;
import com.example.tuyendung.dto.request.KyNangRequest;
import com.example.tuyendung.dto.response.KyNangResponse;
import com.example.tuyendung.service.KyNangService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller cho quản lý kỹ năng
 * Endpoint: /api/skills
 * SOLID Principles:
 * - Single Responsibility: Chỉ xử lý HTTP requests/responses
 * - Dependency Inversion: Inject KyNangService interface
 */
@Slf4j
@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SkillsController {

    // Dependency Injection
    private final KyNangService kyNangService;

    /**
     * C12: Danh sách kỹ năng (GET)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<KyNangResponse>>> getAllSkills() {
        log.info("GET /api/skills - Danh sách kỹ năng");

        List<KyNangResponse> skills = kyNangService.getAllKyNang();

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách kỹ năng thành công", skills));
    }

    /**
     * C13: Tạo kỹ năng mới (POST)
     * Chỉ admin mới có thể tạo kỹ năng
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<KyNangResponse>> createSkill(
            @Valid @RequestBody KyNangRequest request) {
        log.info("POST /api/skills - Tạo kỹ năng mới");

        KyNangResponse response = kyNangService.createKyNang(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo kỹ năng thành công", response));
    }

    /**
     * C14: Tìm kiếm kỹ năng (GET)
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<KyNangResponse>>> searchSkills(
            @RequestParam String keyword) {
        log.info("GET /api/skills/search - Tìm kiếm kỹ năng: {}", keyword);

        List<KyNangResponse> skills = kyNangService.searchKyNang(keyword);

        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm kỹ năng thành công", skills));
    }

    /**
     * Lấy chi tiết kỹ năng (GET)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KyNangResponse>> getSkillById(
            @PathVariable Long id) {
        log.info("GET /api/skills/{} - Chi tiết kỹ năng", id);

        KyNangResponse response = kyNangService.getKyNangById(id);

        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết kỹ năng thành công", response));
    }

    /**
     * Cập nhật kỹ năng (PUT)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<KyNangResponse>> updateSkill(
            @PathVariable Long id,
            @Valid @RequestBody KyNangRequest request) {
        log.info("PUT /api/skills/{} - Cập nhật kỹ năng", id);

        KyNangResponse response = kyNangService.updateKyNang(id, request);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật kỹ năng thành công", response));
    }

    /**
     * Xóa kỹ năng (DELETE)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSkill(
            @PathVariable Long id) {
        log.info("DELETE /api/skills/{} - Xóa kỹ năng", id);

        kyNangService.deleteKyNang(id);

        return ResponseEntity.ok(ApiResponse.success("Xóa kỹ năng thành công", null));
    }
}
