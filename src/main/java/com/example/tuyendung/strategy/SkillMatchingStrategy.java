package com.example.tuyendung.strategy;

import com.example.tuyendung.dto.response.MatchScoreResponse;
import java.util.List;

/**
 * Strategy Pattern Interface cho matching algorithms
 * 
 * SOLID Principles:
 * - Open/Closed Principle: Open for extension, closed for modification
 * - Strategy Pattern: Encapsulate algorithms in separate classes
 * - Dependency Inversion: Service depends on abstraction, not concrete impl
 * 
 * Designed để dễ dàng thêm các thuật toán mới:
 * - CosineSimilarityStrategy (ML-based)
 * - KeywordMatchingStrategy (Simple keyword)
 * - WeightedStrategy (Combination)
 * - etc.
 */
public interface SkillMatchingStrategy {

    /**
     * Tính điểm match giữa CV và Job dựa trên kỹ năng
     * 
     * @param cvSkillIds Danh sách ID kỹ năng của CV
     * @param cvSkillProficiency Map<SkillId, ProficiencyLevel> cho CV
     * @param jobSkillIds Danh sách ID kỹ năng yêu cầu của job
     * @param jobSkillRequirements Map<SkillId, RequirementLevel> cho job
     * @return MatchScoreResponse với score từ 0-1.0
     */
    MatchScoreResponse calculateMatchScore(
            List<Long> cvSkillIds,
            java.util.Map<Long, Integer> cvSkillProficiency,
            List<Long> jobSkillIds,
            java.util.Map<Long, Integer> jobSkillRequirements
    );

    /**
     * Lấy tên thuật toán
     */
    String getStrategyName();

}