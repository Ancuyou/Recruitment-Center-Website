import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { AuthUser, VaiTroTaiKhoan } from '@/types/auth.types';
import type { AuthResponse } from '@/types/auth.types';

interface AuthState {
  user: AuthUser | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;

  // Actions
  setAuth: (response: AuthResponse) => void;
  logout: () => void;
  updateUser: (partial: Partial<AuthUser>) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,

      setAuth: (response: AuthResponse) => {
        // Sync tokens to localStorage for Axios interceptor
        localStorage.setItem('accessToken', response.accessToken);
        localStorage.setItem('refreshToken', response.refreshToken);

        set({
          isAuthenticated: true,
          accessToken: response.accessToken,
          refreshToken: response.refreshToken,
          user: {
            taiKhoanId: response.taiKhoanId,
            email: response.email,
            vaiTro: response.vaiTro,
            hoTen: response.userInfo?.hoTen ?? '',
            anhDaiDien: response.userInfo?.anhDaiDien,
            tenCongTy: response.userInfo?.tenCongTy,
          },
        });
      },

      logout: () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        set({ user: null, accessToken: null, refreshToken: null, isAuthenticated: false });
      },

      updateUser: (partial) =>
        set((state) => ({
          user: state.user ? { ...state.user, ...partial } : null,
        })),
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);

// Selector helpers
export const selectUser = (s: AuthState) => s.user;
export const selectRole = (s: AuthState): VaiTroTaiKhoan | null => s.user?.vaiTro ?? null;
export const selectIsAuthenticated = (s: AuthState) => s.isAuthenticated;
