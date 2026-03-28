package com.example.tuyendung.service.util;

import com.example.tuyendung.entity.DonUngTuyen;
import com.example.tuyendung.entity.NhaTuyenDung;
import com.example.tuyendung.entity.UngVien;
import com.example.tuyendung.entity.enums.VaiTroTaiKhoan;
import com.example.tuyendung.exception.BusinessException;
import com.example.tuyendung.exception.UnauthorizedApplicationAccessException;
import com.example.tuyendung.repository.NhaTuyenDungRepository;
import com.example.tuyendung.repository.TaiKhoanRepository;
import com.example.tuyendung.repository.UngVienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Utility class để kiểm tra quyền truy cập đơn ứng tuyển.
 *
 * SOLID - DRY: tập trung logic kiểm tra quyền để tránh duplicate code
 *         trong các service (DonUngTuyenServiceImpl, LichPhongVanServiceImpl, LichSuTrangThaiServiceImpl).
 *
 * Design Pattern: Shared Service Utility
 */
@Component
@RequiredArgsConstructor
public class ApplicationAccessVerifier {

    private final UngVienRepository ungVienRepository;
    private final NhaTuyenDungRepository nhaTuyenDungRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    /**
     * Verify người dùng có quyền truy cập đơn ứng tuyển.
     *
     * @param don         Đơn ứng tuyển cần kiểm tra
     * @param taiKhoanId  ID tài khoản người dùng
     * @throws UnauthorizedApplicationAccessException nếu không có quyền
     */
    public void verifyApplicationAccess(DonUngTuyen don, Long taiKhoanId) {
        var tk = taiKhoanRepository.findById(taiKhoanId)
                .orElseThrow(() -> new BusinessException("Tài khoản không hợp lệ"));

        if (tk.getVaiTro() == VaiTroTaiKhoan.UNG_VIEN) {
            verifyUngVienOwnsApplication(don, taiKhoanId);
        } else if (tk.getVaiTro() == VaiTroTaiKhoan.NHA_TUYEN_DUNG) {
            verifyRecruiterOwnsApplication(don, taiKhoanId);
        }
        // ADMIN: không chặn
    }

    /**
     * Verify ứng viên là chủ sở hữu của đơn ứng tuyển.
     */
    public void verifyUngVienOwnsApplication(DonUngTuyen don, Long taiKhoanId) {
        UngVien uv = ungVienRepository.findByTaiKhoanId(taiKhoanId).orElse(null);
        if (uv == null || !don.getHoSoCv().getUngVien().getId().equals(uv.getId())) {
            throw new UnauthorizedApplicationAccessException(don.getId());
        }
    }

    /**
     * Verify nhà tuyển dụng sở hữu tin liên quan đến đơn.
     */
    public void verifyRecruiterOwnsApplication(DonUngTuyen don, Long taiKhoanId) {
        NhaTuyenDung ntd = findNhaTuyenDungByTaiKhoan(taiKhoanId);
        if (!don.getTinTuyenDung().getNhaTuyenDung().getId().equals(ntd.getId())) {
            throw new UnauthorizedApplicationAccessException(don.getId());
        }
    }

    /**
     * Find NhaTuyenDung by taiKhoanId, throw exception if not found.
     */
    public NhaTuyenDung findNhaTuyenDungByTaiKhoan(Long taiKhoanId) {
        return nhaTuyenDungRepository.findByTaiKhoanId(taiKhoanId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy nhà tuyển dụng"));
    }

    /**
     * Find UngVien by taiKhoanId, throw exception if not found.
     */
    public UngVien findUngVienByTaiKhoan(Long taiKhoanId) {
        return ungVienRepository.findByTaiKhoanId(taiKhoanId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy ứng viên"));
    }
}
