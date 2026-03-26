package com.example.tuyendung.entity;

import com.example.tuyendung.entity.enums.VaiTroTaiKhoan;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor  // ← THÊM CÁI NÀY
@Builder             // ← THÊM CÁI NÀY
@Entity
@Table(name = "tai_khoan")
public class TaiKhoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "mat_khau_hash", nullable = false, length = 255)
    private String matKhauHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "vai_tro", nullable = false)
    private VaiTroTaiKhoan vaiTro;

    @Column(name = "la_kich_hoat")
    @Builder.Default
    private Boolean laKichHoat = true;

    @CreationTimestamp
    @Column(name = "ngay_tao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;

    @UpdateTimestamp
    @Column(name = "ngay_cap_nhat", insertable = false, updatable = false)
    private LocalDateTime ngayCapNhat;

    @OneToOne(mappedBy = "taiKhoan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UngVien ungVien;

    @OneToOne(mappedBy = "taiKhoan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private NhaTuyenDung nhaTuyenDung;

    @OneToMany(mappedBy = "nguoiThucHien", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<LichSuTrangThai> lichSuTrangThais = new HashSet<>();
}