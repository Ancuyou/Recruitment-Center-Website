package com.example.tuyendung.entity;

import com.example.tuyendung.entity.enums.NganhNgheEnum;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity cho bảng cong_ty
 * Ngành nghề được lưu dạng @ElementCollection (không cần bảng entity riêng)
 */
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

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "ngay_tao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat", insertable = false, updatable = false)
    private LocalDateTime ngayCapNhat;

    /**
     * Danh sách ngành nghề — lưu dạng String vào bảng ct_cty_nganh
     * Không cần entity NganhNghe hay repository riêng
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "ct_cty_nganh",
            joinColumns = @JoinColumn(name = "cong_ty_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "nganh_nghe", length = 50)
    private Set<NganhNgheEnum> nganhNghes = new HashSet<>();

    @OneToMany(mappedBy = "congTy")
    private Set<NhaTuyenDung> nhaTuyenDungs = new HashSet<>();

    @OneToMany(mappedBy = "congTy")
    private Set<TinTuyenDung> tinTuyenDungs = new HashSet<>();
}
