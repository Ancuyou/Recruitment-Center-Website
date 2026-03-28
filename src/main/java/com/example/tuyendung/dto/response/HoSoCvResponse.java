package com.example.tuyendung.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoSoCvResponse {

    private Long id;
    private String tieuDeCv;
    private String mucTieuNgheNghiep;
    private String fileCvUrl;
    private Boolean laCvChinh;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private List<ChiTietCvResponse> chiTietCvs;
    private List<ChiTietKyNangCvResponse> kyNangs;
}
