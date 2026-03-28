package com.example.tuyendung.service.impl;

import com.example.tuyendung.dto.request.ChangePasswordRequest;
import com.example.tuyendung.dto.request.UpdateCandidateProfileRequest;
import com.example.tuyendung.dto.request.UpdateRecruiterProfileRequest;
import com.example.tuyendung.dto.request.UploadAvatarRequest;
import com.example.tuyendung.dto.response.UserInfoResponse;
import com.example.tuyendung.entity.NhaTuyenDung;
import com.example.tuyendung.entity.TaiKhoan;
import com.example.tuyendung.entity.UngVien;
import com.example.tuyendung.entity.enums.VaiTroTaiKhoan;
import com.example.tuyendung.exception.ResourceNotFoundException;
import com.example.tuyendung.exception.ValidationException;
import com.example.tuyendung.repository.NhaTuyenDungRepository;
import com.example.tuyendung.repository.TaiKhoanRepository;
import com.example.tuyendung.repository.UngVienRepository;
import com.example.tuyendung.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final UngVienRepository ungVienRepository;
    private final NhaTuyenDungRepository nhaTuyenDungRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserInfoResponse getUserProfile(Long taiKhoanId) {
        TaiKhoan tk = taiKhoanRepository.findById(taiKhoanId)
                .orElseThrow(() -> new ResourceNotFoundException("TaiKhoan", taiKhoanId));
        return mapToUserInfoResponse(tk);
    }

    @Override
    @Transactional
    public UserInfoResponse updateCandidateProfile(Long taiKhoanId, UpdateCandidateProfileRequest request) {
        UngVien uv = ungVienRepository.findByTaiKhoanId(taiKhoanId)
                .orElseThrow(() -> new ValidationException("Tài khoản", "không phải Ứng Viên"));

        uv.setHoTen(request.getHoTen());
        uv.setSoDienThoai(request.getSoDienThoai());
        uv.setGioiTinh(request.getGioiTinh());
        uv.setNgaySinh(request.getNgaySinh());

        ungVienRepository.save(uv);
        return mapToUserInfoResponse(uv.getTaiKhoan());
    }

    @Override
    @Transactional
    public UserInfoResponse updateRecruiterProfile(Long taiKhoanId, UpdateRecruiterProfileRequest request) {
        NhaTuyenDung ntd = nhaTuyenDungRepository.findByTaiKhoanId(taiKhoanId)
                .orElseThrow(() -> new ValidationException("Tài khoản", "không phải Nhà Tuyển Dụng"));

        ntd.setHoTen(request.getHoTen());
        ntd.setSoDienThoai(request.getSoDienThoai());
        ntd.setChucVu(request.getChucVu());

        nhaTuyenDungRepository.save(ntd);
        return mapToUserInfoResponse(ntd.getTaiKhoan());
    }

    @Override
    @Transactional
    public void changePassword(Long taiKhoanId, ChangePasswordRequest request) {
        TaiKhoan tk = taiKhoanRepository.findById(taiKhoanId)
                .orElseThrow(() -> new ResourceNotFoundException("TaiKhoan", taiKhoanId));

        if (!passwordEncoder.matches(request.getOldPassword(), tk.getMatKhauHash())) {
            throw new ValidationException("Mật khẩu cũ không chính xác");
        }

        tk.setMatKhauHash(passwordEncoder.encode(request.getNewPassword()));
        taiKhoanRepository.save(tk);
    }

    @Override
    @Transactional
    public UserInfoResponse updateAvatar(Long taiKhoanId, UploadAvatarRequest request) {
        TaiKhoan tk = taiKhoanRepository.findById(taiKhoanId)
                .orElseThrow(() -> new ResourceNotFoundException("TaiKhoan", taiKhoanId));

        if (tk.getVaiTro() == VaiTroTaiKhoan.UNG_VIEN) {
            UngVien uv = tk.getUngVien();
            if (uv != null) {
                uv.setAnhDaiDien(request.getAvatarUrl());
                ungVienRepository.save(uv);
            }
        } else {
            NhaTuyenDung ntd = tk.getNhaTuyenDung();
            if (ntd != null && ntd.getCongTy() != null) {
                ntd.getCongTy().setLogoUrl(request.getAvatarUrl());
            }
        }

        return mapToUserInfoResponse(tk);
    }

    private UserInfoResponse mapToUserInfoResponse(TaiKhoan tk) {
        UserInfoResponse res = new UserInfoResponse();
        res.setTaiKhoanId(tk.getId());
        res.setEmail(tk.getEmail());
        res.setVaiTro(tk.getVaiTro());

        if (tk.getVaiTro() == VaiTroTaiKhoan.UNG_VIEN && tk.getUngVien() != null) {
            res.setHoTen(tk.getUngVien().getHoTen());
            res.setSoDienThoai(tk.getUngVien().getSoDienThoai());
            res.setAnhDaiDien(tk.getUngVien().getAnhDaiDien());
        } else if (tk.getNhaTuyenDung() != null) {
            res.setHoTen(tk.getNhaTuyenDung().getHoTen());
            res.setSoDienThoai(tk.getNhaTuyenDung().getSoDienThoai());
            if (tk.getNhaTuyenDung().getCongTy() != null) {
                res.setAnhDaiDien(tk.getNhaTuyenDung().getCongTy().getLogoUrl());
                res.setTenCongTy(tk.getNhaTuyenDung().getCongTy().getTenCongTy());
            }
        }
        return res;
    }
}
