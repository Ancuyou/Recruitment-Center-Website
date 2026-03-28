package com.example.tuyendung.strategy;

import com.example.tuyendung.dto.response.MatchScoreResponse;
import com.example.tuyendung.util.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Simple Keyword Matching Strategy
 * 
 * Algorithm:
 * - Score = (matched_skills / total_job_skills) * 100%
 * - Simple percentage-based matching
 * - Không xem xét mức độ thành thạo
 * 
 * Ưu điểm:
 * - Dễ hiểu, nhanh tính toán
 * - Tốt cho initial filtering
 * - Fallback strategy
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KeywordMatchingStrategy implements SkillMatchingStrategy {

    private final TimeProvider timeProvider;

    @Override
    public MatchScoreResponse calculateMatchScore(
            List<Long> cvSkillIds,
            Map<Long, Integer> cvSkillProficiency,
            List<Long> jobSkillIds,
            Map<Long, Integer> jobSkillRequirements) {

        log.info("Calculating match score using Keyword Matching - CV: {} skills, Job: {} skills",
                cvSkillIds.size(), jobSkillIds.size());

        // Tìm các kỹ năng trùng khớp
        Set<Long> cvSkillSet = new HashSet<>(cvSkillIds);
        Set<Long> jobSkillSet = new HashSet<>(jobSkillIds);
        Set<Long> matchedSkillIds = new HashSet<>(cvSkillSet);
        matchedSkillIds.retainAll(jobSkillSet);

        // Tìm các kỹ năng thiếu
        Set<Long> missingSkillIds = new HashSet<>(jobSkillSet);
        missingSkillIds.removeAll(cvSkillSet);

        // Tính match score đơn giản: matched / total_job_skills
        double matchScore = jobSkillIds.isEmpty() 
                ? 0.0 
                : (double) matchedSkillIds.size() / jobSkillIds.size();

        int matchPercentage = (int) (matchScore * 100);
        String matchLevel = MatchScoreResponse.determineMatchLevel(matchScore);

        return MatchScoreResponse.builder()
                .matchScore(Math.round(matchScore * 100.0) / 100.0)
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

    @Override
    public String getStrategyName() {
        return "Simple Keyword Matching";
    }

}