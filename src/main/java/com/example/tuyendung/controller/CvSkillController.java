package com.example.tuyendung.controller;

import com.example.tuyendung.common.ApiResponse;
import com.example.tuyendung.common.JwtTokenExtractor;
import com.example.tuyendung.dto.request.CvSkillRequest;
import com.example.tuyendung.dto.response.CvSkillResponse;
import com.example.tuyendung.service.CvSkillService;
import com.example.tuyendung.service.HoSoCvService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller cho CV Skills (E4-E7)
 * 
 * SOLID Principles:
 * - Single Responsibility: Chỉ xử lý HTTP requests/responses
 * - Dependency Inversion: Inject service interfaces
 * 
 * Security: Requires Authorization header + CV ownership verification
 */
@Slf4j
@RestController
@RequestMapping("/api/cvs/{cvId}/skills")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CvSkillController {

    private final CvSkillService cvSkillService;
    private final HoSoCvService hoSoCvService;
    private final JwtTokenExtractor jwtTokenExtractor;

    /**
     * E4: Thêm kỽ năng vào CV (POST)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CvSkillResponse>> addSkillToCv(
            @PathVariable Long cvId,
            @Valid @RequestBody CvSkillRequest request,
            @RequestHeader("Authorization") String authorization) {
        log.info("POST /api/cvs/{}/skills - Thêm kỽ năng", cvId);

        Long ungVienId = jwtTokenExtractor.extractUserIdFromToken(authorization);
        hoSoCvService.getCvById(cvId, ungVienId); // Verify ownership

        CvSkillResponse response = cvSkillService.addSkillToCv(cvId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm kỽ năng vào CV thành công", response));
    }

    /**
     * E5: Lấy danh sách kỽ năng CV (GET)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CvSkillResponse>>> getCvSkills(
            @PathVariable Long cvId,
            @RequestHeader("Authorization") String authorization) {
        log.info("GET /api/cvs/{}/skills - Danh sách kỽ năng", cvId);

        Long ungVienId = jwtTokenExtractor.extractUserIdFromToken(authorization);
        hoSoCvService.getCvById(cvId, ungVienId); // Verify ownership

        List<CvSkillResponse> skills = cvSkillService.getCvSkills(cvId);

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách kỽ năng thành công", skills));
    }

    /**
     * E6: Cập nhật mức thành thạo (PUT)
     */
    @PutMapping("/{skillId}")
    public ResponseEntity<ApiResponse<CvSkillResponse>> updateCvSkillProficiency(
            @PathVariable Long cvId,
            @PathVariable Long skillId,
            @Valid @RequestBody CvSkillRequest request,
            @RequestHeader("Authorization") String authorization) {
        log.info("PUT /api/cvs/{}/skills/{} - Cập nhật mức thành thạo", cvId, skillId);

        Long ungVienId = jwtTokenExtractor.extractUserIdFromToken(authorization);
        hoSoCvService.getCvById(cvId, ungVienId); // Verify ownership

        CvSkillResponse response = cvSkillService.updateCvSkillProficiency(cvId, skillId, request);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật kỽ năng thành công", response));
    }

    /**
     * E7: Xóa kỽ năng (DELETE)
     */
    @DeleteMapping("/{skillId}")
    public ResponseEntity<ApiResponse<Void>> deleteCvSkill(
            @PathVariable Long cvId,
            @PathVariable Long skillId,
            @RequestHeader("Authorization") String authorization) {
        log.info("DELETE /api/cvs/{}/skills/{} - Xóa kỽ năng", cvId, skillId);

        Long ungVienId = jwtTokenExtractor.extractUserIdFromToken(authorization);
        hoSoCvService.getCvById(cvId, ungVienId); // Verify ownership

        cvSkillService.deleteCvSkill(cvId, skillId);

        return ResponseEntity.ok(ApiResponse.success("Xóa kỽ năng thành công", null));
    }
}