package com.example.tuyendung.controller;

import com.example.tuyendung.common.ApiResponse;
import com.example.tuyendung.common.Constants;
import com.example.tuyendung.dto.request.CvSkillRequest;
import com.example.tuyendung.dto.response.CvSkillResponse;
import com.example.tuyendung.entity.UngVien;
import com.example.tuyendung.exception.BaseBusinessException;
import com.example.tuyendung.exception.ErrorCode;
import com.example.tuyendung.repository.UngVienRepository;
import com.example.tuyendung.security.CustomUserDetails;
import com.example.tuyendung.service.CvSkillService;
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
 * Controller cho CV Skills
 * SOLID Principles:
 * - Single Responsibility: Chỉ xử lý HTTP requests/responses
 * - Dependency Inversion: Inject service interfaces
 */
@Slf4j
@RestController
@RequestMapping("/api/cvs/{cvId}/skills")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CvSkillController {

    private final CvSkillService cvSkillService;
    private final HoSoCvService hoSoCvService;
    private final UngVienRepository ungVienRepository;

    private Long resolveUngVienId(Long taiKhoanId) {
        UngVien ungVien = ungVienRepository.findByTaiKhoanId(taiKhoanId)
                .orElseThrow(() -> new BaseBusinessException(
                        ErrorCode.CANDIDATE_NOT_FOUND,
                        "Không tìm thấy ứng viên cho tài khoản ID: " + taiKhoanId));
        return ungVien.getId();
    }

    /** E4: Thêm kỹ năng vào CV (POST) */
    @PostMapping
    @PreAuthorize(Constants.ROLE_UV_EXPR)
    public ResponseEntity<ApiResponse<CvSkillResponse>> addSkillToCv(
            @PathVariable Long cvId,
            @Valid @RequestBody CvSkillRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("POST /api/cvs/{}/skills - Thêm kỹ năng", cvId);
        Long ungVienId = resolveUngVienId(userDetails.getId());
        hoSoCvService.getCvById(cvId, ungVienId); // Verify ownership
        CvSkillResponse response = cvSkillService.addSkillToCv(cvId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm kỹ năng vào CV thành công", response));
    }

    /** E5: Lấy danh sách kỹ năng CV (GET) */
    @GetMapping
    @PreAuthorize(Constants.ROLE_UV_EXPR)
    public ResponseEntity<ApiResponse<List<CvSkillResponse>>> getCvSkills(
            @PathVariable Long cvId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("GET /api/cvs/{}/skills - Danh sách kỹ năng", cvId);
        Long ungVienId = resolveUngVienId(userDetails.getId());
        hoSoCvService.getCvById(cvId, ungVienId); // Verify ownership
        List<CvSkillResponse> skills = cvSkillService.getCvSkills(cvId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách kỹ năng thành công", skills));
    }

    /** E6: Cập nhật mức thành thạo (PUT) */
    @PutMapping("/{skillId}")
    @PreAuthorize(Constants.ROLE_UV_EXPR)
    public ResponseEntity<ApiResponse<CvSkillResponse>> updateCvSkillProficiency(
            @PathVariable Long cvId,
            @PathVariable Long skillId,
            @Valid @RequestBody CvSkillRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("PUT /api/cvs/{}/skills/{} - Cập nhật mức thành thạo", cvId, skillId);
        Long ungVienId = resolveUngVienId(userDetails.getId());
        hoSoCvService.getCvById(cvId, ungVienId); // Verify ownership
        CvSkillResponse response = cvSkillService.updateCvSkillProficiency(cvId, skillId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật kỹ năng thành công", response));
    }

    /** E7: Xóa kỹ năng (DELETE) */
    @DeleteMapping("/{skillId}")
    @PreAuthorize(Constants.ROLE_UV_EXPR)
    public ResponseEntity<ApiResponse<Void>> deleteCvSkill(
            @PathVariable Long cvId,
            @PathVariable Long skillId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("DELETE /api/cvs/{}/skills/{} - Xóa kỹ năng", cvId, skillId);
        Long ungVienId = resolveUngVienId(userDetails.getId());
        hoSoCvService.getCvById(cvId, ungVienId); // Verify ownership
        cvSkillService.deleteCvSkill(cvId, skillId);
        return ResponseEntity.ok(ApiResponse.success("Xóa kỹ năng thành công", null));
    }
}