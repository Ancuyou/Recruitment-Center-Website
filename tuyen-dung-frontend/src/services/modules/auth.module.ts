import api from '@/services/api';
import type { ApiResponse } from '@/types/api.types';
import type {
  DangNhapRequest,
  DangKyUngVienRequest,
  DangKyNhaTuyenDungRequest,
  AuthResponse,
  UserInfoResponse,
} from '@/types/auth.types';

const AUTH_URL = '/api/auth';
const USERS_URL = '/api/users';

export const authService = {
  dangNhap: async (payload: DangNhapRequest): Promise<AuthResponse> => {
    const res = await api.post<ApiResponse<AuthResponse>>(`${AUTH_URL}/dang-nhap`, payload);
    return res.data.data;
  },

  dangKyUngVien: async (payload: DangKyUngVienRequest): Promise<AuthResponse> => {
    const res = await api.post<ApiResponse<AuthResponse>>(`${AUTH_URL}/dang-ky-ung-vien`, payload);
    return res.data.data;
  },

  dangKyNhaTuyenDung: async (payload: DangKyNhaTuyenDungRequest): Promise<AuthResponse> => {
    const res = await api.post<ApiResponse<AuthResponse>>(`${AUTH_URL}/dang-ky-nha-tuyen-dung`, payload);
    return res.data.data;
  },

  refreshToken: async (refreshToken: string): Promise<AuthResponse> => {
    const res = await api.post<ApiResponse<AuthResponse>>(
      `${AUTH_URL}/refresh-token`,
      null,
      { params: { refreshToken } }
    );
    return res.data.data;
  },

  logout: async (): Promise<void> => {
    await api.post<ApiResponse<void>>(`${AUTH_URL}/dang-xuat`);
  },

  forgotPassword: async (email: string): Promise<void> => {
    await api.post<ApiResponse<void>>(`${AUTH_URL}/forgot-password`, { email });
  },

  resetPassword: async (token: string, newPassword: string): Promise<void> => {
    await api.post<ApiResponse<void>>(`${AUTH_URL}/reset-password`, { token, newPassword });
  },

  verifyEmail: async (token: string): Promise<void> => {
    await api.get<ApiResponse<void>>(`${AUTH_URL}/verify-email`, { params: { token } });
  },

  getProfile: async (): Promise<UserInfoResponse> => {
    const res = await api.get<ApiResponse<UserInfoResponse>>(`${USERS_URL}/profile`);
    return res.data.data;
  },

  getMe: async (): Promise<UserInfoResponse> => {
    const res = await api.get<ApiResponse<UserInfoResponse>>(`${USERS_URL}/profile`);
    return res.data.data;
  },

  getThongTin: async (taiKhoanId: number): Promise<UserInfoResponse> => {
    const res = await api.get<ApiResponse<UserInfoResponse>>(`${AUTH_URL}/thong-tin/${taiKhoanId}`);
    return res.data.data;
  },
};
