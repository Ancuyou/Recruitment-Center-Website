package com.example.tuyendung.repository;

import com.example.tuyendung.entity.PendingPasswordReset;
import com.example.tuyendung.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PendingPasswordResetRepository extends JpaRepository<PendingPasswordReset, Long> {
    Optional<PendingPasswordReset> findByToken(String token);
    Optional<PendingPasswordReset> findByTaiKhoan(TaiKhoan taiKhoan);
    void deleteByTaiKhoan(TaiKhoan taiKhoan);
}
