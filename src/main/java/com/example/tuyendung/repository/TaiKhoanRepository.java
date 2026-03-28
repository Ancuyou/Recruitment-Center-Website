package com.example.tuyendung.repository;

import com.example.tuyendung.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, Long> {

    Optional<TaiKhoan> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT t FROM TaiKhoan t JOIN FETCH t.ungVien WHERE t.email = :email")
    Optional<TaiKhoan> findByEmailWithUngVien(String email);

    @Query("SELECT t FROM TaiKhoan t JOIN FETCH t.nhaTuyenDung WHERE t.email = :email")
    Optional<TaiKhoan> findByEmailWithNhaTuyenDung(String email);

    Optional<TaiKhoan> findByResetToken(String resetToken);

    Optional<TaiKhoan> findByVerifyToken(String verifyToken);
}