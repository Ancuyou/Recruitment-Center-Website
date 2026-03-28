package com.example.tuyendung.repository;

import com.example.tuyendung.entity.ThongBao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository cho thực thể ThongBao (Module D15-D18).
 *
 * SRP: chỉ tương tác với bảng thong_bao.
 */
@Repository
public interface ThongBaoRepository extends JpaRepository<ThongBao, Long> {

    /**
     * D15: Lấy danh sách thông báo của tài khoản, sắp xếp mới nhất trước.
     */
    Page<ThongBao> findByTaiKhoanIdOrderByNgayTaoDesc(Long taiKhoanId, Pageable pageable);

    /**
     * D17: Đếm số thông báo chưa đọc.
     */
    @Query("SELECT COUNT(t) FROM ThongBao t WHERE t.taiKhoan.id = :taiKhoanId AND t.daDoc = false")
    long countUnreadByTaiKhoanId(@Param("taiKhoanId") Long taiKhoanId);

    /**
     * D18: Bulk update tất cả thông báo chưa đọc → đã đọc.
     * Tránh N+1 write problem (không cần load từng entity rồi save từng cái).
     */
    @Modifying
    @Query("UPDATE ThongBao t SET t.daDoc = true WHERE t.taiKhoan.id = :taiKhoanId AND t.daDoc = false")
    void markAllAsReadByTaiKhoanId(@Param("taiKhoanId") Long taiKhoanId);
}
