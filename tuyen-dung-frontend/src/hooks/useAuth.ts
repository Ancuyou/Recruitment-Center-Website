import { useAuthStore } from '@/store/auth.store';
import { authService } from '@/services/auth.service';

/**
 * Hook to access auth state and actions.
 * Wraps Zustand store + service calls.
 */
export function useAuth() {
  const { user, isAuthenticated, setAuth, logout } = useAuthStore();

  const getMe = async () => {
    try {
      return await authService.getMe();
    } catch {
      logout();
      return null;
    }
  };

  return { user, isAuthenticated, setAuth, logout, getMe };
}
