package com.example.tuyendung.dto.request;

import com.example.tuyendung.entity.enums.GioiTinh;
import com.example.tuyendung.entity.enums.VaiTroTaiKhoan;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AdminUserCreateRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String matKhau;

    @NotNull(message = "Vai trò không được để trống")
    private VaiTroTaiKhoan vaiTro;

    private Boolean laKichHoat;

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
