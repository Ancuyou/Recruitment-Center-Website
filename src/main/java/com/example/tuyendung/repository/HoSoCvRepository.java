package com.example.tuyendung.repository;

import com.example.tuyendung.entity.HoSoCv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoSoCvRepository extends JpaRepository<HoSoCv, Long> {

    /**
     * Tìm tất cả CV của một ứng viên (chưa bị xóa mềm)
     */
    @Query("SELECT cv FROM HoSoCv cv WHERE cv.ungVien.id = :ungVienId AND cv.daXoa = false ORDER BY cv.ngayCapNhat DESC")
    List<HoSoCv> findByUngVienIdAndNotDeleted(Long ungVienId);

    /**
     * Tìm CV chính của ứng viên
     */
    @Query("SELECT cv FROM HoSoCv cv WHERE cv.ungVien.id = :ungVienId AND cv.laCvChinh = true AND cv.daXoa = false")
    Optional<HoSoCv> findDefaultCvByUngVienId(Long ungVienId);

    /**
     * Tìm CV theo ID (chưa bị xóa mềm)
     */
    @Query("SELECT cv FROM HoSoCv cv WHERE cv.id = :cvId AND cv.daXoa = false")
    Optional<HoSoCv> findByIdAndNotDeleted(Long cvId);

    /**
     * Kiểm tra CV có tồn tại và thuộc về ứng viên
     */
    @Query("SELECT CASE WHEN COUNT(cv) > 0 THEN true ELSE false END FROM HoSoCv cv WHERE cv.id = :cvId AND cv.ungVien.id = :ungVienId AND cv.daXoa = false")
    boolean existsByIdAndUngVienIdAndNotDeleted(Long cvId, Long ungVienId);

    /**
     * Atomically set all CVs for a user to non-default (laCvChinh = false)
     */
    @Modifying
    @Query("UPDATE HoSoCv cv SET cv.laCvChinh = false WHERE cv.ungVien.id = :ungVienId")
    void updateSetAllDefaultToFalse(Long ungVienId);

    /**
     * Atomically set a specific CV as default (laCvChinh = true)
     */
    @Modifying
    @Query("UPDATE HoSoCv cv SET cv.laCvChinh = true WHERE cv.id = :cvId")
    void updateSetDefaultCvById(Long cvId);
}
