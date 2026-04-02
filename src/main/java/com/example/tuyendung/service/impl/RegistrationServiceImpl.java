package com.example.tuyendung.service.impl;

import com.example.tuyendung.config.AuthProperties;
import com.example.tuyendung.dto.request.DangKyNhaTuyenDungRequest;
import com.example.tuyendung.dto.request.DangKyUngVienRequest;
import com.example.tuyendung.dto.response.AuthResponse;
import com.example.tuyendung.entity.CongTy;
import com.example.tuyendung.entity.NhaTuyenDung;
import com.example.tuyendung.entity.PendingRegistration;
import com.example.tuyendung.entity.TaiKhoan;
import com.example.tuyendung.entity.UngVien;
import com.example.tuyendung.entity.enums.VaiTroTaiKhoan;
import com.example.tuyendung.exception.BaseBusinessException;
import com.example.tuyendung.exception.ErrorCode;
import com.example.tuyendung.repository.CongTyRepository;
import com.example.tuyendung.repository.NhaTuyenDungRepository;
import com.example.tuyendung.repository.PendingRegistrationRepository;
import com.example.tuyendung.repository.TaiKhoanRepository;
import com.example.tuyendung.repository.UngVienRepository;
import com.example.tuyendung.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final UngVienRepository ungVienRepository;
    private final NhaTuyenDungRepository nhaTuyenDungRepository;
    private final CongTyRepository congTyRepository;
    private final PasswordEncoder passwordEncoder;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final AuthProperties authProperties;

    @Override
    @Transactional
    public AuthResponse dangKyUngVien(DangKyUngVienRequest request) {
        if (taiKhoanRepository.existsByEmail(request.getEmail())) {
            throw new BaseBusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (request.getSoDienThoai() != null && ungVienRepository.existsBySoDienThoai(request.getSoDienThoai())) {
            throw new BaseBusinessException(ErrorCode.DUPLICATE_RESOURCE, "Số điện thoại " + request.getSoDienThoai() + " đã tồn tại");
        }

        String vToken = UUID.randomUUID().toString();
        TaiKhoan taiKhoan = TaiKhoan.builder()
                .email(request.getEmail())
                .matKhauHash(passwordEncoder.encode(request.getMatKhau()))
                .vaiTro(VaiTroTaiKhoan.UNG_VIEN)
                .laKichHoat(false)
                .build();
        TaiKhoan savedTaiKhoan = taiKhoanRepository.save(taiKhoan);

        PendingRegistration pendingReg = PendingRegistration.builder()
                .token(vToken)
                .taiKhoan(savedTaiKhoan)
                .ngayHetHan(LocalDateTime.now().plusHours(authProperties.getVerificationExpirationHours()))
                .build();
        pendingRegistrationRepository.save(pendingReg);

        log.info("A1 (SRP): Vui lòng xác thực email ứng viên tại: http://localhost:8080/api/auth/verify-email?token={}", vToken);

        UngVien ungVien = new UngVien();
        ungVien.setTaiKhoan(taiKhoan);
        ungVien.setHoTen(request.getHoTen());
        ungVien.setSoDienThoai(request.getSoDienThoai());
        ungVien.setNgaySinh(request.getNgaySinh());
        ungVien.setGioiTinh(request.getGioiTinh());
        ungVienRepository.save(ungVien);

        return AuthResponse.builder()
                .taiKhoanId(taiKhoan.getId())
                .email(taiKhoan.getEmail())
                .vaiTro(taiKhoan.getVaiTro())
                .userInfo(AuthResponse.UserInfoDTO.builder()
                        .id(ungVien.getId())
                        .hoTen(ungVien.getHoTen())
                        .soDienThoai(ungVien.getSoDienThoai())
                        .anhDaiDien(ungVien.getAnhDaiDien())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse dangKyNhaTuyenDung(DangKyNhaTuyenDungRequest request) {
        if (taiKhoanRepository.existsByEmail(request.getEmail())) {
            throw new BaseBusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        CongTy congTy = congTyRepository.findByMaSoThue(request.getMaSoThue())
                .orElseGet(() -> {
                    CongTy newCongTy = new CongTy();
                    newCongTy.setMaSoThue(request.getMaSoThue());
                    newCongTy.setTenCongTy(request.getTenCongTy());
                    newCongTy.setWebsite(request.getWebsite());
                    return congTyRepository.save(newCongTy);
                });

        String vToken = UUID.randomUUID().toString();
        TaiKhoan taiKhoan = TaiKhoan.builder()
                .email(request.getEmail())
                .matKhauHash(passwordEncoder.encode(request.getMatKhau()))
                .vaiTro(VaiTroTaiKhoan.NHA_TUYEN_DUNG)
                .laKichHoat(false)
                .build();
        TaiKhoan savedTaiKhoan = taiKhoanRepository.save(taiKhoan);

        PendingRegistration pendingReg = PendingRegistration.builder()
                .token(vToken)
                .taiKhoan(savedTaiKhoan)
                .ngayHetHan(LocalDateTime.now().plusHours(authProperties.getVerificationExpirationHours()))
                .build();
        pendingRegistrationRepository.save(pendingReg);

        log.info("A2 (SRP): Vui lòng xác thực email NTD tại: http://localhost:8080/api/auth/verify-email?token={}", vToken);

        NhaTuyenDung nhaTuyenDung = new NhaTuyenDung();
        nhaTuyenDung.setTaiKhoan(taiKhoan);
        nhaTuyenDung.setCongTy(congTy);
        nhaTuyenDung.setHoTen(request.getHoTen());
        nhaTuyenDung.setChucVu(request.getChucVu());
        nhaTuyenDung.setSoDienThoai(request.getSoDienThoai());
        nhaTuyenDungRepository.save(nhaTuyenDung);

        return AuthResponse.builder()
                .taiKhoanId(taiKhoan.getId())
                .email(taiKhoan.getEmail())
                .vaiTro(taiKhoan.getVaiTro())
                .userInfo(AuthResponse.UserInfoDTO.builder()
                        .id(nhaTuyenDung.getId())
                        .hoTen(nhaTuyenDung.getHoTen())
                        .tenCongTy(congTy.getTenCongTy())
                        .build())
                .build();
    }
}
