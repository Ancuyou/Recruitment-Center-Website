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
@Table(name = "don_ung_tuyen")
public class DonUngTuyen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tin_tuyen_dung_id", nullable = false)
    private TinTuyenDung tinTuyenDung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ho_so_cv_id", nullable = false)
    private HoSoCv hoSoCv;

    @Column(name = "ban_sao_cv_url", length = 255)
    private String banSaoCvUrl;

    @Column(name = "thu_ngo", columnDefinition = "TEXT")
    private String thuNgo;

    @Column(name = "trang_thai_hien_tai")
    private Integer trangThaiHienTai = 1;

    @Column(name = "ngay_nop", insertable = false, updatable = false)
    private LocalDateTime ngayNop;

    @OneToMany(mappedBy = "donUngTuyen")
    private Set<LichSuTrangThai> lichSuTrangThais = new HashSet<>();

    @OneToMany(mappedBy = "donUngTuyen")
    private Set<LichPhongVan> lichPhongVans = new HashSet<>();
}

