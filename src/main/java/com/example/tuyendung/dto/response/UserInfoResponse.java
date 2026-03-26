package com.example.tuyendung.dto.response;

import com.example.tuyendung.entity.enums.GioiTinh;
import com.example.tuyendung.entity.enums.VaiTroTaiKhoan;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    private Long taiKhoanId;
    private String email;
    private VaiTroTaiKhoan vaiTro;
    private String hoTen;
    private String soDienThoai;
    private LocalDate ngaySinh;
    private GioiTinh gioiTinh;
    private String anhDaiDien;
    private String tenCongTy;
    private String chucVu;
}