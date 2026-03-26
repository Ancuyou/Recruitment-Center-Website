package com.example.tuyendung.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cong_ty")
public class CongTy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_cong_ty", nullable = false, length = 255)
    private String tenCongTy;

    @Column(name = "ma_so_thue", unique = true, length = 50)
    private String maSoThue;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Column(name = "nganh_nghe", length = 100)
    private String nganhNghe;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "ngay_tao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat", insertable = false, updatable = false)
    private LocalDateTime ngayCapNhat;

    @OneToMany(mappedBy = "congTy")
    private Set<NhaTuyenDung> nhaTuyenDungs = new HashSet<>();

    @OneToMany(mappedBy = "congTy")
    private Set<TinTuyenDung> tinTuyenDungs = new HashSet<>();
}

