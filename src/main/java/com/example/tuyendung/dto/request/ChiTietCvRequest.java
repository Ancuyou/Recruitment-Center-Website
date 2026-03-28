package com.example.tuyendung.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietCvRequest {

    @NotNull(message = "Loại bản ghi không được để trống")
    @Min(value = 1, message = "Loại bản ghi không hợp lệ")
    @Max(value = 3, message = "Loại bản ghi không hợp lệ")
    private Integer loaiBanGhi; // 1: Học vấn, 2: Kinh nghiệm làm việc, 3: Chứng chỉ

    @NotBlank(message = "Tên tổ chức không được để trống")
    @Size(min = 1, max = 200, message = "Tên tổ chức phải từ 1 đến 200 ký tự")
    private String tenToChuc;

    @Size(max = 200, message = "Chuyên ngành/vị trí tối đa 200 ký tự")
    private String chuyenNganhHoacViTri;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @JsonProperty("ngayBatDau")
    private LocalDate ngayBatDau;

    @JsonProperty("ngayKetThuc")
    private LocalDate ngayKetThuc;

    @Size(max = 2000, message = "Mô tả chi tiết tối đa 2000 ký tự")
    private String moTaChiTiet;
}
