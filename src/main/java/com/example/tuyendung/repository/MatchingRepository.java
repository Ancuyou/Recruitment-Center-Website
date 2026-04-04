package com.example.tuyendung.repository;

import com.example.tuyendung.entity.ChiTietKyNangCv;
import com.example.tuyendung.entity.id.ChiTietKyNangCvId;
import com.example.tuyendung.entity.id.ChiTietKyNangTin;
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
public interface MatchingRepository extends JpaRepository<ChiTietKyNangCv, ChiTietKyNangCvId> {

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
    @Query("SELECT cknt FROM ChiTietKyNangTin cknt " +
           "WHERE cknt.tinTuyenDung.id = :jobId AND cknt.daXoa = false " +
           "ORDER BY cknt.yeucau DESC")
    List<ChiTietKyNangTin> getJobSkills(@Param("jobId") Long jobId);

    /**
     * Lấy các kỹ năng trùng khớp giữa CV và Job
     */
    @Query("SELECT cknc.kyNang.id FROM ChiTietKyNangCv cknc " +
           "WHERE cknc.hoSoCv.id = :cvId AND cknc.hoSoCv.daXoa = false " +
           "AND cknc.kyNang.id IN (" +
           "  SELECT cknt.kyNang.id FROM ChiTietKyNangTin cknt " +
           "  WHERE cknt.tinTuyenDung.id = :jobId AND cknt.daXoa = false)")
    List<Long> getMatchedSkillIds(@Param("cvId") Long cvId, @Param("jobId") Long jobId);

    /**
     * Lấy các kỹ năng thiếu (yêu cầu nhưng không có)
     */
    @Query("SELECT cknt.kyNang.id FROM ChiTietKyNangTin cknt " +
           "WHERE cknt.tinTuyenDung.id = :jobId AND cknt.daXoa = false " +
           "AND cknt.kyNang.id NOT IN (" +
           "  SELECT cknc.kyNang.id FROM ChiTietKyNangCv cknc " +
           "  WHERE cknc.hoSoCv.id = :cvId AND cknc.hoSoCv.daXoa = false)")
    List<Long> getMissingSkillIds(@Param("cvId") Long cvId, @Param("jobId") Long jobId);

    /**
     * Tìm các ứng viên phù hợp với job (dựa trên kỹ năng)
     */
    @Query("SELECT cknc.hoSoCv.id FROM ChiTietKyNangCv cknc " +
           "WHERE cknc.hoSoCv.daXoa = false " +
           "AND cknc.kyNang.id IN (" +
           "  SELECT cknt.kyNang.id FROM ChiTietKyNangTin cknt " +
           "  WHERE cknt.tinTuyenDung.id = :jobId AND cknt.daXoa = false) " +
           "GROUP BY cknc.hoSoCv.id " +
           "ORDER BY MAX(cknc.hoSoCv.ngayTao) DESC")
    List<Long> findMatchingCandidatesByJobId(@Param("jobId") Long jobId);

    /**
     * Tìm các công việc phù hợp với CV
     */
    @Query("SELECT cknt.tinTuyenDung.id FROM ChiTietKyNangTin cknt " +
           "WHERE cknt.daXoa = false " +
           "AND cknt.kyNang.id IN (" +
           "  SELECT cknc.kyNang.id FROM ChiTietKyNangCv cknc " +
           "  WHERE cknc.hoSoCv.id = :cvId AND cknc.hoSoCv.daXoa = false) " +
           "GROUP BY cknt.tinTuyenDung.id " +
           "ORDER BY MAX(cknt.tinTuyenDung.ngayTao) DESC")
    List<Long> findMatchingJobsByCvId(@Param("cvId") Long cvId);

}