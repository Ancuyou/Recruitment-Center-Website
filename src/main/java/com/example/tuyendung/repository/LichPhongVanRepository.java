package com.example.tuyendung.repository;

import com.example.tuyendung.entity.LichPhongVan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichPhongVanRepository extends JpaRepository<LichPhongVan, Long> {

    /**
     * D10: Lấy tất cả lịch phỏng vấn của một đơn ứng tuyển.
     * JOIN FETCH đầy đủ để mapToResponse không gây N+1:
     *   l.nguoiPhongVan, l.donUngTuyen, d.tinTuyenDung, d.hoSoCv, cv.ungVien
     */
    @Query("""
            SELECT l FROM LichPhongVan l
            JOIN FETCH l.nguoiPhongVan ntd
            JOIN FETCH l.donUngTuyen d
            JOIN FETCH d.tinTuyenDung t
            JOIN FETCH d.hoSoCv cv
            JOIN FETCH cv.ungVien uv
            WHERE l.donUngTuyen.id = :donUngTuyenId
            ORDER BY l.thoiGianBatDau ASC
            """)
    List<LichPhongVan> findByDonUngTuyenId(@Param("donUngTuyenId") Long donUngTuyenId);

    /**
     * Lấy các lịch phỏng vấn đang diễn ra hoặc sắp tới của một Nhà Tuyển Dụng
     */
    @Query("SELECT l FROM LichPhongVan l JOIN FETCH l.donUngTuyen d JOIN FETCH d.hoSoCv cv JOIN FETCH cv.ungVien uv WHERE l.nguoiPhongVan.id = :nguoiPhongVanId ORDER BY l.thoiGianBatDau ASC")
    Page<LichPhongVan> findByNguoiPhongVanId(@Param("nguoiPhongVanId") Long nguoiPhongVanId, Pageable pageable);

    // Dashboard metrics
    @Query("SELECT COUNT(l) FROM LichPhongVan l WHERE l.donUngTuyen.hoSoCv.ungVien.id = :ungVienId AND l.trangThaiPhongVan = 'CHO_PHONG_VAN'")
    long countUpcomingByUngVienId(@Param("ungVienId") Long ungVienId);

    @Query("SELECT COUNT(l) FROM LichPhongVan l WHERE l.nguoiPhongVan.id = :ntdId AND l.trangThaiPhongVan = 'CHO_PHONG_VAN'")
    long countUpcomingByNhaTuyenDungId(@Param("ntdId") Long ntdId);
}
