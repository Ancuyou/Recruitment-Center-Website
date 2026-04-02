package com.example.tuyendung.repository;

import com.example.tuyendung.entity.PendingRegistration;
import com.example.tuyendung.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, Long> {
    Optional<PendingRegistration> findByToken(String token);
    Optional<PendingRegistration> findByTaiKhoan(TaiKhoan taiKhoan);
    void deleteByTaiKhoan(TaiKhoan taiKhoan);
}
