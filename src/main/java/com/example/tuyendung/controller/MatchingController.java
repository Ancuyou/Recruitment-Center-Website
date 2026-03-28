package com.example.tuyendung.controller;

import com.example.tuyendung.common.ApiResponse;
import com.example.tuyendung.dto.request.MatchScoreRequest;
import com.example.tuyendung.dto.response.CandidateSuggestionResponse;
import com.example.tuyendung.dto.response.JobSuggestionResponse;
import com.example.tuyendung.dto.response.MatchScoreResponse;
import com.example.tuyendung.service.MatchingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller cho Matching Algorithm (E1-E3)
 * 
 * SOLID Principles:
 * - Single Responsibility: Chỉ xử lý HTTP requests/responses
 * - Dependency Inversion: Inject MatchingService interface
 * 
 * Features:
 * - Smart matching using Strategy pattern
 * - Skill-based recommendation algorithm
 * - Configurable matching strategies
 * - Public endpoints (no authorization needed)
 */
@Slf4j
@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MatchingController {

    private final MatchingService matchingService;

    /**
     * E1: Gợi ý ứng viên cho job
     * Trả về danh sách ứng viên phù hợp nhất sắp xếp theo match score
     * 
     * @param jobId ID công việc
     * @param limit Số lượng gợi ý (default: 10)
     * @return Danh sách ứng viên với match score
     */
    @GetMapping("/candidates/{jobId}")
    public ResponseEntity<ApiResponse<List<CandidateSuggestionResponse>>> suggestCandidatesForJob(
            @PathVariable Long jobId,
            @RequestParam(required = false) Integer limit) {
        log.info("E1: GET /api/matching/candidates/{} - Gợi ý ứng viên", jobId);

        List<CandidateSuggestionResponse> candidates = matchingService.suggestCandidatesForJob(jobId, limit);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Gợi ý ứng viên thành công: " + candidates.size() + " kết quả",
                        candidates
                )
        );
    }

    /**
     * E2: Gợi ý công việc cho ứng viên
     * Trả về danh sách công việc phù hợp nhất sắp xếp theo match score
     * 
     * @param candidateId ID ứng viên (sử dụng default CV)
     * @param limit Số lượng gợi ý (default: 10)
     * @return Danh sách công việc với match score
     */
    @GetMapping("/jobs/{candidateId}")
    public ResponseEntity<ApiResponse<List<JobSuggestionResponse>>> suggestJobsForCandidate(
            @PathVariable Long candidateId,
            @RequestParam(required = false) Integer limit) {
        log.info("E2: GET /api/matching/jobs/{} - Gợi ý công việc", candidateId);

        List<JobSuggestionResponse> jobs = matchingService.suggestJobsForCandidate(candidateId, limit);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Gợi ý công việc thành công: " + jobs.size() + " kết quả",
                        jobs
                )
        );
    }

    /**
     * E3: Tính điểm match giữa CV và Job
     * Sử dụng thuật toán mặc định (Cosine Similarity)
     * 
     * @param request Chứa cvId và jobId
     * @return MatchScore với chi tiết kỹ năng trùng/thiếu
     */
    @PostMapping("/score")
    public ResponseEntity<ApiResponse<MatchScoreResponse>> calculateMatchScore(
            @Valid @RequestBody MatchScoreRequest request) {
        log.info("E3: POST /api/matching/score - CV {} vs Job {}", request.getCvId(), request.getJobId());

        MatchScoreResponse score = matchingService.calculateMatchScore(request.getCvId(), request.getJobId());

        return ResponseEntity.ok(
                ApiResponse.success(
                        String.format("Match score: %.0f%% (%s)", 
                                score.getMatchPercentage(), 
                                score.getMatchLevel()),
                        score
                )
        );
    }

    /**
     * Tính điểm match với strategy cụ thể
     * Hỗ trợ multiple matching algorithms
     * 
     * @param request Chứa cvId và jobId
     * @param strategy Tên strategy: "cosine_similarity" (default) hoặc "keyword_matching"
     * @return MatchScore theo strategy chọn
     */
    @PostMapping("/score/{strategy}")
    public ResponseEntity<ApiResponse<MatchScoreResponse>> calculateMatchScoreWithStrategy(
            @Valid @RequestBody MatchScoreRequest request,
            @PathVariable String strategy) {
        log.info("POST /api/matching/score/{} - CV {} vs Job {}", strategy, request.getCvId(), request.getJobId());

        MatchScoreResponse score = matchingService.calculateMatchScoreWithStrategy(
                request.getCvId(),
                request.getJobId(),
                strategy
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        String.format("Match score (%s): %.0f%%", strategy, score.getMatchPercentage()),
                        score
                )
        );
    }

}