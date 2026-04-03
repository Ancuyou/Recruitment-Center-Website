import api from '@/services/api';
import type { ApiResponse } from '@/types/api.types';
import type {
  ChangePasswordRequest,
  UpdateCandidateProfileRequest,
  UpdateRecruiterProfileRequest,
  UploadAvatarRequest,
  UserProfileResponse,
} from '@/types/user.types';

const USERS_URL = '/api/users';

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
};
