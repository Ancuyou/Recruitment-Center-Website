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
@Table(name = "ho_so_cv")
public class HoSoCv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ung_vien_id", nullable = false)
    private UngVien ungVien;

    @Column(name = "tieu_de_cv", nullable = false, length = 100)
    private String tieuDeCv;

    @Column(name = "muc_tieu_nghe_nghiep", columnDefinition = "TEXT")
    private String mucTieuNgheNghiep;

    @Column(name = "file_cv_url", length = 255)
    private String fileCvUrl;

    @Column(name = "la_cv_chinh")
    private Boolean laCvChinh = false;

    @Column(name = "da_xoa")
    private Boolean daXoa = false;

    @Column(name = "ngay_tao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat", insertable = false, updatable = false)
    private LocalDateTime ngayCapNhat;

    @OneToMany(mappedBy = "hoSoCv")
    private Set<ChiTietKyNangCv> chiTietKyNangCvs = new HashSet<>();

    @OneToMany(mappedBy = "hoSoCv")
    private Set<ChiTietCv> chiTietCvs = new HashSet<>();

    @OneToMany(mappedBy = "hoSoCv")
    private Set<DonUngTuyen> donUngTuyens = new HashSet<>();
}

