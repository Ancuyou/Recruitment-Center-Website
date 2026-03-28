package com.example.tuyendung.service;

import com.example.tuyendung.dto.response.DashboardCandidateResponse;
import com.example.tuyendung.dto.response.DashboardRecruiterResponse;

public interface DashboardService {
    DashboardCandidateResponse getCandidateDashboard(Long taiKhoanId);
    DashboardRecruiterResponse getRecruiterDashboard(Long taiKhoanId);
}
