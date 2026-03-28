package com.example.tuyendung.dto.response;

import com.example.tuyendung.entity.enums.CapBacYeuCau;
import com.example.tuyendung.entity.enums.HinhThucLamViec;
import com.example.tuyendung.entity.enums.KhuVucEnum;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response DTO cho tin tuyển dụng (B14, B15)
 */
@Getter
@Builder
public class TinTuyenDungResponse {

    private Long id;
    private Long nhaTuyenDungId;
    private String tenNhaTuyenDung;
    private Long congTyId;
    private String tenCongTy;
    private String logoUrl;

    private String tieuDe;
    private String moTaCongViec;
    private String yeuCauUngVien;
    private BigDecimal mucLuongMin;
    private BigDecimal mucLuongMax;
    private String diaDiem;
    private CapBacYeuCau capBacYeuCau;
    private HinhThucLamViec hinhThucLamViec;
    private LocalDate hanNop;

    private String trangThaiLabel;
    private Integer trangThai;

    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;

    /** Khu vực áp dụng (enum values) */
    private Set<KhuVucEnum> khuVucs;

    /** Số lượng đơn ứng tuyển */
    private long soLuongDon;
}
