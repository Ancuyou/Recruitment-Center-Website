import api from '@/services/api';
import type { ApiResponse, PageResponse } from '@/types/api.types';
import type {
  ApplicationItem,
  InterviewRescheduleRequest,
  InterviewUpsertRequest,
  ApplicationStatusHistoryItem,
  InterviewItem,
  SubmitApplicationRequest,
  UpdateApplicationStatusRequest,
} from '@/types/application.types';

const APPLICATIONS_URL = '/api/applications';
const INTERVIEWS_URL = '/api/interviews';

export const applicationService = {
  submitApplication: async (payload: SubmitApplicationRequest): Promise<ApplicationItem> => {
    const res = await api.post<ApiResponse<ApplicationItem>>(APPLICATIONS_URL, payload);
    return res.data.data;
  },

  getCandidateApplications: async (page = 0, size = 10): Promise<PageResponse<ApplicationItem>> => {
    const res = await api.get<ApiResponse<PageResponse<ApplicationItem>>>(APPLICATIONS_URL, {
      params: { page, size },
    });
    return res.data.data;
  },

  getRecruiterApplications: async (
    tinTuyenDungId: number,
    page = 0,
    size = 10
  ): Promise<PageResponse<ApplicationItem>> => {
    const res = await api.get<ApiResponse<PageResponse<ApplicationItem>>>(`${APPLICATIONS_URL}/recruiter`, {
      params: { tinTuyenDungId, page, size },
    });
    return res.data.data;
  },

  getApplicationDetail: async (id: number): Promise<ApplicationItem> => {
    const res = await api.get<ApiResponse<ApplicationItem>>(`${APPLICATIONS_URL}/${id}`);
    return res.data.data;
  },

  updateApplicationStatus: async (
    id: number,
    payload: UpdateApplicationStatusRequest
  ): Promise<ApplicationItem> => {
    const res = await api.patch<ApiResponse<ApplicationItem>>(`${APPLICATIONS_URL}/${id}/status`, payload);
    return res.data.data;
  },

  rejectApplication: async (id: number, ghiChu?: string): Promise<ApplicationItem> => {
    const res = await api.patch<ApiResponse<ApplicationItem>>(`${APPLICATIONS_URL}/${id}/reject`, null, {
      params: { ghiChu },
    });
    return res.data.data;
  },

  getCvSnapshotUrl: async (id: number): Promise<string> => {
    const res = await api.get<ApiResponse<string>>(`${APPLICATIONS_URL}/${id}/cv-snapshot`);
    return res.data.data;
  },

  getApplicationHistory: async (
    id: number,
    page = 0,
    size = 10
  ): Promise<PageResponse<ApplicationStatusHistoryItem>> => {
    const res = await api.get<ApiResponse<PageResponse<ApplicationStatusHistoryItem>>>(
      `${APPLICATIONS_URL}/${id}/history`,
      {
        params: { page, size },
      }
    );
    return res.data.data;
  },

  getInterviewsByApplication: async (applicationId: number): Promise<InterviewItem[]> => {
    const res = await api.get<ApiResponse<InterviewItem[]>>(`${INTERVIEWS_URL}/${applicationId}/list`);
    return res.data.data;
  },

  getInterviewById: async (id: number): Promise<InterviewItem> => {
    const res = await api.get<ApiResponse<InterviewItem>>(`${INTERVIEWS_URL}/${id}`);
    return res.data.data;
  },

  getMyInterviews: async (page = 0, size = 10): Promise<PageResponse<InterviewItem>> => {
    const res = await api.get<ApiResponse<PageResponse<InterviewItem>>>(`${INTERVIEWS_URL}/my`, {
      params: { page, size },
    });
    return res.data.data;
  },

  createInterview: async (payload: InterviewUpsertRequest): Promise<InterviewItem> => {
    const res = await api.post<ApiResponse<InterviewItem>>(INTERVIEWS_URL, payload);
    return res.data.data;
  },

  updateInterview: async (id: number, payload: InterviewUpsertRequest): Promise<InterviewItem> => {
    const res = await api.put<ApiResponse<InterviewItem>>(`${INTERVIEWS_URL}/${id}`, payload);
    return res.data.data;
  },

  rescheduleInterview: async (
    id: number,
    payload: InterviewRescheduleRequest
  ): Promise<InterviewItem> => {
    const res = await api.patch<ApiResponse<InterviewItem>>(`${INTERVIEWS_URL}/${id}/reschedule`, payload);
    return res.data.data;
  },

  cancelInterview: async (id: number): Promise<void> => {
    await api.delete<ApiResponse<void>>(`${INTERVIEWS_URL}/${id}`);
  },
};
