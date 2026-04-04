import api from '@/services/api';
import type { ApiResponse } from '@/types/api.types';
import type { SkillItem, SkillUpsertRequest } from '@/types/skill.types';

const SKILLS_URL = '/api/skills';

export const skillService = {
  getAllSkills: async (): Promise<SkillItem[]> => {
    const res = await api.get<ApiResponse<SkillItem[]>>(SKILLS_URL);
    return res.data.data;
  },

  getSkillById: async (id: number): Promise<SkillItem> => {
    const res = await api.get<ApiResponse<SkillItem>>(`${SKILLS_URL}/${id}`);
    return res.data.data;
  },

  searchSkills: async (keyword: string): Promise<SkillItem[]> => {
    const res = await api.get<ApiResponse<SkillItem[]>>(`${SKILLS_URL}/search`, {
      params: { keyword },
    });
    return res.data.data;
  },

  createSkill: async (payload: SkillUpsertRequest): Promise<SkillItem> => {
    const res = await api.post<ApiResponse<SkillItem>>(SKILLS_URL, payload);
    return res.data.data;
  },

  updateSkill: async (id: number, payload: SkillUpsertRequest): Promise<SkillItem> => {
    const res = await api.put<ApiResponse<SkillItem>>(`${SKILLS_URL}/${id}`, payload);
    return res.data.data;
  },

  deleteSkill: async (id: number): Promise<void> => {
    await api.delete<ApiResponse<void>>(`${SKILLS_URL}/${id}`);
  },
};
