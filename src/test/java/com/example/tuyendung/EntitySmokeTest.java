package com.example.tuyendung;

import com.example.tuyendung.entity.TaiKhoan;
import com.example.tuyendung.entity.enums.VaiTroTaiKhoan;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EntitySmokeTest {

    @Test
    void canInstantiateEntity() {
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setEmail("test@example.com");
        taiKhoan.setVaiTro(VaiTroTaiKhoan.UNG_VIEN);

        Assertions.assertEquals("test@example.com", taiKhoan.getEmail());
        Assertions.assertEquals(VaiTroTaiKhoan.UNG_VIEN, taiKhoan.getVaiTro());
    }
}

