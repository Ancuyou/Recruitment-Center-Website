package com.example.tuyendung.repository;

import com.example.tuyendung.entity.LichSuTrangThai;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichSuTrangThaiRepository extends JpaRepository<LichSuTrangThai, Long> {

    /**
     * Find all status history for an application (non-paginated).
     * Used when fetching all history without pagination.
     */
    @Query("SELECT l FROM LichSuTrangThai l JOIN FETCH l.nguoiThucHien WHERE l.donUngTuyen.id = :donUngTuyenId ORDER BY l.thoiGianChuyen DESC")
    List<LichSuTrangThai> findByDonUngTuyenIdOrderByThoiGianChuyenDesc(@Param("donUngTuyenId") Long donUngTuyenId);

    /**
     * Find paginated status history (D8 – optimized for DB-level pagination).
     * Uses Pageable for database-level offset/limit instead of loading all records.
     */
    @Query("SELECT l FROM LichSuTrangThai l JOIN FETCH l.nguoiThucHien WHERE l.donUngTuyen.id = :donUngTuyenId ORDER BY l.thoiGianChuyen DESC")
    Page<LichSuTrangThai> findByDonUngTuyenIdPageable(@Param("donUngTuyenId") Long donUngTuyenId, Pageable pageable);
}
