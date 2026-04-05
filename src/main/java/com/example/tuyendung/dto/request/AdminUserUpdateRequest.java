package com.example.tuyendung.dto.request;

import com.example.tuyendung.entity.enums.GioiTinh;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AdminUserUpdateRequest {

    @Email(message = "Email không hợp lệ")
    private String email;

    @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự")
    private String matKhauMoi;

    // Shared profile fields
    private String hoTen;
    private String soDienThoai;

    // Candidate fields
    private LocalDate ngaySinh;
    private GioiTinh gioiTinh;

    // Recruiter fields
    private String chucVu;
    private Long congTyId;
}
