package com.example.tuyendung.entity;

import com.example.tuyendung.entity.id.ChiTietKyNangCvId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chi_tiet_ky_nang_cv")
public class ChiTietKyNangCv {

    @EmbeddedId
    private ChiTietKyNangCvId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("hoSoCvId")
    @JoinColumn(name = "ho_so_cv_id", nullable = false)
    private HoSoCv hoSoCv;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("kyNangId")
    @JoinColumn(name = "ky_nang_id", nullable = false)
    private KyNang kyNang;

    @Column(name = "muc_thanh_thao", nullable = false)
    private Integer mucThanhThao;

    @Column(name = "mo_ta", length = 500)
    private String moTa;

    @Column(name = "ngay_tao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private Long ngayCapNhat;

    @Column(name = "da_xoa", nullable = false)
    @Builder.Default
    private Boolean daXoa = false;
}

