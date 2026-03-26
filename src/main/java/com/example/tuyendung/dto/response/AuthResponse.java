package com.example.tuyendung.dto.response;

import com.example.tuyendung.entity.enums.VaiTroTaiKhoan;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private Long taiKhoanId;
    private String email;
    private VaiTroTaiKhoan vaiTro;
    private String accessToken;
    private String refreshToken;
    private UserInfoDTO userInfo;

    @Data
    @Builder
    public static class UserInfoDTO {
        private Long id;
        private String hoTen;
        private String soDienThoai;
        private String anhDaiDien;
        private String tenCongTy;  // Chỉ có cho NhaTuyenDung
    }
}