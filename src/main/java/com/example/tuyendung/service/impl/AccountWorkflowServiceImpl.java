package com.example.tuyendung.service.impl;

import com.example.tuyendung.config.AuthProperties;
import com.example.tuyendung.dto.request.ForgotPasswordRequest;
import com.example.tuyendung.dto.request.ResetPasswordRequest;
import com.example.tuyendung.entity.PendingPasswordReset;
import com.example.tuyendung.entity.PendingRegistration;
import com.example.tuyendung.entity.TaiKhoan;
import com.example.tuyendung.exception.BaseBusinessException;
import com.example.tuyendung.exception.ErrorCode;
import com.example.tuyendung.repository.PendingPasswordResetRepository;
import com.example.tuyendung.repository.PendingRegistrationRepository;
import com.example.tuyendung.repository.TaiKhoanRepository;
import com.example.tuyendung.service.AccountWorkflowService;
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
public class AccountWorkflowServiceImpl implements AccountWorkflowService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final PendingPasswordResetRepository pendingPasswordResetRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties authProperties;

    @Override
    @Transactional
    public void verifyEmail(String token) {
        PendingRegistration pendingReg = pendingRegistrationRepository.findByToken(token)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.INVALID_TOKEN, "Token xác thực không hợp lệ hoặc không tồn tại."));

        if (pendingReg.getNgayHetHan().isBefore(LocalDateTime.now())) {
            throw new BaseBusinessException(ErrorCode.INVALID_TOKEN, "Token đã hết hạn. Vui lòng đăng ký lại.");
        }

        TaiKhoan tk = pendingReg.getTaiKhoan();
        tk.setLaKichHoat(true);
        taiKhoanRepository.save(tk);

        pendingRegistrationRepository.delete(pendingReg);
        log.info("A8 (SRP): Token xác thực email {} thành công, tài khoản {} đã được kích hoạt.", token, tk.getEmail());
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        TaiKhoan tk = taiKhoanRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.USER_NOT_FOUND));

        // Tuỳ chọn: xoá token cũ nếu có
        pendingPasswordResetRepository.deleteByTaiKhoan(tk);

        String resetToken = UUID.randomUUID().toString();
        PendingPasswordReset pendingReset = PendingPasswordReset.builder()
                .token(resetToken)
                .taiKhoan(tk)
                .ngayHetHan(LocalDateTime.now().plusMinutes(authProperties.getResetExpirationMinutes()))
                .build();
        pendingPasswordResetRepository.save(pendingReset);

        log.info("A6 (SRP): Vui lòng click vào link sau để đặt lại mật khẩu của bạn (Hạn {} phút): http://localhost:8080/api/auth/reset-password?token={}", 
                 authProperties.getResetExpirationMinutes(), resetToken);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PendingPasswordReset pendingReset = pendingPasswordResetRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.INVALID_TOKEN));

        if (pendingReset.getNgayHetHan().isBefore(LocalDateTime.now())) {
            throw new BaseBusinessException(ErrorCode.INVALID_TOKEN, "Token đã hết hạn");
        }

        TaiKhoan tk = pendingReset.getTaiKhoan();
        tk.setMatKhauHash(passwordEncoder.encode(request.getNewPassword()));
        taiKhoanRepository.save(tk);
        
        pendingPasswordResetRepository.delete(pendingReset);
        log.info("A7 (SRP): Token khôi phục mật khẩu {} thành công, tài khoản {} đổi pass thành công.", request.getToken(), tk.getEmail());
    }
}
