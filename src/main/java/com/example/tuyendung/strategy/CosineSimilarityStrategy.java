package com.example.tuyendung.strategy;

import com.example.tuyendung.dto.response.MatchScoreResponse;
import com.example.tuyendung.util.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Cosine Similarity Strategy cho matching
 * 
 * Algorithm:
 * - Tạo vector cho CV skills và job skills
 * - Tính cosine similarity giữa hai vector
 * - Score = number_of_matched_skills / sqrt(cv_skills * job_skills)
 * - Adjusted by proficiency/requirement levels
 * 
 * Ưu điểm:
 * - Cân nhắc cả số lượng và chất lượng kỹ năng
 * - Sử dụng mức độ thành thạo/yêu cầu
 * - Phổ biến trong recommendation systems
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CosineSimilarityStrategy implements SkillMatchingStrategy {

    private final TimeProvider timeProvider;

    @Override
    public MatchScoreResponse calculateMatchScore(
            List<Long> cvSkillIds,
            Map<Long, Integer> cvSkillProficiency,
            List<Long> jobSkillIds,
            Map<Long, Integer> jobSkillRequirements) {

        log.info("Calculating match score using Cosine Similarity - CV: {} skills, Job: {} skills",
                cvSkillIds.size(), jobSkillIds.size());

        // Tìm các kỹ năng trùng khớp
        Set<Long> cvSkillSet = new HashSet<>(cvSkillIds);
        Set<Long> jobSkillSet = new HashSet<>(jobSkillIds);
        Set<Long> matchedSkillIds = new HashSet<>(cvSkillSet);
        matchedSkillIds.retainAll(jobSkillSet);

        // Tìm các kỹ năng thiếu
        Set<Long> missingSkillIds = new HashSet<>(jobSkillSet);
        missingSkillIds.removeAll(cvSkillSet);

        // Tính cosine similarity với weighted scores
        double matchScore = calculateWeightedSimilarity(
                matchedSkillIds,
                cvSkillProficiency,
                jobSkillRequirements,
                cvSkillIds.size(),
                jobSkillIds.size()
        );

        int matchPercentage = (int) (matchScore * 100);
        String matchLevel = MatchScoreResponse.determineMatchLevel(matchScore);

        return MatchScoreResponse.builder()
                .matchScore(Math.round(matchScore * 100.0) / 100.0) // Round to 2 decimals
                .matchPercentage(matchPercentage)
                .matchLevel(matchLevel)
                .matchedSkills(
                        matchedSkillIds.stream()
                                .map(String::valueOf)
                                .collect(Collectors.toList())
                )
                .missingSkills(
                        missingSkillIds.stream()
                                .map(String::valueOf)
                                .collect(Collectors.toList())
                )
                .calculatedAt(timeProvider.getCurrentTimeMillis())
                .build();
    }

    /**
     * Tính weighted cosine similarity
     * Formula: sum(matched_weights) / sqrt(cv_weight_sum * job_weight_sum)
     */
    private double calculateWeightedSimilarity(
            Set<Long> matchedSkillIds,
            Map<Long, Integer> cvSkillProficiency,
            Map<Long, Integer> jobSkillRequirements,
            int cvTotalSkills,
            int jobTotalSkills) {

        // Tính tổng weight của kỹ năng trùng khớp
        double matchedWeight = matchedSkillIds.stream()
                .mapToDouble(skillId -> {
                    Integer cvProf = cvSkillProficiency.getOrDefault(skillId, 0);
                    Integer jobReq = jobSkillRequirements.getOrDefault(skillId, 0);
                    // Weight = min(proficiency, requirement) / 5 để normalize
                    int minLevel = Math.min(cvProf, jobReq);
                    return minLevel / 5.0;
                })
                .sum();

        // Tính norm vectors
        double cvVectorNorm = calculateVectorNorm(cvSkillProficiency.values());
        double jobVectorNorm = calculateVectorNorm(jobSkillRequirements.values());

        // Tính cosine similarity
        if (cvVectorNorm == 0 || jobVectorNorm == 0) {
            return 0.0;
        }

        double cosineSimilarity = matchedWeight / (cvVectorNorm * jobVectorNorm);

        // Normalize: bổ sung penalty cho kỹ năng thiếu và bonus cho trùng khớp
        double penalty = 1.0 - (matchedSkillIds.size() / (double) Math.max(cvTotalSkills, jobTotalSkills));
        double bonus = matchedSkillIds.size() / (double) jobTotalSkills;

        // Final score = (cosine_similarity + bonus) * penalty
        double finalScore = Math.min(1.0, (cosineSimilarity + bonus) * (1.0 - penalty * 0.3));

        return Math.max(0.0, finalScore);
    }

    /**
     * Tính L2 norm của vector
     */
    private double calculateVectorNorm(Collection<Integer> values) {
        return Math.sqrt(
                values.stream()
                        .mapToDouble(v -> Math.pow(v / 5.0, 2))
                        .sum()
        );
    }

    @Override
    public String getStrategyName() {
        return "Cosine Similarity with Proficiency Weighting";
    }

}