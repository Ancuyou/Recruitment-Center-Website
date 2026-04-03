import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '@/store/auth.store';
import { ROUTES, getHomePathByRole } from '@/constants/routes';
import type { VaiTroTaiKhoan } from '@/types/auth.types';

interface Props {
  allowedRoles?: VaiTroTaiKhoan[];
}

/** Redirects unauthenticated users to /dang-nhap */
export function ProtectedRoute({ allowedRoles }: Props) {
  const { isAuthenticated, user } = useAuthStore();

  if (!isAuthenticated || !user) return <Navigate to={ROUTES.auth.login} replace />;
  if (allowedRoles && !allowedRoles.includes(user.vaiTro)) {
    return <Navigate to={ROUTES.auth.unauthorized} replace />;
  }
  return <Outlet />;
}

/** Redirects already-authenticated users away from auth pages */
export function GuestRoute() {
  const { isAuthenticated, user } = useAuthStore();

  if (!isAuthenticated || !user) return <Outlet />;
  return <Navigate to={getHomePathByRole(user.vaiTro)} replace />;
}
