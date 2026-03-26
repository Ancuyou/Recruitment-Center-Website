package com.example.tuyendung.repository;

import com.example.tuyendung.entity.UngVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UngVienRepository extends JpaRepository<UngVien, Long> {

    Optional<UngVien> findByTaiKhoanId(Long taiKhoanId);

    boolean existsBySoDienThoai(String soDienThoai);
}