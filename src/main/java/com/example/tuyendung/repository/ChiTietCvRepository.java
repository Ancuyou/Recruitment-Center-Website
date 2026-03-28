package com.example.tuyendung.repository;

import com.example.tuyendung.entity.ChiTietCv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChiTietCvRepository extends JpaRepository<ChiTietCv, Long> {

    /**
     * Tìm tất cả chi tiết CV theo CV ID, được sắp xếp theo ngày tạo (only from non-deleted CVs)
     */
    @Query("SELECT cd FROM ChiTietCv cd WHERE cd.hoSoCv.id = :hoSoCvId AND cd.hoSoCv.daXoa = false ORDER BY cd.ngayTao DESC")
    List<ChiTietCv> findByHoSoCvId(Long hoSoCvId);

    /**
     * Tìm chi tiết CV theo ID, kiểm tra loại bản ghi (only from non-deleted CVs)
     */
    @Query("SELECT cd FROM ChiTietCv cd WHERE cd.id = :id AND cd.hoSoCv.id = :hoSoCvId AND cd.hoSoCv.daXoa = false")
    Optional<ChiTietCv> findByIdAndHoSoCvId(Long id, Long hoSoCvId);

    /**
     * Lấy danh sách chi tiết theo loại bản ghi (1: Học vấn, 2: Kinh nghiệm, 3: Chứng chỉ) from non-deleted CVs
     */
    @Query("SELECT cd FROM ChiTietCv cd WHERE cd.hoSoCv.id = :hoSoCvId AND cd.loaiBanGhi = :loaiBanGhi AND cd.hoSoCv.daXoa = false ORDER BY cd.ngayBatDau DESC")
    List<ChiTietCv> findByHoSoCvIdAndLoaiBanGhi(Long hoSoCvId, Integer loaiBanGhi);

    /**
     * Xóa tất cả chi tiết CV của một CV
     */
    void deleteByHoSoCvId(Long hoSoCvId);
}
