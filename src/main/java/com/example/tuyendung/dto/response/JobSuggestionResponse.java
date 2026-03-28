package com.example.tuyendung.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho gợi ý công việc (E2)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSuggestionResponse {

    private Long jobId;
    private String tenVitri;
    private String moTa;
    private String congTyId;
    private String congTyName;
    private Double matchScore; // 0-1.0
    private Integer matchPercentage; // 0-100%
    private String matchLevel; // Perfect, Excellent, Good, Fair, Poor
    private Integer skillMatchCount; // Số kỹ năng trùng
    private Integer totalSkillsInCv; // Tổng kỹ năng của CV
    private String diaDiem;
    private Long calculatedAt;

}