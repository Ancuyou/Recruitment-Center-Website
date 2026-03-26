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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
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

    @Column(name = "ngay_tao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;
}

