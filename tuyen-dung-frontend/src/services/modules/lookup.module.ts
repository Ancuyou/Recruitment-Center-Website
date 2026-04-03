import api from '@/services/api';
import type { ApiResponse } from '@/types/api.types';
import type { LookupItem } from '@/types/lookup.types';

type SkillDto = {
  id: number;
  tenKyNang: string;
};

export const lookupService = {
  getIndustries: async (): Promise<LookupItem[]> => {
    const res = await api.get<ApiResponse<LookupItem[]>>('/api/industries');
    return res.data.data;
  },

  getLocations: async (): Promise<LookupItem[]> => {
    const res = await api.get<ApiResponse<LookupItem[]>>('/api/locations');
    return res.data.data;
  },

  getSkills: async (): Promise<LookupItem[]> => {
    const res = await api.get<ApiResponse<SkillDto[]>>('/api/skills');
    return res.data.data.map((item) => ({
      value: String(item.id),
      label: item.tenKyNang,
    }));
  },
};
