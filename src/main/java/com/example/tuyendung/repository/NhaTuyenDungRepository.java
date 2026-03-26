package com.example.tuyendung.repository;

import com.example.tuyendung.entity.NhaTuyenDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NhaTuyenDungRepository extends JpaRepository<NhaTuyenDung, Long> {

    Optional<NhaTuyenDung> findByTaiKhoanId(Long taiKhoanId);
}