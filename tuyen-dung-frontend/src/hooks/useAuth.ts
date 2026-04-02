import { useAuthStore } from '@/store/auth.store';
import { authService } from '@/services/auth.service';

/**
 * Hook to access auth state and actions.
 * Wraps Zustand store + service calls.
 *
 * getProfile() — lấy thông tin người dùng hiện tại từ GET /api/users/profile
 */
export function useAuth() {
  const { user, isAuthenticated, setAuth, logout } = useAuthStore();

  const getProfile = async () => {
    try {
      return await authService.getProfile();
    } catch {
      logout();
      return null;
    }
  };

  /** @deprecated Dùng getProfile() thay thế */
  const getMe = getProfile;

  return { user, isAuthenticated, setAuth, logout, getProfile, getMe };
}
