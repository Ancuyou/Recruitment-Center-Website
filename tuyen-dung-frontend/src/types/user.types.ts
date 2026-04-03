import type { GioiTinh, UserInfoResponse } from '@/types/auth.types';

export interface UpdateCandidateProfileRequest {
  hoTen: string;
  soDienThoai?: string;
  gioiTinh?: GioiTinh;
  ngaySinh?: string;
}

export interface UpdateRecruiterProfileRequest {
  hoTen: string;
  soDienThoai?: string;
  chucVu: string;
}

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
}

export interface UploadAvatarRequest {
  avatarUrl: string;
}

export type UserProfileResponse = UserInfoResponse;
