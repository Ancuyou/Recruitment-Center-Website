package com.example.tuyendung.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateRecruiterProfileRequest {
    @NotBlank(message = "Họ tên không được để trống")
    private String hoTen;

    private String soDienThoai;
    private String chucVu;
}
