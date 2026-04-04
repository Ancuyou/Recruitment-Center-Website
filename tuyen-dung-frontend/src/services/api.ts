import axios, {
  type AxiosInstance,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from 'axios';
import { ROUTES } from '@/constants/routes';
import type { ApiResponse } from '@/types/api.types';
import type { AuthResponse } from '@/types/auth.types';

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

const api: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

type RetriableRequest = InternalAxiosRequestConfig & { _retry?: boolean };

function clearAuthAndRedirect(): void {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('auth-storage');
  window.location.href = ROUTES.auth.login;
}

// ── Request interceptor: attach Bearer token ──
api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem('accessToken');
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  // Let the browser set multipart boundary for FormData requests.
  if (config.data instanceof FormData && config.headers) {
    delete config.headers['Content-Type'];
  }

  return config;
});

// ── Response interceptor: unwrap data & handle 401 ──
api.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => response,
  async (error) => {
    const originalRequest = error.config as RetriableRequest | undefined;
    const isUnauthorized = error.response?.status === 401;
    const refreshToken = localStorage.getItem('refreshToken');
    const isRefreshCall = String(originalRequest?.url || '').includes('/api/auth/refresh-token');

    if (isUnauthorized && originalRequest && !originalRequest._retry && refreshToken && !isRefreshCall) {
      originalRequest._retry = true;

      try {
        const refreshRes = await axios.post<ApiResponse<AuthResponse>>(
          `${BASE_URL}/api/auth/refresh-token`,
          null,
          {
            params: { refreshToken },
            timeout: 15000,
          }
        );

        const authData = refreshRes.data.data;
        localStorage.setItem('accessToken', authData.accessToken);
        localStorage.setItem('refreshToken', authData.refreshToken);

        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${authData.accessToken}`;
        }

        return api(originalRequest);
      } catch {
        clearAuthAndRedirect();
        return Promise.reject(error);
      }
    }

    if (isUnauthorized) {
      clearAuthAndRedirect();
    }

    return Promise.reject(error);
  }
);

export default api;
