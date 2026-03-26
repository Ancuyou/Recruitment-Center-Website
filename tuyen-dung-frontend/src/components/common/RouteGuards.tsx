import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '@/store/auth.store';
import type { VaiTroTaiKhoan } from '@/types/auth.types';

interface Props {
  allowedRoles?: VaiTroTaiKhoan[];
}

/** Redirects unauthenticated users to /dang-nhap */
export function ProtectedRoute({ allowedRoles }: Props) {
  const { isAuthenticated, user } = useAuthStore();

  if (!isAuthenticated) return <Navigate to="/dang-nhap" replace />;
  if (allowedRoles && user && !allowedRoles.includes(user.vaiTro)) {
    return <Navigate to="/unauthorized" replace />;
  }
  return <Outlet />;
}

/** Redirects already-authenticated users away from auth pages */
export function GuestRoute() {
  const { isAuthenticated, user } = useAuthStore();

  if (!isAuthenticated) return <Outlet />;

  if (user?.vaiTro === 'UNG_VIEN') return <Navigate to="/ung-vien/dashboard" replace />;
  if (user?.vaiTro === 'NHA_TUYEN_DUNG') return <Navigate to="/nha-tuyen-dung/dashboard" replace />;
  return <Navigate to="/" replace />;
}
