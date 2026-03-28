package com.example.tuyendung.repository;

import com.example.tuyendung.entity.ChiTietKyNangCv;
import com.example.tuyendung.entity.id.ChiTietKyNangCvId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChiTietKyNangCvRepository extends JpaRepository<ChiTietKyNangCv, ChiTietKyNangCvId> {

    /**
     * Tìm tất cả kỹ năng của một CV (only from non-deleted CVs)
     */
    @Query("SELECT ck FROM ChiTietKyNangCv ck WHERE ck.hoSoCv.id = :hoSoCvId AND ck.hoSoCv.daXoa = false ORDER BY ck.ngayTao DESC")
    List<ChiTietKyNangCv> findByHoSoCvId(Long hoSoCvId);

    /**
     * Tìm một kỹ năng cụ thể trong một CV (only from non-deleted CVs)
     */
    @Query("SELECT ck FROM ChiTietKyNangCv ck WHERE ck.hoSoCv.id = :hoSoCvId AND ck.kyNang.id = :kyNangId AND ck.hoSoCv.daXoa = false")
    Optional<ChiTietKyNangCv> findByHoSoCvIdAndKyNangId(Long hoSoCvId, Long kyNangId);

    /**
     * Xóa tất cả kỹ năng của một CV
     */
    void deleteByHoSoCvId(Long hoSoCvId);
}
