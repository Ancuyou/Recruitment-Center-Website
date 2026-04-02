package com.example.tuyendung.repository;

import com.example.tuyendung.entity.CongTy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CongTyRepository extends JpaRepository<CongTy, Long> {

    Optional<CongTy> findByMaSoThue(String maSoThue);

    boolean existsByMaSoThue(String maSoThue);

    /**
     * Đếm số tin đang mở (trangThai=1) của một công ty — dùng thay vì lazy-load toàn collection
     * so sánh theo enum TrangThaiTin.MO để khớp với kiểu của entity
     */
    @Query("SELECT COUNT(t) FROM TinTuyenDung t WHERE t.congTy.id = :congTyId AND t.trangThai = com.example.tuyendung.entity.enums.TrangThaiTin.MO")
    long countActiveJobsByCongTyId(@Param("congTyId") Long congTyId);
}
