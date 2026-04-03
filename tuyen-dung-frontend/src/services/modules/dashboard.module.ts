import api from '@/services/api';
import type { ApiResponse } from '@/types/api.types';
import type { RecruiterDashboardStats } from '@/types/dashboard.types';

const DASHBOARD_URL = '/api/dashboard';

export const dashboardService = {
  getRecruiterDashboard: async (): Promise<RecruiterDashboardStats> => {
    const res = await api.get<ApiResponse<RecruiterDashboardStats>>(`${DASHBOARD_URL}/recruiter`);
    return res.data.data;
  },
};
