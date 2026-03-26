package com.example.tuyendung.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@Table(name = "nha_tuyen_dung")
public class NhaTuyenDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tai_khoan_id", nullable = false, unique = true)
    private TaiKhoan taiKhoan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cong_ty_id", nullable = false)
    private CongTy congTy;

    @Column(name = "ho_ten", nullable = false, length = 150)
    private String hoTen;

    @Column(name = "chuc_vu", length = 100)
    private String chucVu;

    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @Column(name = "ngay_tao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat", insertable = false, updatable = false)
    private LocalDateTime ngayCapNhat;

    @OneToMany(mappedBy = "nhaTuyenDung")
    private Set<TinTuyenDung> tinTuyenDungs = new HashSet<>();

    @OneToMany(mappedBy = "nguoiPhongVan")
    private Set<LichPhongVan> lichPhongVans = new HashSet<>();
}

