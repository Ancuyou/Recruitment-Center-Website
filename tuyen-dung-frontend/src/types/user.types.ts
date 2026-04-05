import type { GioiTinh, UserInfoResponse } from '@/types/auth.types';
import type { VaiTroTaiKhoan } from '@/types/auth.types';

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

export interface AdminUserResponse {
  taiKhoanId: number;
  email: string;
  vaiTro: VaiTroTaiKhoan;
  laKichHoat: boolean;
  hoTen?: string;
  soDienThoai?: string;
  ngaySinh?: string;
  gioiTinh?: GioiTinh;
  congTyId?: number;
  tenCongTy?: string;
  chucVu?: string;
  ngayTao?: string;
  ngayCapNhat?: string;
}

export interface AdminUserCreateRequest {
  email: string;
  matKhau: string;
  vaiTro: VaiTroTaiKhoan;
  laKichHoat?: boolean;
  hoTen?: string;
  soDienThoai?: string;
  ngaySinh?: string;
  gioiTinh?: GioiTinh;
  congTyId?: number;
  chucVu?: string;
}

export interface AdminUserUpdateRequest {
  email?: string;
  matKhauMoi?: string;
  hoTen?: string;
  soDienThoai?: string;
  ngaySinh?: string;
  gioiTinh?: GioiTinh;
  congTyId?: number;
  chucVu?: string;
}

export interface AdminUserQuery {
  keyword?: string;
  vaiTro?: VaiTroTaiKhoan;
  laKichHoat?: boolean;
}
