package com.example.tuyendung.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "lich_su_trang_thai")
public class LichSuTrangThai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "don_ung_tuyen_id", nullable = false)
    private DonUngTuyen donUngTuyen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_thuc_hien_id", nullable = false)
    private TaiKhoan nguoiThucHien;

    @Column(name = "trang_thai_cu")
    private Integer trangThaiCu;

    @Column(name = "trang_thai_moi", nullable = false)
    private Integer trangThaiMoi;

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;

    @Column(name = "thoi_gian_chuyen", insertable = false, updatable = false)
    private LocalDateTime thoiGianChuyen;
}

