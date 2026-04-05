import api from '@/services/api';
import type { ApiResponse } from '@/types/api.types';
import type {
  AdminUserCreateRequest,
  AdminUserQuery,
  AdminUserResponse,
  AdminUserUpdateRequest,
  ChangePasswordRequest,
  UpdateCandidateProfileRequest,
  UpdateRecruiterProfileRequest,
  UploadAvatarRequest,
  UserProfileResponse,
} from '@/types/user.types';

const USERS_URL = '/api/users';
const ADMIN_USERS_URL = '/api/admin/users';

export const userService = {
  getProfile: async (): Promise<UserProfileResponse> => {
    const res = await api.get<ApiResponse<UserProfileResponse>>(`${USERS_URL}/profile`);
    return res.data.data;
  },

  updateCandidateProfile: async (
    payload: UpdateCandidateProfileRequest
  ): Promise<UserProfileResponse> => {
    const res = await api.put<ApiResponse<UserProfileResponse>>(
      `${USERS_URL}/profile/candidate`,
      payload
    );
    return res.data.data;
  },

  updateRecruiterProfile: async (
    payload: UpdateRecruiterProfileRequest
  ): Promise<UserProfileResponse> => {
    const res = await api.put<ApiResponse<UserProfileResponse>>(
      `${USERS_URL}/profile/recruiter`,
      payload
    );
    return res.data.data;
  },

  changePassword: async (payload: ChangePasswordRequest): Promise<void> => {
    await api.put<ApiResponse<void>>(`${USERS_URL}/change-password`, payload);
  },

  uploadAvatar: async (payload: UploadAvatarRequest): Promise<UserProfileResponse> => {
    const res = await api.post<ApiResponse<UserProfileResponse>>(`${USERS_URL}/upload-avatar`, payload);
    return res.data.data;
  },

  listAdminUsers: async (query?: AdminUserQuery): Promise<AdminUserResponse[]> => {
    const res = await api.get<ApiResponse<AdminUserResponse[]>>(ADMIN_USERS_URL, {
      params: {
        keyword: query?.keyword,
        vaiTro: query?.vaiTro,
        laKichHoat: query?.laKichHoat,
      },
    });
    return res.data.data;
  },

  getAdminUserById: async (taiKhoanId: number): Promise<AdminUserResponse> => {
    const res = await api.get<ApiResponse<AdminUserResponse>>(`${ADMIN_USERS_URL}/${taiKhoanId}`);
    return res.data.data;
  },

  createAdminUser: async (payload: AdminUserCreateRequest): Promise<AdminUserResponse> => {
    const res = await api.post<ApiResponse<AdminUserResponse>>(ADMIN_USERS_URL, payload);
    return res.data.data;
  },

  updateAdminUser: async (
    taiKhoanId: number,
    payload: AdminUserUpdateRequest
  ): Promise<AdminUserResponse> => {
    const res = await api.put<ApiResponse<AdminUserResponse>>(`${ADMIN_USERS_URL}/${taiKhoanId}`, payload);
    return res.data.data;
  },

  lockAdminUser: async (taiKhoanId: number): Promise<AdminUserResponse> => {
    const res = await api.patch<ApiResponse<AdminUserResponse>>(`${ADMIN_USERS_URL}/${taiKhoanId}/lock`);
    return res.data.data;
  },

  unlockAdminUser: async (taiKhoanId: number): Promise<AdminUserResponse> => {
    const res = await api.patch<ApiResponse<AdminUserResponse>>(`${ADMIN_USERS_URL}/${taiKhoanId}/unlock`);
    return res.data.data;
  },

  deleteAdminUser: async (taiKhoanId: number): Promise<void> => {
    await api.delete<ApiResponse<void>>(`${ADMIN_USERS_URL}/${taiKhoanId}`);
  },
};
