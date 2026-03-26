package com.example.tuyendung.entity;

import com.example.tuyendung.entity.enums.GioiTinh;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ung_vien")
public class UngVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tai_khoan_id", nullable = false, unique = true)
    private TaiKhoan taiKhoan;

    @Column(name = "ho_ten", nullable = false, length = 150)
    private String hoTen;

    @Column(name = "so_dien_thoai", unique = true, length = 20)
    private String soDienThoai;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Enumerated(EnumType.STRING)
    @Column(name = "gioi_tinh")
    private GioiTinh gioiTinh;

    @Column(name = "anh_dai_dien", length = 255)
    private String anhDaiDien;

    @Column(name = "ngay_tao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat", insertable = false, updatable = false)
    private LocalDateTime ngayCapNhat;

    @OneToMany(mappedBy = "ungVien")
    private Set<HoSoCv> hoSoCvs = new HashSet<>();
}

