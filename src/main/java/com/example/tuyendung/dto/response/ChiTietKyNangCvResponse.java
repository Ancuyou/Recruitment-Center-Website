package com.example.tuyendung.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietKyNangCvResponse {

    private Long kyNangId;
    private String tenKyNang;
    private Integer mucThanhThao;

    // Hàm để lấy tên mức thành thạo
    public String getMucThanhThaoName() {
        return switch (mucThanhThao) {
            case 1 -> "Cơ bản";
            case 2 -> "Trung bình";
            case 3 -> "Tốt";
            case 4 -> "Rất tốt";
            case 5 -> "Chuyên gia";
            default -> "Không xác định";
        };
    }
}
