package com.example.tuyendung.service.impl;

import com.example.tuyendung.dto.request.DangNhapRequest;
import com.example.tuyendung.dto.response.AuthResponse;
import com.example.tuyendung.entity.NhaTuyenDung;
import com.example.tuyendung.entity.TaiKhoan;
import com.example.tuyendung.entity.UngVien;
import com.example.tuyendung.entity.enums.VaiTroTaiKhoan;
import com.example.tuyendung.exception.BaseBusinessException;
import com.example.tuyendung.exception.ErrorCode;
import com.example.tuyendung.repository.NhaTuyenDungRepository;
import com.example.tuyendung.repository.TaiKhoanRepository;
import com.example.tuyendung.repository.UngVienRepository;
import com.example.tuyendung.security.CustomUserDetails;
import com.example.tuyendung.security.JwtTokenProvider;
import com.example.tuyendung.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final UngVienRepository ungVienRepository;
    private final NhaTuyenDungRepository nhaTuyenDungRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse dangNhap(DangNhapRequest request) {
        // Authentication – BadCredentialsException được handle bởi GlobalExceptionHandler
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMatKhau())
        );

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        TaiKhoan taiKhoan = customUserDetails.getTaiKhoan();

        // Kiểm tra tài khoản đã kích hoạt chưa
        if (!taiKhoan.getLaKichHoat()) {
            throw new BaseBusinessException(ErrorCode.ACCOUNT_NOT_ACTIVATED);
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

        log.info("Đăng nhập thành công: {} ({})", taiKhoan.getEmail(), taiKhoan.getVaiTro());

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
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        // Xác thực token – trả mã lỗi chuẩn thay vì string thô
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BaseBusinessException(ErrorCode.INVALID_TOKEN);
        }

        Long taiKhoanId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        TaiKhoan taiKhoan = taiKhoanRepository.findById(taiKhoanId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.USER_NOT_FOUND));

        if (!taiKhoan.getLaKichHoat()) {
            throw new BaseBusinessException(ErrorCode.ACCOUNT_LOCKED);
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
    public void logout(String token) {
        // Với JWT stateless, ta có thể yêu cầu client xoá token.
        // Thực tế có thể lưu token vào Redis blacklist. Ở đây giả lập thành công.
        log.info("User đã đăng xuất khỏi hệ thống.");
    }
}