package com.example.tuyendung.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietCvResponse {

    private Long id;
    private Integer loaiBanGhi;
    private String tenToChuc;
    private String chuyenNganhHoacViTri;

    @JsonProperty("ngayBatDau")
    private LocalDate ngayBatDau;

    @JsonProperty("ngayKetThuc")
    private LocalDate ngayKetThuc;

    private String moTaChiTiet;
    private LocalDateTime ngayTao;

    // Hàm để lấy tên loại bản ghi
    public String getLoaiBanGhiName() {
        return switch (loaiBanGhi) {
            case 1 -> "Học vấn";
            case 2 -> "Kinh nghiệm làm việc";
            case 3 -> "Chứng chỉ";
            default -> "Khác";
        };
    }
}
