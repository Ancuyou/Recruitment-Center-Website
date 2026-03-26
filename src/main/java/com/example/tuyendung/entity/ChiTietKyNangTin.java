package com.example.tuyendung.entity;

import com.example.tuyendung.entity.id.ChiTietKyNangTinId;
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
@Table(name = "chi_tiet_ky_nang_tin")
public class ChiTietKyNangTin {

    @EmbeddedId
    private ChiTietKyNangTinId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tinTuyenDungId")
    @JoinColumn(name = "tin_tuyen_dung_id", nullable = false)
    private TinTuyenDung tinTuyenDung;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("kyNangId")
    @JoinColumn(name = "ky_nang_id", nullable = false)
    private KyNang kyNang;

    @Column(name = "muc_yeu_cau", nullable = false)
    private Integer mucYeuCau;

    @Column(name = "ngay_tao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;
}

