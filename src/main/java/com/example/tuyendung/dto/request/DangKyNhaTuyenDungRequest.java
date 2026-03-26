package com.example.tuyendung.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DangKyNhaTuyenDungRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String matKhau;

    @NotBlank(message = "Họ tên không được để trống")
    private String hoTen;

    @NotBlank(message = "Chức vụ không được để trống")
    private String chucVu;

    private String soDienThoai;

    @NotBlank(message = "Mã số thuế công ty không được để trống")
    private String maSoThue;

    @NotBlank(message = "Tên công ty không được để trống")
    private String tenCongTy;

    private String nganhNghe;

    private String website;
}