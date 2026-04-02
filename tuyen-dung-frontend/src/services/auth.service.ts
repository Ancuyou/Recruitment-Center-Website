import api from './api';
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
  /** POST /api/auth/dang-nhap */
  dangNhap: async (payload: DangNhapRequest): Promise<AuthResponse> => {
    const res = await api.post<ApiResponse<AuthResponse>>(`${AUTH_URL}/dang-nhap`, payload);
    return res.data.data;
  },

  /** POST /api/auth/dang-ky-ung-vien */
  dangKyUngVien: async (payload: DangKyUngVienRequest): Promise<AuthResponse> => {
    const res = await api.post<ApiResponse<AuthResponse>>(`${AUTH_URL}/dang-ky-ung-vien`, payload);
    return res.data.data;
  },

  /** POST /api/auth/dang-ky-nha-tuyen-dung */
  dangKyNhaTuyenDung: async (payload: DangKyNhaTuyenDungRequest): Promise<AuthResponse> => {
    const res = await api.post<ApiResponse<AuthResponse>>(`${AUTH_URL}/dang-ky-nha-tuyen-dung`, payload);
    return res.data.data;
  },

  /** POST /api/auth/refresh-token?refreshToken=... */
  refreshToken: async (refreshToken: string): Promise<AuthResponse> => {
    const res = await api.post<ApiResponse<AuthResponse>>(
      `${AUTH_URL}/refresh-token`,
      null,
      { params: { refreshToken } }
    );
    return res.data.data;
  },

  /**
   * GET /api/users/profile — canonical endpoint lấy thông tin người dùng hiện tại.
   * Thay thế /api/auth/me đã bỏ.
   */
  getProfile: async (): Promise<UserInfoResponse> => {
    const res = await api.get<ApiResponse<UserInfoResponse>>(`${USERS_URL}/profile`);
    return res.data.data;
  },

  /**
   * @deprecated Dùng getProfile() thay thế. Giữ lại để tương thích ngược.
   * GET /api/users/profile
   */
  getMe: async (): Promise<UserInfoResponse> => {
    const res = await api.get<ApiResponse<UserInfoResponse>>(`${USERS_URL}/profile`);
    return res.data.data;
  },

  /** GET /api/auth/thong-tin/:taiKhoanId */
  getThongTin: async (taiKhoanId: number): Promise<UserInfoResponse> => {
    const res = await api.get<ApiResponse<UserInfoResponse>>(`${AUTH_URL}/thong-tin/${taiKhoanId}`);
    return res.data.data;
  },
};
