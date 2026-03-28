package com.example.tuyendung.controller;

import com.example.tuyendung.common.ApiResponse;
import com.example.tuyendung.dto.response.DashboardCandidateResponse;
import com.example.tuyendung.dto.response.DashboardRecruiterResponse;
import com.example.tuyendung.security.CustomUserDetails;
import com.example.tuyendung.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // D19
    @PreAuthorize("hasRole('CANDIDATE') or hasRole('UNG_VIEN')")
    @GetMapping("/candidate")
    public ResponseEntity<ApiResponse<DashboardCandidateResponse>> getCandidateDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("D19: Thống kê Dashboard Candidate");
        return ResponseEntity.ok(ApiResponse.success("Thống kê tổng quan UV",
                dashboardService.getCandidateDashboard(userDetails.getId())));
    }

    // D20
    @PreAuthorize("hasRole('RECRUITER') or hasRole('NHA_TUYEN_DUNG')")
    @GetMapping("/recruiter")
    public ResponseEntity<ApiResponse<DashboardRecruiterResponse>> getRecruiterDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("D20: Thống kê Dashboard Recruiter");
        return ResponseEntity.ok(ApiResponse.success("Thống kê tổng quan NTD",
                dashboardService.getRecruiterDashboard(userDetails.getId())));
    }
}
