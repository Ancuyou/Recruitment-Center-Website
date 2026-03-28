package com.example.tuyendung.controller;

import com.example.tuyendung.common.ApiResponse;
import com.example.tuyendung.dto.request.JobSkillRequest;
import com.example.tuyendung.dto.response.JobSkillResponse;
import com.example.tuyendung.service.JobSkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller cho Job Skills (E8-E10)
 * 
 * SOLID Principles:
 * - Single Responsibility: Chỉ xử lý HTTP requests/responses
 * - Dependency Inversion: Inject service interface
 * 
 * Security: Requires RECRUITER role to manage job skills
 * Note: Public users can view, only recruiters can modify
 */
@Slf4j
@RestController
@RequestMapping("/api/jobs/{jobId}/skills")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class JobSkillController {

    private final JobSkillService jobSkillService;

    /**
     * E8: Thêm kỽ năng yêu cầu vào Job (POST)
     * Chỉ recruiter có thể thêm
     */
    @PreAuthorize("hasRole('RECRUITER')")
    @PostMapping
    public ResponseEntity<ApiResponse<JobSkillResponse>> addSkillToJob(
            @PathVariable Long jobId,
            @Valid @RequestBody JobSkillRequest request) {
        log.info("POST /api/jobs/{}/skills - Thêm kỽ năng yêu cầu", jobId);

        JobSkillResponse response = jobSkillService.addSkillToJob(jobId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm kỽ năng yêu cầu thành công", response));
    }

    /**
     * E9: Lấy danh sách kỽ năng yêu cầu (GET)
     * Công khai
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<JobSkillResponse>>> getJobSkills(
            @PathVariable Long jobId) {
        log.info("GET /api/jobs/{}/skills - Danh sách kỽ năng yêu cầu", jobId);

        List<JobSkillResponse> skills = jobSkillService.getJobSkills(jobId);

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách kỽ năng yêu cầu thành công", skills));
    }

    /**
     * E10: Cập nhật mức yêu cầu (PUT)
     * Chỉ recruiter có thể cập nhật
     */
    @PreAuthorize("hasRole('RECRUITER')")
    @PutMapping("/{skillId}")
    public ResponseEntity<ApiResponse<JobSkillResponse>> updateJobSkillRequirement(
            @PathVariable Long jobId,
            @PathVariable Long skillId,
            @Valid @RequestBody JobSkillRequest request) {
        log.info("PUT /api/jobs/{}/skills/{} - Cập nhật mức yêu cầu", jobId, skillId);

        JobSkillResponse response = jobSkillService.updateJobSkillRequirement(jobId, skillId, request);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật kỽ năng yêu cầu thành công", response));
    }

    /**
     * Xóa kỽ năng yêu cầu (DELETE)
     * Chỉ recruiter có thể xóa
     */
    @PreAuthorize("hasRole('RECRUITER')")
    @DeleteMapping("/{skillId}")
    public ResponseEntity<ApiResponse<Void>> deleteJobSkill(
            @PathVariable Long jobId,
            @PathVariable Long skillId) {
        log.info("DELETE /api/jobs/{}/skills/{} - Xóa kỽ năng yêu cầu", jobId, skillId);

        jobSkillService.deleteJobSkill(jobId, skillId);

        return ResponseEntity.ok(ApiResponse.success("Xóa kỽ năng yêu cầu thành công", null));
    }

    /**
     * Lấy chi tiết kỽ năng (GET)
     */
    @GetMapping("/{skillId}")
    public ResponseEntity<ApiResponse<JobSkillResponse>> getJobSkillById(
            @PathVariable Long jobId,
            @PathVariable Long skillId) {
        log.info("GET /api/jobs/{}/skills/{} - Chi tiết kỽ năng", jobId, skillId);

        JobSkillResponse response = jobSkillService.getJobSkillById(jobId, skillId);

        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết kỽ năng thành công", response));
    }

}