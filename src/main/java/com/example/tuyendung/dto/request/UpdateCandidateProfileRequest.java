package com.example.tuyendung.dto.request;

import com.example.tuyendung.entity.enums.GioiTinh;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateCandidateProfileRequest {
    @NotBlank(message = "Họ tên không được để trống")
    private String hoTen;

    private String soDienThoai;
    private GioiTinh gioiTinh;
    private LocalDate ngaySinh;
}
