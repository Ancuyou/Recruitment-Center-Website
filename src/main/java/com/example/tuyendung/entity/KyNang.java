package com.example.tuyendung.entity;

import com.example.tuyendung.entity.id.ChiTietKyNangTin;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "ky_nang")
public class KyNang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_ky_nang", nullable = false, unique = true, length = 100)
    private String tenKyNang;

    @Column(name = "ngay_tao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;

    @OneToMany(mappedBy = "kyNang")
    private Set<ChiTietKyNangTin> chiTietKyNangTins = new HashSet<>();

    @OneToMany(mappedBy = "kyNang")
    private Set<ChiTietKyNangCv> chiTietKyNangCvs = new HashSet<>();
}

