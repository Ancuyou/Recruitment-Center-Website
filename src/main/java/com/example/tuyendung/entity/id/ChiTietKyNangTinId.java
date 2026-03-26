package com.example.tuyendung.entity.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class ChiTietKyNangTinId implements Serializable {

    @Column(name = "tin_tuyen_dung_id")
    private Long tinTuyenDungId;

    @Column(name = "ky_nang_id")
    private Long kyNangId;
}

