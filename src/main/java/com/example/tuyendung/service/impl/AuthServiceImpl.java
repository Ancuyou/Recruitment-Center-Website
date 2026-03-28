package com.example.tuyendung.service.impl;

import com.example.tuyendung.dto.request.DangKyUngVienRequest;
import com.example.tuyendung.dto.request.DangKyNhaTuyenDungRequest;
import com.example.tuyendung.dto.request.DangNhapRequest;
import com.example.tuyendung.dto.response.AuthResponse;
import com.example.tuyendung.dto.response.UserInfoResponse;
import com.example.tuyendung.entity.*;
import com.example.tuyendung.entity.enums.VaiTroTaiKhoan;
import com.example.tuyendung.exception.DuplicateResourceException;
import com.example.tuyendung.exception.ResourceNotFoundException;
import com.example.tuyendung.exception.ValidationException;
import com.example.tuyendung.repository.*;
import com.example.tuyendung.security.CustomUserDetails;
import com.example.tuyendung.security.JwtTokenProvider;
import com.example.tuyendung.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
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
            throw new DuplicateResourceException("Email", request.getEmail());
        }

        // Check số điện thoại tồn tại
        if (request.getSoDienThoai() != null &&
                ungVienRepository.existsBySoDienThoai(request.getSoDienThoai())) {
            throw new DuplicateResourceException("Số điện thoại", request.getSoDienThoai());
        }

        // Tạo tài khoản (Chờ xác thực email)
        String vToken = UUID.randomUUID().toString();
        TaiKhoan taiKhoan = TaiKhoan.builder()
                .email(request.getEmail())
                .matKhauHash(passwordEncoder.encode(request.getMatKhau()))
                .vaiTro(VaiTroTaiKhoan.UNG_VIEN)
                .laKichHoat(false) // Bắt buộc xác thực email
                .verifyToken(vToken)
                .build();
        taiKhoanRepository.save(taiKhoan);

        log.info("A1: Vui lòng xác thực email ứng viên tại: http://localhost:8080/api/auth/verify-email?token={}", vToken);

        // Tạo ứng viên
        UngVien ungVien = new UngVien();
        ungVien.setTaiKhoan(taiKhoan);
        ungVien.setHoTen(request.getHoTen());
        ungVien.setSoDienThoai(request.getSoDienThoai());
        ungVien.setNgaySinh(request.getNgaySinh());
        ungVien.setGioiTinh(request.getGioiTinh());
        ungVienRepository.save(ungVien);

        // Generate token
        CustomUserDetails userDetails = new CustomUserDetails(taiKhoan);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
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
            throw new DuplicateResourceException("Email", request.getEmail());
        }

        // Tìm hoặc tạo công ty
        CongTy congTy = congTyRepository.findByMaSoThue(request.getMaSoThue())
                .orElseGet(() -> {
                    CongTy newCongTy = new CongTy();
                    newCongTy.setMaSoThue(request.getMaSoThue());
                    newCongTy.setTenCongTy(request.getTenCongTy());
                    newCongTy.setWebsite(request.getWebsite());
                    return congTyRepository.save(newCongTy);
                });

        // Tạo tài khoản (Chờ xác thực email)
        String vToken = UUID.randomUUID().toString();
        TaiKhoan taiKhoan = TaiKhoan.builder()
                .email(request.getEmail())
                .matKhauHash(passwordEncoder.encode(request.getMatKhau()))
                .vaiTro(VaiTroTaiKhoan.NHA_TUYEN_DUNG)
                .laKichHoat(false) // Bắt buộc xác thực email
                .verifyToken(vToken)
                .build();
        taiKhoanRepository.save(taiKhoan);

        log.info("A2: Vui lòng xác thực email NTD tại: http://localhost:8080/api/auth/verify-email?token={}", vToken);

        // Tạo nhà tuyển dụng
        NhaTuyenDung nhaTuyenDung = new NhaTuyenDung();
        nhaTuyenDung.setTaiKhoan(taiKhoan);
        nhaTuyenDung.setCongTy(congTy);
        nhaTuyenDung.setHoTen(request.getHoTen());
        nhaTuyenDung.setChucVu(request.getChucVu());
        nhaTuyenDung.setSoDienThoai(request.getSoDienThoai());
        nhaTuyenDungRepository.save(nhaTuyenDung);

        // Generate token
        CustomUserDetails userDetails = new CustomUserDetails(taiKhoan);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
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

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        TaiKhoan taiKhoan = customUserDetails.getTaiKhoan();

        if (!taiKhoan.getLaKichHoat()) {
            throw new ValidationException("Tài khoản chưa được kích hoạt. Vui lòng kiểm tra email.");
        }

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
            throw new ValidationException("Refresh token không hợp lệ");
        }

        Long taiKhoanId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        TaiKhoan taiKhoan = taiKhoanRepository.findById(taiKhoanId)
                .orElseThrow(() -> new ResourceNotFoundException("TaiKhoan", taiKhoanId));

        if (!taiKhoan.getLaKichHoat()) {
            throw new ValidationException("Tài khoản đã bị khóa");
        }

        CustomUserDetails userDetails = new CustomUserDetails(taiKhoan);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
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
                .orElseThrow(() -> new ResourceNotFoundException("TaiKhoan", taiKhoanId));

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

    @Override
    public void logout(String token) {
        // Với JWT stateless, ta có thể yêu cầu client xoá token.
        // Thực tế có thể lưu token vào Redis blacklist. Ở đây giả lập thành công.
        log.info("A4: User đã đăng xuất khỏi hệ thống.");
    }

    @Override
    @Transactional
    public void forgotPassword(com.example.tuyendung.dto.request.ForgotPasswordRequest request) {
        TaiKhoan tk = taiKhoanRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("TaiKhoan", request.getEmail()));

        String resetToken = UUID.randomUUID().toString();
        tk.setResetToken(resetToken);
        tk.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        taiKhoanRepository.save(tk);

        log.info("A6: Vui lòng click vào link sau để đặt lại mật khẩu của bạn (Hạn 15 phút): http://localhost:8080/api/auth/reset-password?token={}", resetToken);
    }

    @Override
    @Transactional
    public void resetPassword(com.example.tuyendung.dto.request.ResetPasswordRequest request) {
        TaiKhoan tk = taiKhoanRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new ValidationException("Token không hợp lệ hoặc không tồn tại"));

        if (tk.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Token đã hết hạn");
        }

        tk.setMatKhauHash(passwordEncoder.encode(request.getNewPassword()));
        tk.setResetToken(null);
        tk.setResetTokenExpiry(null);
        taiKhoanRepository.save(tk);
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        TaiKhoan tk = taiKhoanRepository.findByVerifyToken(token)
                .orElseThrow(() -> new ValidationException("Token xác thực không hợp lệ"));

        tk.setLaKichHoat(true);
        tk.setVerifyToken(null);
        taiKhoanRepository.save(tk);
    }
}