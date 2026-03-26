package com.example.tuyendung.entity;

import com.example.tuyendung.entity.enums.HinhThucPhongVan;
import com.example.tuyendung.entity.enums.TrangThaiPhongVan;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "lich_phong_van")
public class LichPhongVan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "don_ung_tuyen_id", nullable = false)
    private DonUngTuyen donUngTuyen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_phong_van_id", nullable = false)
    private NhaTuyenDung nguoiPhongVan;

    @Column(name = "tieu_de_vong", nullable = false, length = 150)
    private String tieuDeVong;

    @Column(name = "thoi_gian_bat_dau", nullable = false)
    private LocalDateTime thoiGianBatDau;

    @Column(name = "thoi_gian_ket_thuc", nullable = false)
    private LocalDateTime thoiGianKetThuc;

    @Enumerated(EnumType.STRING)
    @Column(name = "hinh_thuc", nullable = false)
    private HinhThucPhongVan hinhThuc;

    @Column(name = "dia_diem_hoac_link", length = 255)
    private String diaDiemHoacLink;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai_phong_van")
    private TrangThaiPhongVan trangThaiPhongVan = TrangThaiPhongVan.CHO_PHONG_VAN;

    @Column(name = "ngay_tao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;
}

