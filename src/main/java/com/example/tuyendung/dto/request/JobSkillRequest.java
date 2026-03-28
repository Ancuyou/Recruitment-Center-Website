package com.example.tuyendung.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho thêm/cập nhật kỹ năng vào Job
 * SOLID: Single Responsibility - chỉ xử lý input validation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSkillRequest {

    @NotNull(message = "ID kỹ năng không được để trống")
    @Positive(message = "ID kỹ năng phải > 0")
    private Long kyNangId;

    @NotNull(message = "Yêu cầu không được để trống")
    @Min(value = 1, message = "Yêu cầu phải từ 1 đến 5")
    @Max(value = 5, message = "Yêu cầu phải từ 1 đến 5")
    private Integer yeucau;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String moTa;

}