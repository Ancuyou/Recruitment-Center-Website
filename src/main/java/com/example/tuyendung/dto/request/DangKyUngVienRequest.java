package com.example.tuyendung.dto.request;

import com.example.tuyendung.entity.enums.GioiTinh;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DangKyUngVienRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String matKhau;

    @NotBlank(message = "Họ tên không được để trống")
    private String hoTen;

    @Pattern(regexp = "^\\d{10,11}$", message = "Số điện thoại không hợp lệ")
    private String soDienThoai;

    private LocalDate ngaySinh;

    private GioiTinh gioiTinh;
}