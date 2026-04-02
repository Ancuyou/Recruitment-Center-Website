package com.example.tuyendung.repository;

import com.example.tuyendung.entity.DonUngTuyen;
import com.example.tuyendung.entity.enums.TrangThaiDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DonUngTuyenRepository extends JpaRepository<DonUngTuyen, Long> {

    /**
     * Lấy danh sách đơn ứng tuyển của một Ứng Viên
     */
    @Query("SELECT d FROM DonUngTuyen d JOIN FETCH d.tinTuyenDung t JOIN FETCH t.congTy c WHERE d.hoSoCv.ungVien.id = :ungVienId ORDER BY d.ngayNop DESC")
    Page<DonUngTuyen> findByUngVienId(@Param("ungVienId") Long ungVienId, Pageable pageable);

    /**
     * Lấy danh sách đơn ứng tuyển cho một Job của Nhà Tuyển Dụng
     */
    @Query("SELECT d FROM DonUngTuyen d JOIN FETCH d.hoSoCv cv JOIN FETCH cv.ungVien uv WHERE d.tinTuyenDung.id = :tinTuyenDungId AND d.tinTuyenDung.nhaTuyenDung.id = :nhaTuyenDungId ORDER BY d.ngayNop DESC")
    Page<DonUngTuyen> findByTinTuyenDungIdAndNhaTuyenDungId(@Param("tinTuyenDungId") Long tinTuyenDungId, @Param("nhaTuyenDungId") Long nhaTuyenDungId, Pageable pageable);

    /**
     * Đếm tổng số đơn của một tin tuyển dụng (Dashboard HR)
     */
    long countByTinTuyenDungId(Long tinTuyenDungId);

    /**
     * Lấy chi tiết đơn kèm tin, công ty, CV, ứng viên, tài khoản ứng viên (tránh N+1).
     * mapToResponse() truy cập: tinTuyenDung.congTy, hoSoCv.ungVien.taiKhoan
     * → cần JOIN FETCH đầy đủ.
     */
    @Query("""
            SELECT d FROM DonUngTuyen d
            JOIN FETCH d.tinTuyenDung t
            JOIN FETCH t.congTy c
            JOIN FETCH d.hoSoCv cv
            JOIN FETCH cv.ungVien uv
            JOIN FETCH uv.taiKhoan tk
            WHERE d.id = :id
            """)
    Optional<DonUngTuyen> findByIdWithDetails(@Param("id") Long id);

    /**
     * Kiểm tra xem Ứng viên đã nộp đơn vào Job này chưa
     */
    @Query("SELECT COUNT(d) > 0 FROM DonUngTuyen d WHERE d.tinTuyenDung.id = :tinTuyenDungId AND d.hoSoCv.ungVien.taiKhoan.id = :taiKhoanId")
    boolean existsByTinTuyenDungIdAndTaiKhoanId(@Param("tinTuyenDungId") Long tinTuyenDungId, @Param("taiKhoanId") Long taiKhoanId);

    // D19 Dashboard Candidate
    @Query("SELECT COUNT(d) FROM DonUngTuyen d WHERE d.hoSoCv.ungVien.id = :ungVienId")
    long countByUngVienId(@Param("ungVienId") Long ungVienId);

    @Query("SELECT COUNT(d) FROM DonUngTuyen d WHERE d.hoSoCv.ungVien.id = :ungVienId AND d.trangThaiHienTai = :trangThai")
    long countByUngVienIdAndTrangThai(@Param("ungVienId") Long ungVienId, @Param("trangThai") TrangThaiDon trangThai);

    // D20 Dashboard HR
    @Query("SELECT COUNT(d) FROM DonUngTuyen d WHERE d.tinTuyenDung.nhaTuyenDung.id = :ntdId")
    long countByNhaTuyenDungId(@Param("ntdId") Long ntdId);

    @Query("SELECT COUNT(d) FROM DonUngTuyen d WHERE d.tinTuyenDung.nhaTuyenDung.id = :ntdId AND d.trangThaiHienTai = :trangThai")
    long countByNhaTuyenDungIdAndTrangThai(@Param("ntdId") Long ntdId, @Param("trangThai") TrangThaiDon trangThai);
}
