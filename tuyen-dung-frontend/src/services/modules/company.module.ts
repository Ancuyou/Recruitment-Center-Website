import api from '@/services/api';
import type { ApiResponse } from '@/types/api.types';
import type { CompanyItem, CompanyRequest } from '@/types/company.types';

const COMPANIES_URL = '/api/companies';

export const companyService = {
  getAllCompanies: async (): Promise<CompanyItem[]> => {
    const res = await api.get<ApiResponse<CompanyItem[]>>(COMPANIES_URL);
    return res.data.data;
  },

  getCompanyById: async (id: number): Promise<CompanyItem> => {
    const res = await api.get<ApiResponse<CompanyItem>>(`${COMPANIES_URL}/${id}`);
    return res.data.data;
  },

  createCompany: async (payload: CompanyRequest): Promise<CompanyItem> => {
    const res = await api.post<ApiResponse<CompanyItem>>(COMPANIES_URL, payload);
    return res.data.data;
  },

  updateCompany: async (id: number, payload: CompanyRequest): Promise<CompanyItem> => {
    const res = await api.put<ApiResponse<CompanyItem>>(`${COMPANIES_URL}/${id}`, payload);
    return res.data.data;
  },

  deleteCompany: async (id: number): Promise<void> => {
    await api.delete<ApiResponse<void>>(`${COMPANIES_URL}/${id}`);
  },

  verifyTaxCode: async (maSoThue: string): Promise<boolean> => {
    const res = await api.get<ApiResponse<boolean>>(`${COMPANIES_URL}/verify-tax`, {
      params: { maSoThue },
    });
    return res.data.data;
  },

  getCompanyIndustries: async (id: number): Promise<string[]> => {
    const res = await api.get<ApiResponse<string[]>>(`${COMPANIES_URL}/${id}/industries`);
    return res.data.data;
  },

  updateCompanyIndustries: async (id: number, industries: string[]): Promise<CompanyItem> => {
    const res = await api.put<ApiResponse<CompanyItem>>(`${COMPANIES_URL}/${id}/industries`, industries);
    return res.data.data;
  },
};
