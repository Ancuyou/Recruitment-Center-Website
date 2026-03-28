package com.example.tuyendung.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardCandidateResponse {
    private long tongSoDonDaNop;
    private long soDonDangCho;
    private long soLichPhongVan;
    private long soThongBaoChuaDoc;
}
