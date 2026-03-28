package com.example.tuyendung.repository;

import com.example.tuyendung.entity.CtKyNangTin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho kỹ năng yêu cầu của job
 * 
 * SOLID Principles:
 * - Dependency Inversion: Interface-based, service depends on abstraction
 * - Single Responsibility: Chỉ handle data persistence
 */
@Repository
public interface CtKyNangTinRepository extends JpaRepository<CtKyNangTin, Long> {

    /**
     * Lấy danh sách kỹ năng của job (không bị xóa)
     */
    @Query("SELECT ct FROM CtKyNangTin ct WHERE ct.tinTuyendung.id = :jobId AND ct.daXoa = false ORDER BY ct.ngayTao DESC")
    List<CtKyNangTin> findByJobIdAndNotDeleted(@Param("jobId") Long jobId);

    /**
     * Tìm kỹ năng cụ thể trong job
     */
    @Query("SELECT ct FROM CtKyNangTin ct WHERE ct.tinTuyendung.id = :jobId AND ct.kyNang.id = :kyNangId AND ct.daXoa = false")
    Optional<CtKyNangTin> findByJobIdAndKyNangId(@Param("jobId") Long jobId, @Param("kyNangId") Long kyNangId);

    /**
     * Lấy tất cả kỹ năng yêu cầu của job (include nested)
     */
    @Query("SELECT ct FROM CtKyNangTin ct JOIN FETCH ct.kyNang k WHERE ct.tinTuyendung.id = :jobId AND ct.daXoa = false")
    List<CtKyNangTin> findByJobIdWithKyNang(@Param("jobId") Long jobId);

    /**
     * Lấy ID các kỹ năng của job
     */
    @Query("SELECT ct.kyNang.id FROM CtKyNangTin ct WHERE ct.tinTuyendung.id = :jobId AND ct.daXoa = false")
    List<Long> findKyNangIdsByJobId(@Param("jobId") Long jobId);

    /**
     * Kiểm tra job có kỹ năng này không
     */
    @Query("SELECT COUNT(ct) > 0 FROM CtKyNangTin ct WHERE ct.tinTuyendung.id = :jobId AND ct.kyNang.id = :kyNangId AND ct.daXoa = false")
    boolean existsByJobIdAndKyNangId(@Param("jobId") Long jobId, @Param("kyNangId") Long kyNangId);

}