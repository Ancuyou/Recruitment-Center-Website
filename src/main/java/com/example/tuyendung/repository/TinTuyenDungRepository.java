package com.example.tuyendung.repository;

import com.example.tuyendung.entity.TinTuyenDung;
import com.example.tuyendung.entity.enums.CapBacYeuCau;
import com.example.tuyendung.entity.enums.HinhThucLamViec;
import com.example.tuyendung.entity.enums.TrangThaiDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho tin tuyển dụng (Job Postings)
 * 
 * SOLID Principles:
 * - Dependency Inversion: Interface-based, services depend on abstraction
 * - Single Responsibility: Chỉ handle data persistence for job postings
 */
@Repository
public interface TinTuyenDungRepository extends JpaRepository<TinTuyenDung, Long> {

    /**
     * Tìm job by ID (với checking trang_thai > 0, tức là không bị xóa)
     * Chỉ trả về tin đang mở (TrangThaiTin.MO)
     */
    @Query("SELECT t FROM TinTuyenDung t WHERE t.id = :jobId AND t.trangThai IN (com.example.tuyendung.entity.enums.TrangThaiTin.MO, com.example.tuyendung.entity.enums.TrangThaiTin.DONG)")
    Optional<TinTuyenDung> findByIdAndNotDeleted(@Param("jobId") Long jobId);

    /**
     * Tìm job by ID (trả về cả những job bị xóa)
     */
    @Query("SELECT t FROM TinTuyenDung t WHERE t.id = :jobId")
    Optional<TinTuyenDung> findByIdIncludingDeleted(@Param("jobId") Long jobId);

    /**
     * Tìm tất cả job đang hoạt động của 1 nhà tuyển dụng
     */
    @Query("SELECT t FROM TinTuyenDung t WHERE t.nhaTuyenDung.id = :ntdId AND t.trangThai IN (com.example.tuyendung.entity.enums.TrangThaiTin.MO, com.example.tuyendung.entity.enums.TrangThaiTin.DONG) ORDER BY t.ngayTao DESC")
    List<TinTuyenDung> findByNhaTuyenDungIdOrderByNgayTaoDesc(@Param("ntdId") Long ntdId);

    /**
     * Tìm tất cả job đang hoạt động (phân trang)
     */
    @Query("SELECT t FROM TinTuyenDung t WHERE t.trangThai IN (com.example.tuyendung.entity.enums.TrangThaiTin.MO, com.example.tuyendung.entity.enums.TrangThaiTin.DONG)")
    Page<TinTuyenDung> findActiveJobs(Pageable pageable);

    /**
     * Tìm job theo điều kiện lọc
     */
    @Query("SELECT t FROM TinTuyenDung t WHERE t.trangThai IN (com.example.tuyendung.entity.enums.TrangThaiTin.MO, com.example.tuyendung.entity.enums.TrangThaiTin.DONG) " +
            "AND (:keyword IS NULL OR LOWER(t.tieuDe) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.moTaCongViec) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:capBac IS NULL OR t.capBacYeuCau = :capBac) " +
            "AND (:hinhThuc IS NULL OR t.hinhThucLamViec = :hinhThuc) " +
            "AND (:mucLuongMin IS NULL OR t.mucLuongMax >= :mucLuongMin)")
    Page<TinTuyenDung> searchJobs(
            @Param("keyword") String keyword,
            @Param("capBac") CapBacYeuCau capBac,
            @Param("hinhThuc") HinhThucLamViec hinhThuc,
            @Param("mucLuongMin") BigDecimal mucLuongMin,
            Pageable pageable);

    /**
     * Đếm tổng số đơn cho 1 job
     */
    @Query("SELECT COUNT(d) FROM DonUngTuyen d WHERE d.tinTuyenDung.id = :tinId")
    long countTotalApplications(@Param("tinId") Long tinId);

    /**
     * Đếm số đơn theo trạng thái
     */
    @Query("SELECT COUNT(d) FROM DonUngTuyen d WHERE d.tinTuyenDung.id = :tinId AND d.trangThaiHienTai = :status")
    long countApplicationsByStatus(@Param("tinId") Long tinId, @Param("status") TrangThaiDon status);

    /**
     * Đếm số job đang hoạt động của 1 nhà tuyển dụng
     */
    @Query("SELECT COUNT(t) FROM TinTuyenDung t WHERE t.nhaTuyenDung.id = :ntdId AND t.trangThai IN (com.example.tuyendung.entity.enums.TrangThaiTin.MO, com.example.tuyendung.entity.enums.TrangThaiTin.DONG)")
    long countActiveJobsByNhaTuyenDungId(@Param("ntdId") Long ntdId);
}
