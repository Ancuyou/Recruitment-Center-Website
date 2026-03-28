package com.example.tuyendung.service.impl;

import com.example.tuyendung.dto.response.ThongBaoResponse;
import com.example.tuyendung.entity.TaiKhoan;
import com.example.tuyendung.entity.ThongBao;
import com.example.tuyendung.entity.enums.LoaiThongBao;
import com.example.tuyendung.exception.BusinessException;
import com.example.tuyendung.repository.TaiKhoanRepository;
import com.example.tuyendung.repository.ThongBaoRepository;
import com.example.tuyendung.service.ThongBaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Triển khai ThongBaoService – Module D15-D18.
 *
 * Design Patterns áp dụng:
 *  - Repository Pattern: giao tiếp với DB qua ThongBaoRepository / TaiKhoanRepository
 *  - Builder Pattern: ThongBao.builder() để khởi tạo entity an toàn
 *
 * SOLID:
 *  - SRP: class chỉ xử lý logic thông báo
 *  - OCP: thêm loại thông báo → chỉ cần mở rộng LoaiThongBao enum
 *  - LSP: implement đúng contract ThongBaoService
 *  - ISP: ThongBaoService chỉ expose method cần thiết
 *  - DIP: phụ thuộc vào interface Repository, không phụ thuộc implement cụ thể
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThongBaoServiceImpl implements ThongBaoService {

    private final ThongBaoRepository thongBaoRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    @Override
    @Transactional
    public void createNotification(Long taiKhoanId, String tieuDe, String noiDung,
                                   LoaiThongBao loai, String lienKet) {
        TaiKhoan tk = taiKhoanRepository.findById(taiKhoanId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản người nhận"));

        ThongBao tb = ThongBao.builder()
                .taiKhoan(tk)
                .tieuDe(tieuDe)
                .noiDung(noiDung)
                .loaiThongBao(loai)
                .lienKet(lienKet)
                .daDoc(false)
                .build();

        thongBaoRepository.save(tb);
        log.debug("Đã tạo thông báo [{}] cho tài khoản {}", loai, taiKhoanId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ThongBaoResponse> getMyNotifications(Long taiKhoanId, int page, int size) {
        return thongBaoRepository
                .findByTaiKhoanIdOrderByNgayTaoDesc(taiKhoanId, PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(Long taiKhoanId) {
        return thongBaoRepository.countUnreadByTaiKhoanId(taiKhoanId);
    }

    @Override
    @Transactional
    public void markAsRead(Long id, Long taiKhoanId) {
        ThongBao tb = thongBaoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy thông báo"));

        if (!tb.getTaiKhoan().getId().equals(taiKhoanId)) {
            throw new BusinessException(403, "Không có quyền thao tác thông báo này");
        }

        tb.setDaDoc(true);
        thongBaoRepository.save(tb);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long taiKhoanId) {
        // Dùng bulk UPDATE query để tránh N+1 write problem
        thongBaoRepository.markAllAsReadByTaiKhoanId(taiKhoanId);
        log.debug("Đã đánh dấu tất cả thông báo là đã đọc cho tài khoản {}", taiKhoanId);
    }

    // ---- Helpers ----

    private ThongBaoResponse mapToResponse(ThongBao tb) {
        return ThongBaoResponse.builder()
                .id(tb.getId())
                .tieuDe(tb.getTieuDe())
                .noiDung(tb.getNoiDung())
                .loaiThongBao(tb.getLoaiThongBao())
                .lienKet(tb.getLienKet())
                .daDoc(tb.getDaDoc())
                .ngayTao(tb.getNgayTao())
                .build();
    }
}
