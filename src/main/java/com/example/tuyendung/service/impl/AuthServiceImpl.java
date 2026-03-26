package com.example.tuyendung.service.impl;

import com.example.tuyendung.common.Constants;
import com.example.tuyendung.dto.request.DangKyUngVienRequest;
import com.example.tuyendung.dto.request.DangKyNhaTuyenDungRequest;
import com.example.tuyendung.dto.request.DangNhapRequest;
import com.example.tuyendung.dto.response.AuthResponse;
import com.example.tuyendung.dto.response.UserInfoResponse;
import com.example.tuyendung.entity.*;
import com.example.tuyendung.entity.enums.VaiTroTaiKhoan;
import com.example.tuyendung.exception.BusinessException;
import com.example.tuyendung.repository.*;
import com.example.tuyendung.security.JwtTokenProvider;
import com.example.tuyendung.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final UngVienRepository ungVienRepository;
    private final NhaTuyenDungRepository nhaTuyenDungRepository;
    private final CongTyRepository congTyRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public AuthResponse dangKyUngVien(DangKyUngVienRequest request) {
        // Check email tồn tại
        if (taiKhoanRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email đã được đăng ký");
        }

        // Check số điện thoại tồn tại
        if (request.getSoDienThoai() != null &&
                ungVienRepository.existsBySoDienThoai(request.getSoDienThoai())) {
            throw new BusinessException("Số điện thoại đã được đăng ký");
        }

        // Tạo tài khoản
        TaiKhoan taiKhoan = TaiKhoan.builder()
                .email(request.getEmail())
                .matKhauHash(passwordEncoder.encode(request.getMatKhau()))
                .vaiTro(VaiTroTaiKhoan.UNG_VIEN)
                .laKichHoat(Constants.ACCOUNT_ACTIVE)
                .build();
        taiKhoanRepository.save(taiKhoan);

        // Tạo ứng viên
        UngVien ungVien = new UngVien();
        ungVien.setTaiKhoan(taiKhoan);
        ungVien.setHoTen(request.getHoTen());
        ungVien.setSoDienThoai(request.getSoDienThoai());
        ungVien.setNgaySinh(request.getNgaySinh());
        ungVien.setGioiTinh(request.getGioiTinh());
        ungVienRepository.save(ungVien);

        // Generate token
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                taiKhoan, null, java.util.Collections.emptyList()
        );

        return AuthResponse.builder()
                .taiKhoanId(taiKhoan.getId())
                .email(taiKhoan.getEmail())
                .vaiTro(taiKhoan.getVaiTro())
                .accessToken(jwtTokenProvider.generateAccessToken(authentication))
                .refreshToken(jwtTokenProvider.generateRefreshToken(authentication))
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
        // Check email tồn tại
        if (taiKhoanRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email đã được đăng ký");
        }

        // Tìm hoặc tạo công ty
        CongTy congTy = congTyRepository.findByMaSoThue(request.getMaSoThue())
                .orElseGet(() -> {
                    CongTy newCongTy = new CongTy();
                    newCongTy.setMaSoThue(request.getMaSoThue());
                    newCongTy.setTenCongTy(request.getTenCongTy());
                    newCongTy.setNganhNghe(request.getNganhNghe());
                    newCongTy.setWebsite(request.getWebsite());
                    return congTyRepository.save(newCongTy);
                });

        // Tạo tài khoản
        TaiKhoan taiKhoan = TaiKhoan.builder()
                .email(request.getEmail())
                .matKhauHash(passwordEncoder.encode(request.getMatKhau()))
                .vaiTro(VaiTroTaiKhoan.NHA_TUYEN_DUNG)
                .laKichHoat(Constants.ACCOUNT_ACTIVE)
                .build();
        taiKhoanRepository.save(taiKhoan);

        // Tạo nhà tuyển dụng
        NhaTuyenDung nhaTuyenDung = new NhaTuyenDung();
        nhaTuyenDung.setTaiKhoan(taiKhoan);
        nhaTuyenDung.setCongTy(congTy);
        nhaTuyenDung.setHoTen(request.getHoTen());
        nhaTuyenDung.setChucVu(request.getChucVu());
        nhaTuyenDung.setSoDienThoai(request.getSoDienThoai());
        nhaTuyenDungRepository.save(nhaTuyenDung);

        // Generate token
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                taiKhoan, null, java.util.Collections.emptyList()
        );

        return AuthResponse.builder()
                .taiKhoanId(taiKhoan.getId())
                .email(taiKhoan.getEmail())
                .vaiTro(taiKhoan.getVaiTro())
                .accessToken(jwtTokenProvider.generateAccessToken(authentication))
                .refreshToken(jwtTokenProvider.generateRefreshToken(authentication))
                .userInfo(AuthResponse.UserInfoDTO.builder()
                        .id(nhaTuyenDung.getId())
                        .hoTen(nhaTuyenDung.getHoTen())
                        .tenCongTy(congTy.getTenCongTy())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse dangNhap(DangNhapRequest request) {
        // Authentication
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMatKhau())
        );

        TaiKhoan taiKhoan = (TaiKhoan) authentication.getPrincipal();

        // Load thêm thông tin profile
        AuthResponse.UserInfoDTO userInfo = null;

        if (taiKhoan.getVaiTro() == VaiTroTaiKhoan.UNG_VIEN) {
            UngVien ungVien = ungVienRepository.findByTaiKhoanId(taiKhoan.getId()).orElse(null);
            if (ungVien != null) {
                userInfo = AuthResponse.UserInfoDTO.builder()
                        .id(ungVien.getId())
                        .hoTen(ungVien.getHoTen())
                        .soDienThoai(ungVien.getSoDienThoai())
                        .anhDaiDien(ungVien.getAnhDaiDien())
                        .build();
            }
        } else if (taiKhoan.getVaiTro() == VaiTroTaiKhoan.NHA_TUYEN_DUNG) {
            NhaTuyenDung nhaTuyenDung = nhaTuyenDungRepository.findByTaiKhoanId(taiKhoan.getId()).orElse(null);
            if (nhaTuyenDung != null) {
                userInfo = AuthResponse.UserInfoDTO.builder()
                        .id(nhaTuyenDung.getId())
                        .hoTen(nhaTuyenDung.getHoTen())
                        .tenCongTy(nhaTuyenDung.getCongTy().getTenCongTy())
                        .build();
            }
        }

        return AuthResponse.builder()
                .taiKhoanId(taiKhoan.getId())
                .email(taiKhoan.getEmail())
                .vaiTro(taiKhoan.getVaiTro())
                .accessToken(jwtTokenProvider.generateAccessToken(authentication))
                .refreshToken(jwtTokenProvider.generateRefreshToken(authentication))
                .userInfo(userInfo)
                .build();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException("Refresh token không hợp lệ");
        }

        Long taiKhoanId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        TaiKhoan taiKhoan = taiKhoanRepository.findById(taiKhoanId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản"));

        if (!taiKhoan.getLaKichHoat()) {
            throw new BusinessException("Tài khoản đã bị khóa");
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                taiKhoan, null, java.util.Collections.emptyList()
        );

        return AuthResponse.builder()
                .taiKhoanId(taiKhoan.getId())
                .email(taiKhoan.getEmail())
                .vaiTro(taiKhoan.getVaiTro())
                .accessToken(jwtTokenProvider.generateAccessToken(authentication))
                .refreshToken(jwtTokenProvider.generateRefreshToken(authentication))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfoResponse getThongTinUser(Long taiKhoanId) {
        TaiKhoan taiKhoan = taiKhoanRepository.findById(taiKhoanId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản"));

        UserInfoResponse.UserInfoResponseBuilder builder = UserInfoResponse.builder()
                .taiKhoanId(taiKhoan.getId())
                .email(taiKhoan.getEmail())
                .vaiTro(taiKhoan.getVaiTro());

        if (taiKhoan.getVaiTro() == VaiTroTaiKhoan.UNG_VIEN) {
            UngVien ungVien = ungVienRepository.findByTaiKhoanId(taiKhoanId).orElse(null);
            if (ungVien != null) {
                builder.hoTen(ungVien.getHoTen())
                        .soDienThoai(ungVien.getSoDienThoai())
                        .ngaySinh(ungVien.getNgaySinh())
                        .gioiTinh(ungVien.getGioiTinh())
                        .anhDaiDien(ungVien.getAnhDaiDien());
            }
        } else if (taiKhoan.getVaiTro() == VaiTroTaiKhoan.NHA_TUYEN_DUNG) {
            NhaTuyenDung nhaTuyenDung = nhaTuyenDungRepository.findByTaiKhoanId(taiKhoanId).orElse(null);
            if (nhaTuyenDung != null) {
                builder.hoTen(nhaTuyenDung.getHoTen())
                        .soDienThoai(nhaTuyenDung.getSoDienThoai())
                        .chucVu(nhaTuyenDung.getChucVu())
                        .tenCongTy(nhaTuyenDung.getCongTy().getTenCongTy());
            }
        }

        return builder.build();
    }
}