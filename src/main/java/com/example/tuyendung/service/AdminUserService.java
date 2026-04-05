package com.example.tuyendung.service;

import com.example.tuyendung.dto.request.AdminUserCreateRequest;
import com.example.tuyendung.dto.request.AdminUserUpdateRequest;
import com.example.tuyendung.dto.response.AdminUserResponse;
import com.example.tuyendung.entity.enums.VaiTroTaiKhoan;

import java.util.List;

public interface AdminUserService {
    List<AdminUserResponse> getUsers(String keyword, VaiTroTaiKhoan vaiTro, Boolean laKichHoat);

    AdminUserResponse getUserById(Long taiKhoanId);

    AdminUserResponse createUser(AdminUserCreateRequest request);

    AdminUserResponse updateUser(Long taiKhoanId, AdminUserUpdateRequest request);

    AdminUserResponse lockUser(Long taiKhoanId, Long actorTaiKhoanId);

    AdminUserResponse unlockUser(Long taiKhoanId);

    void deleteUser(Long taiKhoanId, Long actorTaiKhoanId);
}
