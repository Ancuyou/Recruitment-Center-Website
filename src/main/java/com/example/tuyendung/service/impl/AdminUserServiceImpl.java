package com.example.tuyendung.service.impl;

import com.example.tuyendung.dto.request.AdminUserCreateRequest;
import com.example.tuyendung.dto.request.AdminUserUpdateRequest;
import com.example.tuyendung.dto.response.AdminUserResponse;
import com.example.tuyendung.entity.CongTy;
import com.example.tuyendung.entity.NhaTuyenDung;
import com.example.tuyendung.entity.TaiKhoan;
import com.example.tuyendung.entity.UngVien;
import com.example.tuyendung.entity.enums.VaiTroTaiKhoan;
import com.example.tuyendung.exception.BaseBusinessException;
import com.example.tuyendung.exception.ErrorCode;
import com.example.tuyendung.repository.CongTyRepository;
import com.example.tuyendung.repository.NhaTuyenDungRepository;
import com.example.tuyendung.repository.TaiKhoanRepository;
import com.example.tuyendung.repository.TinTuyenDungRepository;
import com.example.tuyendung.repository.UngVienRepository;
import com.example.tuyendung.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private static final Pattern CANDIDATE_PHONE_PATTERN = Pattern.compile("^\\d{10,11}$");

    private final TaiKhoanRepository taiKhoanRepository;
    private final UngVienRepository ungVienRepository;
    private final NhaTuyenDungRepository nhaTuyenDungRepository;
    private final CongTyRepository congTyRepository;
    private final TinTuyenDungRepository tinTuyenDungRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserResponse> getUsers(String keyword, VaiTroTaiKhoan vaiTro, Boolean laKichHoat) {
        String normalizedKeyword = normalizeKeyword(keyword);

        return taiKhoanRepository.findAllWithProfiles().stream()
            .filter(tk -> tk.getVaiTro() != VaiTroTaiKhoan.ADMIN)
                .filter(tk -> vaiTro == null || tk.getVaiTro() == vaiTro)
                .filter(tk -> laKichHoat == null || Objects.equals(tk.getLaKichHoat(), laKichHoat))
                .filter(tk -> matchesKeyword(tk, normalizedKeyword))
                .sorted(Comparator.comparing(TaiKhoan::getId).reversed())
                .map(this::mapToAdminUserResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(Long taiKhoanId) {
        return mapToAdminUserResponse(findTaiKhoanWithProfiles(taiKhoanId));
    }

    @Override
    @Transactional
    public AdminUserResponse createUser(AdminUserCreateRequest request) {
        String normalizedEmail = normalizeRequired(request.getEmail(), "Email không được để trống");
        if (taiKhoanRepository.existsByEmail(normalizedEmail)) {
            throw new BaseBusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        TaiKhoan taiKhoan = TaiKhoan.builder()
                .email(normalizedEmail)
                .matKhauHash(passwordEncoder.encode(request.getMatKhau()))
                .vaiTro(request.getVaiTro())
                .laKichHoat(request.getLaKichHoat() == null ? Boolean.TRUE : request.getLaKichHoat())
                .build();
        TaiKhoan savedTaiKhoan = taiKhoanRepository.save(taiKhoan);

        switch (request.getVaiTro()) {
            case UNG_VIEN -> createCandidateProfile(savedTaiKhoan, request);
            case NHA_TUYEN_DUNG -> createRecruiterProfile(savedTaiKhoan, request);
            case ADMIN -> {
                // ADMIN account does not require profile tables.
            }
        }

        return mapToAdminUserResponse(findTaiKhoanWithProfiles(savedTaiKhoan.getId()));
    }

    @Override
    @Transactional
    public AdminUserResponse updateUser(Long taiKhoanId, AdminUserUpdateRequest request) {
        TaiKhoan taiKhoan = findTaiKhoanWithProfiles(taiKhoanId);

        if (request.getEmail() != null) {
            String normalizedEmail = normalizeRequired(request.getEmail(), "Email không được để trống");
            if (!normalizedEmail.equalsIgnoreCase(taiKhoan.getEmail()) && taiKhoanRepository.existsByEmail(normalizedEmail)) {
                throw new BaseBusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
            }
            taiKhoan.setEmail(normalizedEmail);
        }

        if (request.getMatKhauMoi() != null && !request.getMatKhauMoi().isBlank()) {
            taiKhoan.setMatKhauHash(passwordEncoder.encode(request.getMatKhauMoi().trim()));
        }

        switch (taiKhoan.getVaiTro()) {
            case UNG_VIEN -> updateCandidateProfile(taiKhoan, request);
            case NHA_TUYEN_DUNG -> updateRecruiterProfile(taiKhoan, request);
            case ADMIN -> {
                // ADMIN has no profile data to update.
            }
        }

        taiKhoanRepository.save(taiKhoan);
        return mapToAdminUserResponse(findTaiKhoanWithProfiles(taiKhoanId));
    }

    @Override
    @Transactional
    public AdminUserResponse lockUser(Long taiKhoanId, Long actorTaiKhoanId) {
        if (Objects.equals(taiKhoanId, actorTaiKhoanId)) {
            throw new BaseBusinessException(ErrorCode.VALIDATION_ERROR, "Không thể tự khóa tài khoản của chính mình");
        }

        TaiKhoan taiKhoan = findTaiKhoanWithProfiles(taiKhoanId);
        taiKhoan.setLaKichHoat(false);
        taiKhoanRepository.save(taiKhoan);
        return mapToAdminUserResponse(taiKhoan);
    }

    @Override
    @Transactional
    public AdminUserResponse unlockUser(Long taiKhoanId) {
        TaiKhoan taiKhoan = findTaiKhoanWithProfiles(taiKhoanId);
        taiKhoan.setLaKichHoat(true);
        taiKhoanRepository.save(taiKhoan);
        return mapToAdminUserResponse(taiKhoan);
    }

    @Override
    @Transactional
    public void deleteUser(Long taiKhoanId, Long actorTaiKhoanId) {
        if (Objects.equals(taiKhoanId, actorTaiKhoanId)) {
            throw new BaseBusinessException(ErrorCode.VALIDATION_ERROR, "Không thể tự xóa tài khoản của chính mình");
        }

        TaiKhoan taiKhoan = findTaiKhoanWithProfiles(taiKhoanId);

        if (taiKhoan.getVaiTro() == VaiTroTaiKhoan.NHA_TUYEN_DUNG && taiKhoan.getNhaTuyenDung() != null) {
            long activeJobs = tinTuyenDungRepository.countActiveJobsByNhaTuyenDungId(taiKhoan.getNhaTuyenDung().getId());
            if (activeJobs > 0) {
                throw new BaseBusinessException(
                        ErrorCode.VALIDATION_ERROR,
                        "Không thể xóa tài khoản nhà tuyển dụng đang sở hữu tin tuyển dụng"
                );
            }
        }

        try {
            taiKhoanRepository.delete(taiKhoan);
            taiKhoanRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new BaseBusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "Không thể xóa tài khoản do còn dữ liệu liên quan"
            );
        }
    }

    private TaiKhoan findTaiKhoanWithProfiles(Long taiKhoanId) {
        return taiKhoanRepository.findByIdWithProfiles(taiKhoanId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void createCandidateProfile(TaiKhoan taiKhoan, AdminUserCreateRequest request) {
        String hoTen = normalizeRequired(request.getHoTen(), "Họ tên ứng viên không được để trống");
        String phone = normalizeNullable(request.getSoDienThoai());
        ensureCandidatePhoneAvailable(phone, null);

        UngVien ungVien = new UngVien();
        ungVien.setTaiKhoan(taiKhoan);
        ungVien.setHoTen(hoTen);
        ungVien.setSoDienThoai(phone);
        ungVien.setNgaySinh(request.getNgaySinh());
        ungVien.setGioiTinh(request.getGioiTinh());
        ungVienRepository.save(ungVien);
    }

    private void createRecruiterProfile(TaiKhoan taiKhoan, AdminUserCreateRequest request) {
        String hoTen = normalizeRequired(request.getHoTen(), "Họ tên nhà tuyển dụng không được để trống");
        if (request.getCongTyId() == null) {
            throw new BaseBusinessException(ErrorCode.VALIDATION_ERROR, "Nhà tuyển dụng phải gắn với công ty");
        }

        CongTy congTy = congTyRepository.findById(request.getCongTyId())
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy công ty"));

        NhaTuyenDung nhaTuyenDung = new NhaTuyenDung();
        nhaTuyenDung.setTaiKhoan(taiKhoan);
        nhaTuyenDung.setCongTy(congTy);
        nhaTuyenDung.setHoTen(hoTen);
        nhaTuyenDung.setSoDienThoai(normalizeNullable(request.getSoDienThoai()));
        nhaTuyenDung.setChucVu(normalizeNullable(request.getChucVu()));
        nhaTuyenDungRepository.save(nhaTuyenDung);
    }

    private void updateCandidateProfile(TaiKhoan taiKhoan, AdminUserUpdateRequest request) {
        UngVien ungVien = taiKhoan.getUngVien();
        if (ungVien == null) {
            throw new BaseBusinessException(ErrorCode.VALIDATION_ERROR, "Tài khoản ứng viên chưa có hồ sơ ứng viên");
        }

        if (request.getHoTen() != null) {
            ungVien.setHoTen(normalizeRequired(request.getHoTen(), "Họ tên ứng viên không được để trống"));
        }

        if (request.getSoDienThoai() != null) {
            String phone = normalizeNullable(request.getSoDienThoai());
            ensureCandidatePhoneAvailable(phone, ungVien.getId());
            ungVien.setSoDienThoai(phone);
        }

        if (request.getNgaySinh() != null) {
            ungVien.setNgaySinh(request.getNgaySinh());
        }

        if (request.getGioiTinh() != null) {
            ungVien.setGioiTinh(request.getGioiTinh());
        }

        ungVienRepository.save(ungVien);
    }

    private void updateRecruiterProfile(TaiKhoan taiKhoan, AdminUserUpdateRequest request) {
        NhaTuyenDung nhaTuyenDung = taiKhoan.getNhaTuyenDung();
        if (nhaTuyenDung == null) {
            throw new BaseBusinessException(ErrorCode.VALIDATION_ERROR, "Tài khoản nhà tuyển dụng chưa có hồ sơ nhà tuyển dụng");
        }

        if (request.getHoTen() != null) {
            nhaTuyenDung.setHoTen(normalizeRequired(request.getHoTen(), "Họ tên nhà tuyển dụng không được để trống"));
        }

        if (request.getSoDienThoai() != null) {
            nhaTuyenDung.setSoDienThoai(normalizeNullable(request.getSoDienThoai()));
        }

        if (request.getChucVu() != null) {
            nhaTuyenDung.setChucVu(normalizeNullable(request.getChucVu()));
        }

        if (request.getCongTyId() != null) {
            CongTy congTy = congTyRepository.findById(request.getCongTyId())
                    .orElseThrow(() -> new BaseBusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy công ty"));
            nhaTuyenDung.setCongTy(congTy);
        }

        nhaTuyenDungRepository.save(nhaTuyenDung);
    }

    private void ensureCandidatePhoneAvailable(String phone, Long currentUngVienId) {
        if (phone == null) {
            return;
        }

        if (!CANDIDATE_PHONE_PATTERN.matcher(phone).matches()) {
            throw new BaseBusinessException(ErrorCode.VALIDATION_ERROR, "Số điện thoại ứng viên phải gồm 10-11 chữ số");
        }

        ungVienRepository.findBySoDienThoai(phone).ifPresent(existing -> {
            if (currentUngVienId == null || !existing.getId().equals(currentUngVienId)) {
                throw new BaseBusinessException(ErrorCode.DUPLICATE_RESOURCE, "Số điện thoại đã tồn tại");
            }
        });
    }

    private AdminUserResponse mapToAdminUserResponse(TaiKhoan tk) {
        AdminUserResponse.AdminUserResponseBuilder builder = AdminUserResponse.builder()
                .taiKhoanId(tk.getId())
                .email(tk.getEmail())
                .vaiTro(tk.getVaiTro())
                .laKichHoat(tk.getLaKichHoat())
                .ngayTao(tk.getNgayTao())
                .ngayCapNhat(tk.getNgayCapNhat());

        if (tk.getVaiTro() == VaiTroTaiKhoan.UNG_VIEN && tk.getUngVien() != null) {
            UngVien uv = tk.getUngVien();
            builder.hoTen(uv.getHoTen())
                    .soDienThoai(uv.getSoDienThoai())
                    .ngaySinh(uv.getNgaySinh())
                    .gioiTinh(uv.getGioiTinh());
        } else if (tk.getVaiTro() == VaiTroTaiKhoan.NHA_TUYEN_DUNG && tk.getNhaTuyenDung() != null) {
            NhaTuyenDung ntd = tk.getNhaTuyenDung();
            builder.hoTen(ntd.getHoTen())
                    .soDienThoai(ntd.getSoDienThoai())
                    .chucVu(ntd.getChucVu());
            if (ntd.getCongTy() != null) {
                builder.congTyId(ntd.getCongTy().getId())
                        .tenCongTy(ntd.getCongTy().getTenCongTy());
            }
        }

        return builder.build();
    }

    private boolean matchesKeyword(TaiKhoan tk, String keyword) {
        if (keyword == null) {
            return true;
        }

        String email = safeLower(tk.getEmail());
        String profileName = safeLower(resolveProfileName(tk));
        String companyName = safeLower(resolveCompanyName(tk));
        String idText = String.valueOf(tk.getId());

        return email.contains(keyword)
                || profileName.contains(keyword)
                || companyName.contains(keyword)
                || idText.contains(keyword);
    }

    private String resolveProfileName(TaiKhoan tk) {
        if (tk.getVaiTro() == VaiTroTaiKhoan.UNG_VIEN && tk.getUngVien() != null) {
            return tk.getUngVien().getHoTen();
        }
        if (tk.getVaiTro() == VaiTroTaiKhoan.NHA_TUYEN_DUNG && tk.getNhaTuyenDung() != null) {
            return tk.getNhaTuyenDung().getHoTen();
        }
        return null;
    }

    private String resolveCompanyName(TaiKhoan tk) {
        if (tk.getNhaTuyenDung() != null && tk.getNhaTuyenDung().getCongTy() != null) {
            return tk.getNhaTuyenDung().getCongTy().getTenCongTy();
        }
        return null;
    }

    private String normalizeKeyword(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim().toLowerCase(Locale.ROOT);
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeRequired(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new BaseBusinessException(ErrorCode.VALIDATION_ERROR, errorMessage);
        }
        return value.trim();
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
