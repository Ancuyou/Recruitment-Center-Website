package com.example.tuyendung.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DonUngTuyenResponse {
    private Long id;
    
    // Tin Tuyen Dung info
    private Long tinTuyenDungId;
    private String tieuDeTin;
    private String tenCongTy;
    private String logoCongTy;
    
    // Ung Vien info
    private Long ungVienId;
    private String tenUngVien;
    private String emailUngVien;
    
    // CV info
    private Long hoSoCvId;
    private String cvUrl;
    
    // App info
    private String thuNgo;
    private Integer trangThai;
    private String trangThaiLabel;
    private LocalDateTime ngayNop;
}
