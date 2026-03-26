import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/auth.store';
import type { VaiTroTaiKhoan } from '@/types/auth.types';
import s from './Header.module.css';

interface Props {
  title: string;
  breadcrumb?: string;
}

const ROLE_LABEL: Record<VaiTroTaiKhoan, string> = {
  UNG_VIEN: 'Ứng viên',
  NHA_TUYEN_DUNG: 'Nhà tuyển dụng',
  QUAN_TRI_VIEN: 'Quản trị viên',
};

export default function Header({ title, breadcrumb }: Props) {
  const user = useAuthStore((st) => st.user);
  const logout = useAuthStore((st) => st.logout);
  const navigate = useNavigate();

  const initials = user?.hoTen
    ? user.hoTen.split(' ').map((w) => w[0]).slice(-2).join('').toUpperCase()
    : '?';

  const handleLogout = () => {
    logout();
    navigate('/dang-nhap', { replace: true });
  };

  return (
    <header className={s.header}>
      {/* Left */}
      <div className={s.left}>
        <div>
          <div className={s.pageTitle}>{title}</div>
          {breadcrumb && <div className={s.breadcrumb}>{breadcrumb}</div>}
        </div>
      </div>

      {/* Right */}
      <div className={s.right}>
        {/* Notification bell */}
        <button className={s.iconBtn} title="Thông báo">
          🔔
          <span className={s.badge} />
        </button>

        {/* User chip */}
        <div className={s.userMenu}>
          <div className={s.avatar}>{initials}</div>
          <div className={s.userInfo}>
            <span className={s.userName}>{user?.hoTen ?? 'Người dùng'}</span>
            <span className={s.userRole}>
              {user?.vaiTro ? ROLE_LABEL[user.vaiTro] : ''}
            </span>
          </div>
        </div>

        {/* Logout */}
        <button className={s.logoutBtn} onClick={handleLogout}>
          🚪 Đăng xuất
        </button>
      </div>
    </header>
  );
}
