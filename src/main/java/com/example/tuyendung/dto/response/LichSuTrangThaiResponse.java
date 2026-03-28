package com.example.tuyendung.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LichSuTrangThaiResponse {
    private Long id;
    private Long donUngTuyenId;
    private String nguoiThucHien;
    private String vaiTro;
    private Integer trangThaiCu;
    private String trangThaiCuLabel;
    private Integer trangThaiMoi;
    private String trangThaiMoiLabel;
    private String ghiChu;
    private LocalDateTime thoiGianChuyen;
}
