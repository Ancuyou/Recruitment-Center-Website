package com.example.tuyendung.service;

import com.example.tuyendung.dto.request.CongTyRequest;
import com.example.tuyendung.dto.response.CongTyResponse;
import com.example.tuyendung.entity.enums.NganhNgheEnum;

import java.util.List;
import java.util.Set;

/**
 * Service interface cho quản lý Công ty (B1-B8)
 *
 * SOLID — DIP: Controllers depend on this interface
 */
public interface CongTyService {

    /** B1: Tạo công ty mới */
    CongTyResponse createCongTy(CongTyRequest request, Long taiKhoanId);

    /** B2: Danh sách tất cả công ty */
    List<CongTyResponse> getAllCongTy();

    /** B3: Chi tiết công ty */
    CongTyResponse getCongTyById(Long id);

    /** B4: Cập nhật thông tin công ty */
    CongTyResponse updateCongTy(Long id, CongTyRequest request, Long taiKhoanId);

    /** B5: Xóa công ty (cascade close all jobs — Admin only) */
    void deleteCongTy(Long id);

    /** B6: Kiểm tra mã số thuế đã tồn tại chưa */
    boolean verifyMaSoThue(String maSoThue);

    /** B7: Cập nhật (set) danh sách ngành nghề của công ty */
    CongTyResponse updateNganhNghes(Long congTyId, Set<NganhNgheEnum> nganhNghes, Long taiKhoanId);

    /** B8: Lấy danh sách ngành nghề của công ty */
    Set<NganhNgheEnum> getNganhNgheOfCongTy(Long congTyId);

    /** Helper: Lấy congTyId từ taiKhoanId */
    Long getCongTyIdByTaiKhoan(Long taiKhoanId);
}
