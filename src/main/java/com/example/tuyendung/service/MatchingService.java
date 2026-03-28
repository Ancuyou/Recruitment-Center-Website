package com.example.tuyendung.service;

import com.example.tuyendung.dto.response.CandidateSuggestionResponse;
import com.example.tuyendung.dto.response.JobSuggestionResponse;
import com.example.tuyendung.dto.response.MatchScoreResponse;
import java.util.List;

/**
 * Service Interface cho Matching Algorithm (E1-E3)
 * 
 * SOLID Principles:
 * - Dependency Inversion: Uses Strategy pattern via SkillMatchingStrategy
 * - Single Responsibility: Chỉ xử lý matching logic
 * - Open/Closed: Easy to add new strategies without modifying this
 * - Strategy Pattern: Pluggable algorithms
 */
public interface MatchingService {

    /**
     * E1: Gợi ý ứng viên cho job
     * @param jobId ID công việc
     * @param limit Số lượng gợi ý
     * @return Danh sách ứng viên được sắp xếp theo match score giảm dần
     */
    List<CandidateSuggestionResponse> suggestCandidatesForJob(Long jobId, Integer limit);

    /**
     * E2: Gợi ý công việc cho ứng viên
     * @param candidateId ID ứng viên
     * @param limit Số lượng gợi ý
     * @return Danh sách công việc được sắp xếp theo match score giảm dần
     */
    List<JobSuggestionResponse> suggestJobsForCandidate(Long candidateId, Integer limit);

    /**
     * E3: Tính điểm match giữa CV và Job
     * @param cvId ID CV
     * @param jobId ID Job
     * @return Match score với chi tiết
     */
    MatchScoreResponse calculateMatchScore(Long cvId, Long jobId);

    /**
     * Tính điểm match với strategy cụ thể
     * @param strategyName Tên strategy (e.g., "Cosine Similarity", "Keyword Matching")
     */
    MatchScoreResponse calculateMatchScoreWithStrategy(Long cvId, Long jobId, String strategyName);

}