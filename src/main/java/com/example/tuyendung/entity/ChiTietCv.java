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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "chi_tiet_cv")
public class ChiTietCv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ho_so_cv_id", nullable = false)
    private HoSoCv hoSoCv;

    @Column(name = "loai_ban_ghi", nullable = false)
    private Integer loaiBanGhi;

    @Column(name = "ten_to_chuc", nullable = false, length = 200)
    private String tenToChuc;

    @Column(name = "chuyen_nganh_hoac_vi_tri", length = 200)
    private String chuyenNganhHoacViTri;

    @Column(name = "ngay_bat_dau", nullable = false)
    private LocalDate ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDate ngayKetThuc;

    @Column(name = "mo_ta_chi_tiet", columnDefinition = "TEXT")
    private String moTaChiTiet;

    @Column(name = "ngay_tao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;
}

