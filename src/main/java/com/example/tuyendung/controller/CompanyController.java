package com.example.tuyendung.controller;

import com.example.tuyendung.common.ApiResponse;
import com.example.tuyendung.common.JwtTokenExtractor;
import com.example.tuyendung.dto.request.CongTyRequest;
import com.example.tuyendung.dto.response.CongTyResponse;
import com.example.tuyendung.entity.enums.NganhNgheEnum;
import com.example.tuyendung.service.CongTyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import java.util.List;
import java.util.Set;

/**
 * Controller cho Công ty (B1-B8)
 *
 * B7: PUT /api/companies/{id}/industries — set toàn bộ ngành nghề (enum)
 * B8: GET /api/companies/{id}/industries — lấy ngành nghề (enum set)
 */
@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CongTyService congTyService;
    private final JwtTokenExtractor jwtTokenExtractor;

    @PostMapping
    @PreAuthorize("hasAnyRole('NHA_TUYEN_DUNG', 'ADMIN')")
    public ResponseEntity<ApiResponse<CongTyResponse>> createCongTy(
            @Valid @RequestBody CongTyRequest request,
            @RequestHeader("Authorization") String authorization) {
        Long taiKhoanId = jwtTokenExtractor.extractUserIdFromToken(authorization);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo công ty thành công",
                        congTyService.createCongTy(request, taiKhoanId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CongTyResponse>>> getAllCongTy() {
        return ResponseEntity.ok(ApiResponse.success("OK", congTyService.getAllCongTy()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CongTyResponse>> getCongTyById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("OK", congTyService.getCongTyById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('NHA_TUYEN_DUNG', 'ADMIN')")
    public ResponseEntity<ApiResponse<CongTyResponse>> updateCongTy(
            @PathVariable Long id,
            @Valid @RequestBody CongTyRequest request,
            @RequestHeader("Authorization") String authorization) {
        Long taiKhoanId = jwtTokenExtractor.extractUserIdFromToken(authorization);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công",
                congTyService.updateCongTy(id, request, taiKhoanId)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCongTy(@PathVariable Long id) {
        congTyService.deleteCongTy(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa công ty thành công", null));
    }

    /** B6: GET /api/companies/verify-tax?maSoThue=... */
    @GetMapping("/verify-tax")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> verifyMaSoThue(@RequestParam String maSoThue) {
        boolean exists = congTyService.verifyMaSoThue(maSoThue);
        return ResponseEntity.ok(ApiResponse.success(
                exists ? "Mã số thuế đã tồn tại" : "Mã số thuế chưa được đăng ký", exists));
    }

    /**
     * B7: PUT /api/companies/{id}/industries
     * Body: ["CONG_NGHE_THONG_TIN","GIAO_DUC_DAO_TAO"] — set enum names
     */
    @PutMapping("/{id}/industries")
    @PreAuthorize("hasAnyRole('NHA_TUYEN_DUNG', 'ADMIN')")
    public ResponseEntity<ApiResponse<CongTyResponse>> updateNganhNghes(
            @PathVariable Long id,
            @RequestBody Set<NganhNgheEnum> nganhNghes,
            @RequestHeader("Authorization") String authorization) {
        Long taiKhoanId = jwtTokenExtractor.extractUserIdFromToken(authorization);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật ngành nghề thành công",
                congTyService.updateNganhNghes(id, nganhNghes, taiKhoanId)));
    }

    /** B8: GET /api/companies/{id}/industries */
    @GetMapping("/{id}/industries")
    public ResponseEntity<ApiResponse<Set<NganhNgheEnum>>> getNganhNgheOfCongTy(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("OK",
                congTyService.getNganhNgheOfCongTy(id)));
    }
}
