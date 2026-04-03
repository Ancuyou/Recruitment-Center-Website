// ─── Enums matching backend ───────────────────────────────────────
export type VaiTroTaiKhoan = 'UNG_VIEN' | 'NHA_TUYEN_DUNG' | 'ADMIN';
export type GioiTinh = 'NAM' | 'NU' | 'KHAC';

// ─── Request DTOs ─────────────────────────────────────────────────
export interface DangNhapRequest {
  email: string;
  matKhau: string;
}

export interface DangKyUngVienRequest {
  email: string;
  matKhau: string;
  hoTen: string;
  soDienThoai?: string;
  ngaySinh?: string;       // ISO date string "YYYY-MM-DD"
  gioiTinh?: GioiTinh;
}

export interface DangKyNhaTuyenDungRequest {
  email: string;
  matKhau: string;
  hoTen: string;
  chucVu: string;
  soDienThoai?: string;
  maSoThue: string;
  tenCongTy: string;
  nganhNghe?: string;
  website?: string;
}

// ─── Response DTOs ────────────────────────────────────────────────
export interface UserInfoDTO {
  id: number;
  hoTen: string;
  soDienThoai?: string;
  anhDaiDien?: string;
  tenCongTy?: string;
}

export interface AuthResponse {
  taiKhoanId: number;
  email: string;
  vaiTro: VaiTroTaiKhoan;
  accessToken: string;
  refreshToken: string;
  userInfo: UserInfoDTO;
}

export interface UserInfoResponse {
  taiKhoanId: number;
  email: string;
  vaiTro: VaiTroTaiKhoan;
  hoTen: string;
  soDienThoai?: string;
  ngaySinh?: string;
  gioiTinh?: GioiTinh;
  anhDaiDien?: string;
  tenCongTy?: string;
  chucVu?: string;
}

// ─── Auth Store State ─────────────────────────────────────────────
export interface AuthUser {
  taiKhoanId: number;
  email: string;
  vaiTro: VaiTroTaiKhoan;
  hoTen: string;
  anhDaiDien?: string;
  tenCongTy?: string;
}
