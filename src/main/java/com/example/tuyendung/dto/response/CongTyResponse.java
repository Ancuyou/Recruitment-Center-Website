package com.example.tuyendung.dto.response;

import com.example.tuyendung.entity.enums.NganhNgheEnum;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response DTO cho thông tin công ty (B2, B3)
 */
@Getter
@Builder
public class CongTyResponse {

    private Long id;
    private String tenCongTy;
    private String maSoThue;
    private String logoUrl;
    private String website;
    private String moTa;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;

    /** Danh sách ngành nghề (enum values) */
    private Set<NganhNgheEnum> nganhNghes;

    /** Số lượng tin tuyển dụng đang mở */
    private long soTinDangMo;
}
