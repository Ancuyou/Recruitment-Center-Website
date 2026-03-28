package com.example.tuyendung.entity;

import com.example.tuyendung.entity.enums.CapBacYeuCau;
import com.example.tuyendung.entity.enums.HinhThucLamViec;
import com.example.tuyendung.entity.enums.KhuVucEnum;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity cho bảng tin_tuyen_dung
 * Khu vực áp dụng được lưu dạng @ElementCollection (không cần bảng entity riêng)
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tin_tuyen_dung")
public class TinTuyenDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nha_tuyen_dung_id", nullable = false)
    private NhaTuyenDung nhaTuyenDung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cong_ty_id", nullable = false)
    private CongTy congTy;

    @Column(name = "tieu_de", nullable = false, length = 255)
    private String tieuDe;

    @Column(name = "mo_ta_cong_viec", nullable = false, columnDefinition = "TEXT")
    private String moTaCongViec;

    @Column(name = "yeu_cau_ung_vien", nullable = false, columnDefinition = "TEXT")
    private String yeuCauUngVien;

    @Column(name = "muc_luong_min", precision = 10, scale = 2)
    private BigDecimal mucLuongMin;

    @Column(name = "muc_luong_max", precision = 10, scale = 2)
    private BigDecimal mucLuongMax;

    @Column(name = "dia_diem", length = 100)
    private String diaDiem;

    @Enumerated(EnumType.STRING)
    @Column(name = "cap_bac_yeu_cau", nullable = false)
    private CapBacYeuCau capBacYeuCau = CapBacYeuCau.JUNIOR;

    @Enumerated(EnumType.STRING)
    @Column(name = "hinh_thuc_lam_viec", nullable = false)
    private HinhThucLamViec hinhThucLamViec = HinhThucLamViec.OFFICE;

    @Column(name = "han_nop", nullable = false)
    private LocalDate hanNop;

    @Column(name = "trang_thai")
    private Integer trangThai = 1;

    @Column(name = "ngay_tao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat", insertable = false, updatable = false)
    private LocalDateTime ngayCapNhat;

    /**
     * Khu vực áp dụng — lưu dạng String vào bảng ct_kv_tin
     * Không cần entity KhuVuc hay repository riêng
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "ct_kv_tin",
            joinColumns = @JoinColumn(name = "tin_tuyen_dung_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "khu_vuc", length = 30)
    private Set<KhuVucEnum> khuVucs = new HashSet<>();

    @OneToMany(mappedBy = "tinTuyenDung")
    private Set<ChiTietKyNangTin> chiTietKyNangTins = new HashSet<>();

    @OneToMany(mappedBy = "tinTuyenDung")
    private Set<DonUngTuyen> donUngTuyens = new HashSet<>();
}
