package com.example.tuyendung.service.impl;

import com.example.tuyendung.dto.response.DashboardCandidateResponse;
import com.example.tuyendung.dto.response.DashboardRecruiterResponse;
import com.example.tuyendung.entity.NhaTuyenDung;
import com.example.tuyendung.entity.UngVien;
import com.example.tuyendung.entity.enums.TrangThaiDon;
import com.example.tuyendung.exception.BusinessException;
import com.example.tuyendung.repository.DonUngTuyenRepository;
import com.example.tuyendung.repository.LichPhongVanRepository;
import com.example.tuyendung.repository.NhaTuyenDungRepository;
import com.example.tuyendung.repository.ThongBaoRepository;
import com.example.tuyendung.repository.TinTuyenDungRepository;
import com.example.tuyendung.repository.UngVienRepository;
import com.example.tuyendung.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DonUngTuyenRepository donUngTuyenRepository;
    private final LichPhongVanRepository lichPhongVanRepository;
    private final ThongBaoRepository thongBaoRepository;
    private final TinTuyenDungRepository tinTuyenDungRepository;
    private final UngVienRepository ungVienRepository;
    private final NhaTuyenDungRepository nhaTuyenDungRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardCandidateResponse getCandidateDashboard(Long taiKhoanId) {
        UngVien uv = ungVienRepository.findByTaiKhoanId(taiKhoanId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy thông tin Ứng Viên"));

        long tongDon = donUngTuyenRepository.countByUngVienId(uv.getId());
        // Số đơn đang chờ = MỚI (MOI) + REVIEW
        long soDonMoi = donUngTuyenRepository.countByUngVienIdAndTrangThai(uv.getId(), TrangThaiDon.MOI.getValue());
        long soDonReview = donUngTuyenRepository.countByUngVienIdAndTrangThai(uv.getId(), TrangThaiDon.REVIEW.getValue());
        
        long soLich = lichPhongVanRepository.countUpcomingByUngVienId(uv.getId());
        long soThongBao = thongBaoRepository.countUnreadByTaiKhoanId(taiKhoanId);

        return DashboardCandidateResponse.builder()
                .tongSoDonDaNop(tongDon)
                .soDonDangCho(soDonMoi + soDonReview)
                .soLichPhongVan(soLich)
                .soThongBaoChuaDoc(soThongBao)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardRecruiterResponse getRecruiterDashboard(Long taiKhoanId) {
        NhaTuyenDung ntd = nhaTuyenDungRepository.findByTaiKhoanId(taiKhoanId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy thông tin Nhà Tuyển Dụng"));

        long tongTin = tinTuyenDungRepository.countActiveJobsByNhaTuyenDungId(ntd.getId());
        long tongDon = donUngTuyenRepository.countByNhaTuyenDungId(ntd.getId());
        long soDonMoi = donUngTuyenRepository.countByNhaTuyenDungIdAndTrangThai(ntd.getId(), TrangThaiDon.MOI.getValue());
        long soLich = lichPhongVanRepository.countUpcomingByNhaTuyenDungId(ntd.getId());

        return DashboardRecruiterResponse.builder()
                .tongSoTinDangMo(tongTin)
                .tongSoDonUngTuyen(tongDon)
                .soDonMoi(soDonMoi)
                .soLichPhongVanSapToi(soLich)
                .build();
    }
}
