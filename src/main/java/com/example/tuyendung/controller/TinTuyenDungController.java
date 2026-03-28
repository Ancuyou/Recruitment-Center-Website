package com.example.tuyendung.controller;

import com.example.tuyendung.common.ApiResponse;
import com.example.tuyendung.common.JwtTokenExtractor;
import com.example.tuyendung.dto.request.TinTuyenDungRequest;
import com.example.tuyendung.dto.response.JobStatisticsResponse;
import com.example.tuyendung.dto.response.TinTuyenDungResponse;
import com.example.tuyendung.entity.enums.CapBacYeuCau;
import com.example.tuyendung.entity.enums.HinhThucLamViec;
import com.example.tuyendung.entity.enums.KhuVucEnum;
import com.example.tuyendung.service.TinTuyenDungService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Controller cho Tin tuyển dụng (B13-B22)
 *
 * IMPORTANT: /my-jobs và /search khai báo TRƯỚC /{id} để tránh conflict routing
 */
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class TinTuyenDungController {

    private final TinTuyenDungService tinTuyenDungService;
    private final JwtTokenExtractor jwtTokenExtractor;

    /** B13: Đăng tin */
    @PostMapping
    @PreAuthorize("hasRole('NHA_TUYEN_DUNG')")
    public ResponseEntity<ApiResponse<TinTuyenDungResponse>> createTin(
            @Valid @RequestBody TinTuyenDungRequest request,
            @RequestHeader("Authorization") String authorization) {
        Long taiKhoanId = jwtTokenExtractor.extractUserIdFromToken(authorization);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng tin thành công",
                        tinTuyenDungService.createTin(request, taiKhoanId)));
    }

    /** B18: Tin của recruiter (TRƯỚC /{id}) */
    @GetMapping("/my-jobs")
    @PreAuthorize("hasRole('NHA_TUYEN_DUNG')")
    public ResponseEntity<ApiResponse<List<TinTuyenDungResponse>>> getMyTins(
            @RequestHeader("Authorization") String authorization) {
        Long taiKhoanId = jwtTokenExtractor.extractUserIdFromToken(authorization);
        return ResponseEntity.ok(ApiResponse.success("OK",
                tinTuyenDungService.getMyTins(taiKhoanId)));
    }

    /** B19: Tìm kiếm (TRƯỚC /{id}) */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<TinTuyenDungResponse>>> searchTins(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CapBacYeuCau capBac,
            @RequestParam(required = false) HinhThucLamViec hinhThuc,
            @RequestParam(required = false) BigDecimal mucLuongMin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("OK",
                tinTuyenDungService.searchTins(keyword, capBac, hinhThuc, mucLuongMin, page, size)));
    }

    /** B14: Danh sách tin đang mở */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TinTuyenDungResponse>>> getActiveTins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("OK",
                tinTuyenDungService.getActiveTins(page, size)));
    }

    /** B15: Chi tiết tin */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TinTuyenDungResponse>> getTinById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("OK", tinTuyenDungService.getTinById(id)));
    }

    /** B16: Cập nhật tin */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('NHA_TUYEN_DUNG')")
    public ResponseEntity<ApiResponse<TinTuyenDungResponse>> updateTin(
            @PathVariable Long id,
            @Valid @RequestBody TinTuyenDungRequest request,
            @RequestHeader("Authorization") String authorization) {
        Long taiKhoanId = jwtTokenExtractor.extractUserIdFromToken(authorization);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công",
                tinTuyenDungService.updateTin(id, request, taiKhoanId)));
    }

    /** B17: Đóng tin */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('NHA_TUYEN_DUNG', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> closeTin(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorization) {
        Long taiKhoanId = jwtTokenExtractor.extractUserIdFromToken(authorization);
        tinTuyenDungService.closeTin(id, taiKhoanId);
        return ResponseEntity.ok(ApiResponse.success("Đóng tin thành công", null));
    }

    /**
     * B20: PUT /api/jobs/{id}/locations
     * Body: ["HA_NOI","HO_CHI_MINH"] — set toàn bộ khu vực áp dụng
     */
    @PutMapping("/{id}/locations")
    @PreAuthorize("hasRole('NHA_TUYEN_DUNG')")
    public ResponseEntity<ApiResponse<TinTuyenDungResponse>> updateKhuVucs(
            @PathVariable Long id,
            @RequestBody Set<KhuVucEnum> khuVucs,
            @RequestHeader("Authorization") String authorization) {
        Long taiKhoanId = jwtTokenExtractor.extractUserIdFromToken(authorization);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật khu vực thành công",
                tinTuyenDungService.updateKhuVucs(id, khuVucs, taiKhoanId)));
    }

    /** B21: Lấy khu vực của tin */
    @GetMapping("/{id}/locations")
    public ResponseEntity<ApiResponse<Set<KhuVucEnum>>> getKhuVucsOfTin(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("OK",
                tinTuyenDungService.getKhuVucsOfTin(id)));
    }

    /** B22: Thống kê đơn */
    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasAnyRole('NHA_TUYEN_DUNG', 'ADMIN')")
    public ResponseEntity<ApiResponse<JobStatisticsResponse>> getStatistics(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorization) {
        Long taiKhoanId = jwtTokenExtractor.extractUserIdFromToken(authorization);
        return ResponseEntity.ok(ApiResponse.success("OK",
                tinTuyenDungService.getJobStatistics(id, taiKhoanId)));
    }
}
