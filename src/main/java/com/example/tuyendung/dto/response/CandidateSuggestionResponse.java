package com.example.tuyendung.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho gợi ý ứng viên (E1)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateSuggestionResponse {

    private Long ungVienId;
    private String hoTen;
    private String email;
    private String soDienThoai;
    private Long cvId;
    private String tieuDeCv;
    private Double matchScore; // 0-1.0
    private Integer matchPercentage; // 0-100%
    private String matchLevel; // Perfect, Excellent, Good, Fair, Poor
    private Integer skillMatchCount; // Số kỹ năng trùng
    private Integer totalSkillsRequired; // Tổng kỹ năng yêu cầu
    private Long calculatedAt;

}