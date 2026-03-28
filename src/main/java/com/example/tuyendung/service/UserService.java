package com.example.tuyendung.service;

import com.example.tuyendung.dto.request.ChangePasswordRequest;
import com.example.tuyendung.dto.request.UpdateCandidateProfileRequest;
import com.example.tuyendung.dto.request.UpdateRecruiterProfileRequest;
import com.example.tuyendung.dto.request.UploadAvatarRequest;
import com.example.tuyendung.dto.response.UserInfoResponse;

public interface UserService {
    // A9
    UserInfoResponse getUserProfile(Long taiKhoanId);
    
    // A10
    UserInfoResponse updateCandidateProfile(Long taiKhoanId, UpdateCandidateProfileRequest request);
    UserInfoResponse updateRecruiterProfile(Long taiKhoanId, UpdateRecruiterProfileRequest request);
    
    // A11
    void changePassword(Long taiKhoanId, ChangePasswordRequest request);
    
    // A12
    UserInfoResponse updateAvatar(Long taiKhoanId, UploadAvatarRequest request);
}
