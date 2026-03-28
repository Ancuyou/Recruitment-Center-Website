package com.example.tuyendung.service;

import com.example.tuyendung.dto.request.TinTuyenDungRequest;
import com.example.tuyendung.dto.response.JobStatisticsResponse;
import com.example.tuyendung.dto.response.TinTuyenDungResponse;
import com.example.tuyendung.entity.enums.CapBacYeuCau;
import com.example.tuyendung.entity.enums.HinhThucLamViec;
import com.example.tuyendung.entity.enums.KhuVucEnum;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Service interface cho quản lý Tin tuyển dụng (B13-B22)
 */
public interface TinTuyenDungService {

    /** B13: Recruiter đăng tin tuyển dụng */
    TinTuyenDungResponse createTin(TinTuyenDungRequest request, Long taiKhoanId);

    /** B14: Danh sách tin đang mở (public, phân trang) */
    Page<TinTuyenDungResponse> getActiveTins(int page, int size);

    /** B15: Chi tiết một tin (public) */
    TinTuyenDungResponse getTinById(Long id);

    /** B16: Cập nhật tin (chỉ recruiter sở hữu) */
    TinTuyenDungResponse updateTin(Long id, TinTuyenDungRequest request, Long taiKhoanId);

    /** B17: Đóng tin (set trangThai = 2) */
    void closeTin(Long id, Long taiKhoanId);

    /** B18: Lấy danh sách tin của recruiter đang đăng nhập */
    List<TinTuyenDungResponse> getMyTins(Long taiKhoanId);

    /** B19: Tìm kiếm & filter tin */
    Page<TinTuyenDungResponse> searchTins(
            String keyword,
            CapBacYeuCau capBac,
            HinhThucLamViec hinhThuc,
            BigDecimal mucLuongMin,
            int page, int size);

    /** B20: Cập nhật khu vực áp dụng của tin (set toàn bộ) */
    TinTuyenDungResponse updateKhuVucs(Long tinId, Set<KhuVucEnum> khuVucs, Long taiKhoanId);

    /** B21: Lấy danh sách khu vực của tin */
    Set<KhuVucEnum> getKhuVucsOfTin(Long tinId);

    /** B22: Thống kê đơn ứng tuyển của tin */
    JobStatisticsResponse getJobStatistics(Long tinId, Long taiKhoanId);
}
