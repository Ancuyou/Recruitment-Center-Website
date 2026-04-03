import api from '@/services/api';
import type { ApiResponse, PageResponse } from '@/types/api.types';
import type {
  JobStatistics,
  JobPosting,
  JobSearchParams,
  JobUpsertRequest,
  JobSkill,
  KhuVuc,
} from '@/types/job.types';

const JOBS_URL = '/api/jobs';

function cleanParams<T extends Record<string, unknown>>(params: T): Partial<T> {
  const entries = Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== '');
  return Object.fromEntries(entries) as Partial<T>;
}

export const jobService = {
  createJob: async (payload: JobUpsertRequest): Promise<JobPosting> => {
    const res = await api.post<ApiResponse<JobPosting>>(JOBS_URL, payload);
    return res.data.data;
  },

  getMyJobs: async (): Promise<JobPosting[]> => {
    const res = await api.get<ApiResponse<JobPosting[]>>(`${JOBS_URL}/my-jobs`);
    return res.data.data;
  },

  getActiveJobs: async (page = 0, size = 10): Promise<PageResponse<JobPosting>> => {
    const res = await api.get<ApiResponse<PageResponse<JobPosting>>>(JOBS_URL, {
      params: { page, size },
    });
    return res.data.data;
  },

  searchJobs: async (params: JobSearchParams): Promise<PageResponse<JobPosting>> => {
    const res = await api.get<ApiResponse<PageResponse<JobPosting>>>(`${JOBS_URL}/search`, {
      params: cleanParams({
        keyword: params.keyword,
        capBac: params.capBac,
        hinhThuc: params.hinhThuc,
        mucLuongMin: params.mucLuongMin,
        page: params.page ?? 0,
        size: params.size ?? 10,
      }),
    });
    return res.data.data;
  },

  getJobById: async (id: number): Promise<JobPosting> => {
    const res = await api.get<ApiResponse<JobPosting>>(`${JOBS_URL}/${id}`);
    return res.data.data;
  },

  updateJob: async (id: number, payload: JobUpsertRequest): Promise<JobPosting> => {
    const res = await api.put<ApiResponse<JobPosting>>(`${JOBS_URL}/${id}`, payload);
    return res.data.data;
  },

  closeJob: async (id: number): Promise<void> => {
    await api.delete<ApiResponse<void>>(`${JOBS_URL}/${id}`);
  },

  getJobLocations: async (id: number): Promise<KhuVuc[]> => {
    const res = await api.get<ApiResponse<KhuVuc[]>>(`${JOBS_URL}/${id}/locations`);
    return res.data.data;
  },

  updateJobLocations: async (id: number, locations: KhuVuc[]): Promise<JobPosting> => {
    const res = await api.put<ApiResponse<JobPosting>>(`${JOBS_URL}/${id}/locations`, locations);
    return res.data.data;
  },

  getJobSkills: async (id: number): Promise<JobSkill[]> => {
    const res = await api.get<ApiResponse<JobSkill[]>>(`${JOBS_URL}/${id}/skills`);
    return res.data.data;
  },

  getJobStatistics: async (id: number): Promise<JobStatistics> => {
    const res = await api.get<ApiResponse<JobStatistics>>(`${JOBS_URL}/${id}/statistics`);
    return res.data.data;
  },
};
