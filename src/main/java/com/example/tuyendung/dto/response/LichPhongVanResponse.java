package com.example.tuyendung.dto.response;

import com.example.tuyendung.entity.enums.HinhThucPhongVan;
import com.example.tuyendung.entity.enums.TrangThaiPhongVan;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LichPhongVanResponse {
    private Long id;
    private Long donUngTuyenId;
    private String tieuDeTin;
    private String tenUngVien;
    
    // Nguoi phong van
    private Long nguoiPhongVanId;
    private String tenNguoiPhongVan;
    
    private String tieuDeVong;
    private LocalDateTime thoiGianBatDau;
    private LocalDateTime thoiGianKetThuc;
    
    private HinhThucPhongVan hinhThuc;
    private String diaDiemHoacLink;
    
    private TrangThaiPhongVan trangThai;
    private LocalDateTime ngayTao;
}
