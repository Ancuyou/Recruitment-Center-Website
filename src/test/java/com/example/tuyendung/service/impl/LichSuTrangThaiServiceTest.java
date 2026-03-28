package com.example.tuyendung.service.impl;

import com.example.tuyendung.dto.response.LichSuTrangThaiResponse;
import com.example.tuyendung.entity.*;
import com.example.tuyendung.entity.enums.TrangThaiDon;
import com.example.tuyendung.exception.ApplicationNotFoundException;
import com.example.tuyendung.exception.UnauthorizedApplicationAccessException;
import com.example.tuyendung.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho LichSuTrangThaiServiceImpl – Module D8.
 *
 * Test Coverage:
 *  - D8: getStatusHistory (phân trang + authorization)
 *
 * SOLID:
 *  - SRP: Mỗi test chỉ test 1 scenario
 *  - Guard Clause Pattern: Verify authorization checks
 *
 * Clean Code:
 *  - Descriptive test names
 *  - AAA Pattern: Arrange → Act → Assert
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LichSuTrangThaiService Unit Tests")
public class LichSuTrangThaiServiceTest {

    @Mock
    private LichSuTrangThaiRepository lichSuTrangThaiRepository;
    
    @Mock
    private DonUngTuyenRepository donUngTuyenRepository;
    
    @Mock
    private UngVienRepository ungVienRepository;
    
    @Mock
    private NhaTuyenDungRepository nhaTuyenDungRepository;

    @InjectMocks
    private LichSuTrangThaiServiceImpl lichSuTrangThaiService;

    private DonUngTuyen don;
    private UngVien ungVien;
    private TaiKhoan taiKhoanUV;
    private LichSuTrangThai lichSu1;
    private LichSuTrangThai lichSu2;

    @BeforeEach
    void setUp() {
        // Setup test data
        taiKhoanUV = new TaiKhoan();
        taiKhoanUV.setId(1L);
        taiKhoanUV.setEmail("uv@example.com");

        ungVien = new UngVien();
        ungVien.setId(1L);
        ungVien.setTaiKhoan(taiKhoanUV);
        ungVien.setHoTen("Nguyễn Văn A");

        HoSoCv cv = new HoSoCv();
        cv.setId(1L);
        cv.setUngVien(ungVien);

        CongTy congTy = new CongTy();
        congTy.setId(1L);
        congTy.setTenCongTy("TechCorp");

        TinTuyenDung tin = new TinTuyenDung();
        tin.setId(1L);
        tin.setTieuDe("Software Engineer");
        tin.setCongTy(congTy);

        don = new DonUngTuyen();
        don.setId(1L);
        don.setHoSoCv(cv);
        don.setTinTuyenDung(tin);

        // Status history records
        TaiKhoan taiKhoanAdmin = new TaiKhoan();
        taiKhoanAdmin.setId(2L);

        lichSu1 = new LichSuTrangThai();
        lichSu1.setId(1L);
        lichSu1.setDonUngTuyen(don);
        lichSu1.setTrangThaiCu(null);
        lichSu1.setTrangThaiMoi(TrangThaiDon.MOI.getValue());
        lichSu1.setGhiChu("Ứng viên nộp đơn");
        lichSu1.setNguoiThucHien(taiKhoanAdmin);
        lichSu1.setThoiGianChuyen(LocalDateTime.now().minusHours(2));

        lichSu2 = new LichSuTrangThai();
        lichSu2.setId(2L);
        lichSu2.setDonUngTuyen(don);
        lichSu2.setTrangThaiCu(TrangThaiDon.MOI.getValue());
        lichSu2.setTrangThaiMoi(TrangThaiDon.REVIEW.getValue());
        lichSu2.setGhiChu("HR bắt đầu xem xét");
        lichSu2.setNguoiThucHien(taiKhoanAdmin);
        lichSu2.setThoiGianChuyen(LocalDateTime.now().minusHours(1));
    }

    // =========================================================================
    // D8: getStatusHistory
    // =========================================================================

    @Test
    @DisplayName("D8: getStatusHistory - Success (candidate owner, paginated)")
    void testGetStatusHistorySuccessCandidate() {
        // Arrange
        when(donUngTuyenRepository.findByIdWithDetails(1L))
                .thenReturn(Optional.of(don));
        when(ungVienRepository.findByTaiKhoanId(1L))
                .thenReturn(Optional.of(ungVien));
        when(lichSuTrangThaiRepository.findByDonUngTuyenIdOrderByThoiGianChuyenDesc(1L))
                .thenReturn(Arrays.asList(lichSu2, lichSu1));

        // Act
        Page<LichSuTrangThaiResponse> result = lichSuTrangThaiService.getStatusHistory(1L, 1L, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals(0, result.getNumber()); // page index
    }

    @Test
    @DisplayName("D8: getStatusHistory - Success (HR owner, paginated)")
    void testGetStatusHistorySuccessRecruiter() {
        // Arrange
        TaiKhoan taiKhoanHR = new TaiKhoan();
        taiKhoanHR.setId(2L);

        NhaTuyenDung ntd = new NhaTuyenDung();
        ntd.setId(1L);
        ntd.setTaiKhoan(taiKhoanHR);

        don.getTinTuyenDung().setNhaTuyenDung(ntd);

        when(donUngTuyenRepository.findByIdWithDetails(1L))
                .thenReturn(Optional.of(don));
        when(nhaTuyenDungRepository.findByTaiKhoanId(2L))
                .thenReturn(Optional.of(ntd));
        when(lichSuTrangThaiRepository.findByDonUngTuyenIdOrderByThoiGianChuyenDesc(1L))
                .thenReturn(Arrays.asList(lichSu2, lichSu1));

        // Act
        Page<LichSuTrangThaiResponse> result = lichSuTrangThaiService.getStatusHistory(1L, 2L, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
    }

    @Test
    @DisplayName("D8: getStatusHistory - ApplicationNotFoundException (đơn không tồn tại)")
    void testGetStatusHistoryNotFound() {
        // Arrange
        when(donUngTuyenRepository.findByIdWithDetails(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApplicationNotFoundException.class, () -> {
            lichSuTrangThaiService.getStatusHistory(999L, 1L, 0, 10);
        });
    }

    @Test
    @DisplayName("D8: getStatusHistory - UnauthorizedApplicationAccessException (người dùng không có quyền)")
    void testGetStatusHistoryUnauthorized() {
        // Arrange
        when(donUngTuyenRepository.findByIdWithDetails(1L))
                .thenReturn(Optional.of(don));
        when(ungVienRepository.findByTaiKhoanId(999L))
                .thenReturn(Optional.empty()); // Not a candidate
        when(nhaTuyenDungRepository.findByTaiKhoanId(999L))
                .thenReturn(Optional.empty()); // Not a recruiter

        // Act & Assert
        assertThrows(UnauthorizedApplicationAccessException.class, () -> {
            lichSuTrangThaiService.getStatusHistory(1L, 999L, 0, 10);
        });
    }

    @Test
    @DisplayName("D8: getStatusHistory - Empty history (pagination)")
    void testGetStatusHistoryEmpty() {
        // Arrange
        when(donUngTuyenRepository.findByIdWithDetails(1L))
                .thenReturn(Optional.of(don));
        when(ungVienRepository.findByTaiKhoanId(1L))
                .thenReturn(Optional.of(ungVien));
        when(lichSuTrangThaiRepository.findByDonUngTuyenIdOrderByThoiGianChuyenDesc(1L))
                .thenReturn(Arrays.asList());

        // Act
        Page<LichSuTrangThaiResponse> result = lichSuTrangThaiService.getStatusHistory(1L, 1L, 0, 10);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }
}
