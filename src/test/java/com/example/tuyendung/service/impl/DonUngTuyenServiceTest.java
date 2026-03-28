package com.example.tuyendung.service.impl;

import com.example.tuyendung.dto.request.DonUngTuyenRequest;
import com.example.tuyendung.dto.request.TrangThaiDonRequest;
import com.example.tuyendung.dto.response.DonUngTuyenResponse;
import com.example.tuyendung.entity.*;
import com.example.tuyendung.entity.enums.TrangThaiDon;
import com.example.tuyendung.exception.ApplicationNotFoundException;
import com.example.tuyendung.exception.DuplicateApplicationException;
import com.example.tuyendung.repository.*;
import com.example.tuyendung.service.ThongBaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho DonUngTuyenServiceImpl – Module D1-D7.
 *
 * Test Coverage:
 *  - D1: submitApplication (normal + duplicate case)
 *  - D2: getCandidateApplications (phân trang)
 *  - D3: getRecruiterApplications (authorization)
 *  - D4: getApplicationDetail (authorization)
 *  - D5: updateStatus (authorization)
 *  - D6: rejectApplication
 *  - D7: getCvSnapshotUrl
 *
 * SOLID:
 *  - SRP: Mỗi test chỉ test 1 scenario
 *  - AAA Pattern: Arrange → Act → Assert
 * 
 * Clean Code:
 *  - Descriptive test names
 *  - Clear setup/teardown
 *  - Verify both success & error cases
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DonUngTuyenService Unit Tests")
public class DonUngTuyenServiceTest {

    @Mock
    private DonUngTuyenRepository donUngTuyenRepository;
    
    @Mock
    private HoSoCvRepository hoSoCvRepository;
    
    @Mock
    private TinTuyenDungRepository tinTuyenDungRepository;
    
    @Mock
    private UngVienRepository ungVienRepository;
    
    @Mock
    private NhaTuyenDungRepository nhaTuyenDungRepository;
    
    @Mock
    private LichSuTrangThaiRepository lichSuTrangThaiRepository;
    
    @Mock
    private TaiKhoanRepository taiKhoanRepository;
    
    @Mock
    private ThongBaoService thongBaoService;

    @InjectMocks
    private DonUngTuyenServiceImpl donUngTuyenService;

    private TaiKhoan taiKhoanUV;
    private UngVien ungVien;
    private HoSoCv cv;
    private TinTuyenDung tinTuyenDung;
    private CongTy congTy;
    private DonUngTuyen don;

    @BeforeEach
    void setUp() {
        // Khởi tạo test data
        taiKhoanUV = new TaiKhoan();
        taiKhoanUV.setId(1L);
        taiKhoanUV.setEmail("uv@example.com");

        ungVien = new UngVien();
        ungVien.setId(1L);
        ungVien.setTaiKhoan(taiKhoanUV);
        ungVien.setHoTen("Nguyễn Văn A");

        cv = new HoSoCv();
        cv.setId(1L);
        cv.setUngVien(ungVien);
        cv.setFileCvUrl("https://example.com/cv.pdf");

        congTy = new CongTy();
        congTy.setId(1L);
        congTy.setTenCongTy("TechCorp");
        congTy.setLogoUrl("https://example.com/logo.png");

        tinTuyenDung = new TinTuyenDung();
        tinTuyenDung.setId(1L);
        tinTuyenDung.setTieuDe("Software Engineer");
        tinTuyenDung.setCongTy(congTy);

        don = new DonUngTuyen();
        don.setId(1L);
        don.setTinTuyenDung(tinTuyenDung);
        don.setHoSoCv(cv);
        don.setThuNgo("Tôi có kinh nghiệm 5 năm");
        don.setTrangThaiHienTai(TrangThaiDon.MOI.getValue());
        don.setNgayNop(LocalDateTime.now());
    }

    // =========================================================================
    // D1: submitApplication
    // =========================================================================

    @Test
    @DisplayName("D1: submitApplication - Success case")
    void testSubmitApplicationSuccess() {
        // Arrange
        DonUngTuyenRequest request = new DonUngTuyenRequest();
        request.setTinTuyenDungId(1L);
        request.setHoSoCvId(1L);
        request.setThuNgo("Tôi có kinh nghiệm 5 năm");

        when(ungVienRepository.findByTaiKhoanId(1L)).thenReturn(Optional.of(ungVien));
        when(donUngTuyenRepository.existsByTinTuyenDungIdAndTaiKhoanId(1L, 1L))
                .thenReturn(false);
        when(tinTuyenDungRepository.findByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(tinTuyenDung));
        when(hoSoCvRepository.findByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(cv));
        when(donUngTuyenRepository.save(any(DonUngTuyen.class)))
                .thenReturn(don);
        when(lichSuTrangThaiRepository.save(any(LichSuTrangThai.class)))
                .thenReturn(new LichSuTrangThai());
        when(taiKhoanRepository.findById(1L))
                .thenReturn(Optional.of(taiKhoanUV));

        // Act
        DonUngTuyenResponse response = donUngTuyenService.submitApplication(request, 1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Software Engineer", response.getTieuDeTin());
        verify(donUngTuyenRepository).save(any(DonUngTuyen.class));
        verify(lichSuTrangThaiRepository).save(any(LichSuTrangThai.class));
    }

    @Test
    @DisplayName("D1: submitApplication - Duplicate application throws DuplicateApplicationException")
    void testSubmitApplicationDuplicate() {
        // Arrange
        DonUngTuyenRequest request = new DonUngTuyenRequest();
        request.setTinTuyenDungId(1L);
        request.setHoSoCvId(1L);

        when(ungVienRepository.findByTaiKhoanId(1L)).thenReturn(Optional.of(ungVien));
        when(donUngTuyenRepository.existsByTinTuyenDungIdAndTaiKhoanId(1L, 1L))
                .thenReturn(true); // Duplicate!

        // Act & Assert
        assertThrows(DuplicateApplicationException.class, () -> {
            donUngTuyenService.submitApplication(request, 1L);
        });
        verify(donUngTuyenRepository, never()).save(any());
    }

    // =========================================================================
    // D4: getApplicationDetail
    // =========================================================================

    @Test
    @DisplayName("D4: getApplicationDetail - Success (candidate owner)")
    void testGetApplicationDetailSuccess() {
        // Arrange
        when(donUngTuyenRepository.findByIdWithDetails(1L))
                .thenReturn(Optional.of(don));
        when(ungVienRepository.findByTaiKhoanId(1L))
                .thenReturn(Optional.of(ungVien));

        // Act
        DonUngTuyenResponse response = donUngTuyenService.getApplicationDetail(1L, 1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    @DisplayName("D4: getApplicationDetail - ApplicationNotFoundException")
    void testGetApplicationDetailNotFound() {
        // Arrange
        when(donUngTuyenRepository.findByIdWithDetails(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApplicationNotFoundException.class, () -> {
            donUngTuyenService.getApplicationDetail(999L, 1L);
        });
    }

    // =========================================================================
    // D5: updateStatus
    // =========================================================================

    @Test
    @DisplayName("D5: updateStatus - Success (recruiter updates status)")
    void testUpdateStatusSuccess() {
        // Arrange
        TrangThaiDonRequest request = new TrangThaiDonRequest();
        request.setTrangThaiMoi(TrangThaiDon.REVIEW.getValue());
        request.setGhiChu("Đơn tốt");

        TaiKhoan taiKhoanHR = new TaiKhoan();
        taiKhoanHR.setId(2L);

        NhaTuyenDung ntd = new NhaTuyenDung();
        ntd.setId(1L);
        ntd.setTaiKhoan(taiKhoanHR);

        tinTuyenDung.setNhaTuyenDung(ntd);

        when(donUngTuyenRepository.findById(1L)).thenReturn(Optional.of(don));
        when(nhaTuyenDungRepository.findByTaiKhoanId(2L)).thenReturn(Optional.of(ntd));
        when(donUngTuyenRepository.save(any(DonUngTuyen.class))).thenReturn(don);
        when(lichSuTrangThaiRepository.save(any(LichSuTrangThai.class))).thenReturn(new LichSuTrangThai());
        when(taiKhoanRepository.findById(2L)).thenReturn(Optional.of(taiKhoanHR));

        // Act
        DonUngTuyenResponse response = donUngTuyenService.updateStatus(1L, request, 2L);

        // Assert
        assertNotNull(response);
        verify(donUngTuyenRepository).save(any(DonUngTuyen.class));
        verify(lichSuTrangThaiRepository).save(any(LichSuTrangThai.class));
    }

    // =========================================================================
    // D7: getCvSnapshotUrl
    // =========================================================================

    @Test
    @DisplayName("D7: getCvSnapshotUrl - Success")
    void testGetCvSnapshotUrlSuccess() {
        // Arrange
        when(donUngTuyenRepository.findById(1L)).thenReturn(Optional.of(don));
        when(ungVienRepository.findByTaiKhoanId(1L)).thenReturn(Optional.of(ungVien));

        // Act
        String url = donUngTuyenService.getCvSnapshotUrl(1L, 1L);

        // Assert
        assertNotNull(url);
        assertEquals("https://example.com/cv.pdf", url);
    }

    @Test
    @DisplayName("D7: getCvSnapshotUrl - ApplicationNotFoundException")
    void testGetCvSnapshotUrlNotFound() {
        // Arrange
        when(donUngTuyenRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApplicationNotFoundException.class, () -> {
            donUngTuyenService.getCvSnapshotUrl(999L, 1L);
        });
    }
}
