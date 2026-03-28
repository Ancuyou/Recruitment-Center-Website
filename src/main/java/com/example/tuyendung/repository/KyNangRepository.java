package com.example.tuyendung.repository;

import com.example.tuyendung.entity.KyNang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KyNangRepository extends JpaRepository<KyNang, Long> {

    /**
     * Tìm kỹ năng theo tên chính xác (case-insensitive)
     */
    Optional<KyNang> findByTenKyNangIgnoreCase(String tenKyNang);

    /**
     * Tìm kiếm kỹ năng theo từ khóa
     */
    @Query("SELECT k FROM KyNang k WHERE LOWER(k.tenKyNang) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY k.tenKyNang ASC")
    List<KyNang> searchByKeyword(String keyword);

    /**
     * Lấy danh sách tất cả kỹ năng, sắp xếp theo tên
     */
    @Query("SELECT k FROM KyNang k ORDER BY k.tenKyNang ASC")
    List<KyNang> findAllOrderByName();

    /**
     * Kiểm tra sự tồn tại của kỹ năng theo tên (case-insensitive)
     */
    boolean existsByTenKyNangIgnoreCase(String tenKyNang);
}
