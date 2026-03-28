package com.example.tuyendung.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho response kỹ năng CV
 * SOLID: Single Responsibility - chỉ xử lý output data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CvSkillResponse {

    private Long id;
    private Long cvId;
    private Long kyNangId;
    private String tenKyNang;
    private Integer mucThanhThao;
    private String moTa;
    private Long ngayTao;

    /**
     * Helper method: lấy tên mức thành thạo
     */
    public String getMucThanhThaoName() {
        return switch (mucThanhThao) {
            case 1 -> "Sơ cấp";
            case 2 -> "Cơ bản";
            case 3 -> "Trung bình";
            case 4 -> "Nâng cao";
            case 5 -> "Chuyên gia";
            default -> "Không xác định";
        };
    }
}