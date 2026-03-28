package com.example.tuyendung.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardRecruiterResponse {
    private long tongSoTinDangMo;
    private long tongSoDonUngTuyen;
    private long soDonMoi;
    private long soLichPhongVanSapToi;
}
