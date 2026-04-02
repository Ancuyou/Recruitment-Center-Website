package com.example.tuyendung.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho response kỹ năng Job
 * SOLID: Single Responsibility - chỉ xử lý output data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSkillResponse {

    private Long id;
    private Long jobId;
    private Long kyNangId;
    private String tenKyNang;
    private Integer yeucau;
    private String moTa;
    private LocalDateTime ngayTao;

    /**
     * Helper method: lấy tên mức yêu cầu
     */
    public String getYeucauName() {
        return switch (yeucau) {
            case 1 -> "Sơ cấp";
            case 2 -> "Cơ bản";
            case 3 -> "Trung bình";
            case 4 -> "Nâng cao";
            case 5 -> "Chuyên gia";
            default -> "Không xác định";
        };
    }
}