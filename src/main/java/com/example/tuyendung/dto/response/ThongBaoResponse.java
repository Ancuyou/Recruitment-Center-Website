package com.example.tuyendung.dto.response;

import com.example.tuyendung.entity.enums.LoaiThongBao;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ThongBaoResponse {
    private Long id;
    private String tieuDe;
    private String noiDung;
    private LoaiThongBao loaiThongBao;
    private String lienKet;
    private Boolean daDoc;
    private LocalDateTime ngayTao;
}
