import { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/auth.store';
import { ROUTES } from '@/constants/routes';
import type { VaiTroTaiKhoan } from '@/types/auth.types';
import s from './Sidebar.module.css';

// ─── Nav item definition (Open/Closed Principle: add items, never modify) ───
interface NavItem {
  label: string;
  icon: string;
  to: string;
  badge?: number;
}

interface NavSection {
  title?: string;
  items: NavItem[];
}

// ─── Role-specific nav configs ─────────────────────────────────────────────
const NAV_CONFIG: Record<VaiTroTaiKhoan, NavSection[]> = {
  UNG_VIEN: [
    {
      items: [{ label: 'Tổng quan', icon: '🏠', to: ROUTES.candidate.dashboard }],
    },
    {
      title: 'Việc làm',
      items: [
        { label: 'Tìm việc làm', icon: '🔍', to: ROUTES.candidate.jobs },
        { label: 'Đã ứng tuyển', icon: '📋', to: ROUTES.candidate.applications },
        { label: 'Việc đã lưu', icon: '❤️', to: ROUTES.candidate.savedJobs },
      ],
    },
    {
      title: 'Hồ sơ',
      items: [
        { label: 'Hồ sơ của tôi', icon: '👤', to: ROUTES.candidate.profile },
        { label: 'Quản lý CV', icon: '📄', to: ROUTES.candidate.cv },
      ],
    },
    {
      title: 'Thông báo',
      items: [{ label: 'Thông báo', icon: '🔔', to: ROUTES.candidate.notifications, badge: 3 }],
    },
  ],

  NHA_TUYEN_DUNG: [
    {
      items: [{ label: 'Tổng quan', icon: '🏠', to: ROUTES.recruiter.dashboard }],
    },
    {
      title: 'Tuyển dụng',
      items: [
        { label: 'Tin tuyển dụng', icon: '📢', to: ROUTES.recruiter.jobs },
        { label: 'Ứng viên nộp', icon: '📥', to: ROUTES.recruiter.applicants, badge: 5 },
      ],
    },
    {
      title: 'Công ty',
      items: [
        { label: 'Thông tin công ty', icon: '🏢', to: ROUTES.recruiter.company },
        { label: 'Gói dịch vụ', icon: '⭐', to: ROUTES.recruiter.plans },
      ],
    },
    {
      title: 'Khác',
      items: [
        { label: 'Hồ sơ của tôi', icon: '👤', to: ROUTES.recruiter.profile },
        { label: 'Hồ sơ ứng viên', icon: '🗂️', to: ROUTES.recruiter.candidateProfiles },
        { label: 'Thông báo', icon: '🔔', to: ROUTES.recruiter.notifications },
      ],
    },
  ],

  ADMIN: [
    {
      items: [{ label: 'Tổng quan', icon: '🏠', to: ROUTES.admin.dashboard }],
    },
    {
      title: 'Quản lý',
      items: [
        { label: 'Người dùng', icon: '👥', to: ROUTES.admin.users },
        { label: 'Tin tuyển dụng', icon: '📢', to: ROUTES.admin.jobs },
        { label: 'Công ty', icon: '🏢', to: ROUTES.admin.companies },
      ],
    },
    {
      title: 'Hệ thống',
      items: [
        { label: 'Báo cáo', icon: '📊', to: ROUTES.admin.reports },
        { label: 'Cài đặt', icon: '⚙️', to: ROUTES.admin.settings },
        { label: 'Nhật ký', icon: '📝', to: ROUTES.admin.logs },
      ],
    },
  ],
};

const ROLE_LABEL: Record<VaiTroTaiKhoan, string> = {
  UNG_VIEN: 'Ứng viên',
  NHA_TUYEN_DUNG: 'Nhà tuyển dụng',
  ADMIN: 'Quản trị viên',
};

// ─── Component ─────────────────────────────────────────────────────────────
export default function Sidebar() {
  const [collapsed, setCollapsed] = useState(false);
  const user = useAuthStore((st) => st.user);
  const logout = useAuthStore((st) => st.logout);
  const navigate = useNavigate();

  const vaiTro = user?.vaiTro ?? 'UNG_VIEN';
  const sections = NAV_CONFIG[vaiTro];
  const initials = user?.hoTen
    ? user.hoTen.split(' ').map((w) => w[0]).slice(-2).join('').toUpperCase()
    : '?';

  const handleLogout = () => {
    logout();
    navigate(ROUTES.auth.login, { replace: true });
  };

  return (
    <aside className={`${s.sidebar} ${collapsed ? s.collapsed : ''}`}>
      {/* Toggle */}
      <button className={s.toggleBtn} onClick={() => setCollapsed((v) => !v)} title="Thu gọn sidebar">
        ‹
      </button>

      {/* Logo */}
      <NavLink to="/" className={s.logo}>
        <div className={s.logoIcon}>🎯</div>
        <span className={s.logoText}>
          TuyenDung<span className={s.logoAccent}>Pro</span>
        </span>
      </NavLink>

      {/* Role badge */}
      <div className={s.roleBadge}>{ROLE_LABEL[vaiTro]}</div>

      {/* Navigation */}
      <nav className={s.nav}>
        {sections.map((section, si) => (
          <div key={si}>
            {section.title && <div className={s.navSection}>{section.title}</div>}
            {section.items.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) => `${s.navItem} ${isActive ? s.active : ''}`}
              >
                <span className={s.navIcon}>{item.icon}</span>
                <span className={s.navLabel}>{item.label}</span>
                {item.badge !== undefined && (
                  <span className={s.navBadge}>{item.badge}</span>
                )}
              </NavLink>
            ))}
          </div>
        ))}
      </nav>

      {/* User card + logout */}
      <div className={s.bottom}>
        <div className={s.userCard}>
          <div className={s.avatar}>{initials}</div>
          <div className={s.userInfo}>
            <div className={s.userName}>{user?.hoTen ?? 'Người dùng'}</div>
            <div className={s.userRole}>{ROLE_LABEL[vaiTro]}</div>
          </div>
        </div>
        <button
          className={s.navItem}
          onClick={handleLogout}
          style={{ marginTop: 8, color: 'rgba(255,100,100,0.8)' }}
        >
          <span className={s.navIcon}>🚪</span>
          <span className={s.navLabel}>Đăng xuất</span>
        </button>
      </div>
    </aside>
  );
}
