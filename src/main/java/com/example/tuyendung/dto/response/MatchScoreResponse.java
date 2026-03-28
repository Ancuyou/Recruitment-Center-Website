package com.example.tuyendung.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho matching score - kết quả tính điểm match
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchScoreResponse {

    private Long cvId;
    private Long jobId;
    private Double matchScore; // 0-1.0 (0% - 100%)
    private Integer matchPercentage; // 0-100%
    private String matchLevel; // "Perfect", "Excellent", "Good", "Fair", "Poor"
    private List<String> matchedSkills; // Kỹ năng trùng khớp
    private List<String> missingSkills; // Kỹ năng thiếu
    private Long calculatedAt;

    /**
     * Helper method: xác định mức độ match
     */
    public static String determineMatchLevel(Double score) {
        if (score >= 0.9) return "Perfect";
        if (score >= 0.7) return "Excellent";
        if (score >= 0.5) return "Good";
        if (score >= 0.3) return "Fair";
        return "Poor";
    }
}
