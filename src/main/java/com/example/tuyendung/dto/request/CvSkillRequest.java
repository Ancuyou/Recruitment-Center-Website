package com.example.tuyendung.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho thêm/cập nhật kỹ năng vào CV
 * SOLID: Single Responsibility - chỉ xử lý input validation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CvSkillRequest {

    @NotNull(message = "ID kỹ năng không được để trống")
    @Positive(message = "ID kỹ năng phải > 0")
    private Long kyNangId;

    @NotNull(message = "Mức thành thạo không được để trống")
    @Min(value = 1, message = "Mức thành thạo phải từ 1 đến 5")
    @Max(value = 5, message = "Mức thành thạo phải từ 1 đến 5")
    private Integer mucThanhThao;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String moTa;

} 