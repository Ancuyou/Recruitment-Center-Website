package com.example.tuyendung.repository;

import com.example.tuyendung.entity.CtKyNangTin;
import com.example.tuyendung.entity.ChiTietKyNangCv;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Custom Repository cho matching algorithms
 * 
 * SOLID Principles:
 * - Dependency Inversion: Interface-based queries
 * - Single Responsibility: Chỉ handle complex data queries
 */
@Repository
public interface MatchingRepository extends JpaRepository<Object, Object> {

    /**
     * Lấy tất cả kỹ năng của CV (cho matching)
     */
    @Query("SELECT cknc FROM ChiTietKyNangCv cknc " +
           "WHERE cknc.hoSoCv.id = :cvId AND cknc.hoSoCv.daXoa = false " +
           "ORDER BY cknc.mucThanhThao DESC")
    List<ChiTietKyNangCv> getCvSkills(@Param("cvId") Long cvId);

    /**
     * Lấy tất cả kỹ năng yêu cầu của job
     */
    @Query("SELECT cknt FROM CtKyNangTin cknt " +
           "WHERE cknt.tinTuyendung.id = :jobId AND cknt.daXoa = false " +
           "ORDER BY cknt.yeucau DESC")
    List<CtKyNangTin> getJobSkills(@Param("jobId") Long jobId);

    /**
     * Lấy các kỹ năng trùng khớp giữa CV và Job
     */
    @Query("SELECT cknc.kyNang.id FROM ChiTietKyNangCv cknc " +
           "WHERE cknc.hoSoCv.id = :cvId AND cknc.hoSoCv.daXoa = false " +
           "AND cknc.kyNang.id IN (" +
           "  SELECT cknt.kyNang.id FROM CtKyNangTin cknt " +
           "  WHERE cknt.tinTuyendung.id = :jobId AND cknt.daXoa = false)")
    List<Long> getMatchedSkillIds(@Param("cvId") Long cvId, @Param("jobId") Long jobId);

    /**
     * Lấy các kỹ năng thiếu (yêu cầu nhưng không có)
     */
    @Query("SELECT cknt.kyNang.id FROM CtKyNangTin cknt " +
           "WHERE cknt.tinTuyendung.id = :jobId AND cknt.daXoa = false " +
           "AND cknt.kyNang.id NOT IN (" +
           "  SELECT cknc.kyNang.id FROM ChiTietKyNangCv cknc " +
           "  WHERE cknc.hoSoCv.id = :cvId AND cknc.hoSoCv.daXoa = false)")
    List<Long> getMissingSkillIds(@Param("cvId") Long cvId, @Param("jobId") Long jobId);

    /**
     * Tìm các ứng viên phù hợp với job (dựa trên kỹ năng)
     */
    @Query(value = "SELECT DISTINCT hsv.id FROM ho_so_cv hsv " +
           "WHERE hsv.da_xoa = false " +
           "AND EXISTS (" +
           "  SELECT 1 FROM hoc_van_kn cknc " +
           "  WHERE cknc.ho_so_cv_id = hsv.id " +
           "  AND cknc.ky_nang_id IN (" +
           "    SELECT ky_nang_id FROM ct_ky_nang_tin " +
           "    WHERE tin_tuyendung_id = ? AND da_xoa = false)) " +
           "ORDER BY hsv.ngay_tao DESC", nativeQuery = true)
    List<Long> findMatchingCandidatesByJobId(Long jobId);

    /**
     * Tìm các công việc phù hợp với CV
     */
    @Query(value = "SELECT DISTINCT tt.id FROM tin_tuyendung tt " +
           "WHERE EXISTS (" +
           "  SELECT 1 FROM ct_ky_nang_tin cknt " +
           "  WHERE cknt.tin_tuyendung_id = tt.id " +
           "  AND cknt.da_xoa = false " +
           "  AND cknt.ky_nang_id IN (" +
           "    SELECT ky_nang_id FROM hoc_van_kn " +
           "    WHERE ho_so_cv_id = ? AND ho_so_cv_id IN (" +
           "      SELECT id FROM ho_so_cv WHERE da_xoa = false))) " +
           "ORDER BY tt.ngay_dang DESC", nativeQuery = true)
    List<Long> findMatchingJobsByCvId(Long cvId);

}