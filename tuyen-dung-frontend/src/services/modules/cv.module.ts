import api from '@/services/api';
import type { ApiResponse } from '@/types/api.types';
import type {
  CvDetailItem,
  CvDetailRequest,
  CvItem,
  CvRequest,
  LoaiBanGhiCv,
  CvSkillItem,
  CvSkillRequest,
} from '@/types/cv.types';

const CVS_URL = '/api/cvs';

export const cvService = {
  getMyCvs: async (): Promise<CvItem[]> => {
    const res = await api.get<ApiResponse<CvItem[]>>(CVS_URL);
    return res.data.data;
  },

  getCvById: async (id: number): Promise<CvItem> => {
    const res = await api.get<ApiResponse<CvItem>>(`${CVS_URL}/${id}`);
    return res.data.data;
  },

  createCv: async (payload: CvRequest): Promise<CvItem> => {
    const res = await api.post<ApiResponse<CvItem>>(CVS_URL, payload);
    return res.data.data;
  },

  updateCv: async (id: number, payload: CvRequest): Promise<CvItem> => {
    const res = await api.put<ApiResponse<CvItem>>(`${CVS_URL}/${id}`, payload);
    return res.data.data;
  },

  deleteCv: async (id: number): Promise<void> => {
    await api.delete<ApiResponse<void>>(`${CVS_URL}/${id}`);
  },

  setDefaultCv: async (id: number): Promise<void> => {
    await api.post<ApiResponse<void>>(`${CVS_URL}/${id}/set-default`);
  },

  uploadCvFile: async (id: number, file: File): Promise<CvItem> => {
    const formData = new FormData();
    formData.append('file', file);

    const res = await api.post<ApiResponse<CvItem>>(`${CVS_URL}/${id}/upload-file`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return res.data.data;
  },

  getCvSkills: async (cvId: number): Promise<CvSkillItem[]> => {
    const res = await api.get<ApiResponse<CvSkillItem[]>>(`${CVS_URL}/${cvId}/skills`);
    return res.data.data;
  },

  addCvSkill: async (cvId: number, payload: CvSkillRequest): Promise<CvSkillItem> => {
    const res = await api.post<ApiResponse<CvSkillItem>>(`${CVS_URL}/${cvId}/skills`, payload);
    return res.data.data;
  },

  updateCvSkill: async (
    cvId: number,
    skillId: number,
    payload: CvSkillRequest
  ): Promise<CvSkillItem> => {
    const res = await api.put<ApiResponse<CvSkillItem>>(`${CVS_URL}/${cvId}/skills/${skillId}`, payload);
    return res.data.data;
  },

  deleteCvSkill: async (cvId: number, skillId: number): Promise<void> => {
    await api.delete<ApiResponse<void>>(`${CVS_URL}/${cvId}/skills/${skillId}`);
  },

  getCvDetails: async (cvId: number): Promise<CvDetailItem[]> => {
    const res = await api.get<ApiResponse<CvDetailItem[]>>(`${CVS_URL}/${cvId}/hoc-van-kn`);
    return res.data.data;
  },

  getCvDetailsByType: async (cvId: number, loaiBanGhi: LoaiBanGhiCv): Promise<CvDetailItem[]> => {
    const res = await api.get<ApiResponse<CvDetailItem[]>>(
      `${CVS_URL}/${cvId}/hoc-van-kn/type/${loaiBanGhi}`
    );
    return res.data.data;
  },

  addCvDetail: async (cvId: number, payload: CvDetailRequest): Promise<CvDetailItem> => {
    const res = await api.post<ApiResponse<CvDetailItem>>(`${CVS_URL}/${cvId}/hoc-van-kn`, payload);
    return res.data.data;
  },

  updateCvDetail: async (
    cvId: number,
    detailId: number,
    payload: CvDetailRequest
  ): Promise<CvDetailItem> => {
    const res = await api.put<ApiResponse<CvDetailItem>>(
      `${CVS_URL}/${cvId}/hoc-van-kn/${detailId}`,
      payload
    );
    return res.data.data;
  },

  deleteCvDetail: async (cvId: number, detailId: number): Promise<void> => {
    await api.delete<ApiResponse<void>>(`${CVS_URL}/${cvId}/hoc-van-kn/${detailId}`);
  },
};
