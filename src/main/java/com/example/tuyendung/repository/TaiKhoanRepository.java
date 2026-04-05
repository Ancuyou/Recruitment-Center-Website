package com.example.tuyendung.repository;

import com.example.tuyendung.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, Long> {

    Optional<TaiKhoan> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT t FROM TaiKhoan t JOIN FETCH t.ungVien WHERE t.email = :email")
    Optional<TaiKhoan> findByEmailWithUngVien(String email);

    @Query("SELECT t FROM TaiKhoan t JOIN FETCH t.nhaTuyenDung WHERE t.email = :email")
    Optional<TaiKhoan> findByEmailWithNhaTuyenDung(String email);

    @Query("""
            SELECT DISTINCT t FROM TaiKhoan t
            LEFT JOIN FETCH t.ungVien uv
            LEFT JOIN FETCH t.nhaTuyenDung ntd
            LEFT JOIN FETCH ntd.congTy c
            ORDER BY t.id DESC
            """)
    List<TaiKhoan> findAllWithProfiles();

    @Query("""
            SELECT t FROM TaiKhoan t
            LEFT JOIN FETCH t.ungVien uv
            LEFT JOIN FETCH t.nhaTuyenDung ntd
            LEFT JOIN FETCH ntd.congTy c
            WHERE t.id = :id
            """)
    Optional<TaiKhoan> findByIdWithProfiles(@Param("id") Long id);
}