package com.example.tuyendung.dto.response;

import com.example.tuyendung.entity.enums.GioiTinh;
import com.example.tuyendung.entity.enums.VaiTroTaiKhoan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {
    private Long taiKhoanId;
    private String email;
    private VaiTroTaiKhoan vaiTro;
    private Boolean laKichHoat;

    private String hoTen;
    private String soDienThoai;
    private LocalDate ngaySinh;
    private GioiTinh gioiTinh;

    private Long congTyId;
    private String tenCongTy;
    private String chucVu;

    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
}
