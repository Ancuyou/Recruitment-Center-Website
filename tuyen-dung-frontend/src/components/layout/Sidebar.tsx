import { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/auth.store';
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
      items: [{ label: 'Tổng quan', icon: '🏠', to: '/ung-vien/dashboard' }],
    },
    {
      title: 'Việc làm',
      items: [
        { label: 'Tìm việc làm', icon: '🔍', to: '/ung-vien/tim-viec' },
        { label: 'Đã ứng tuyển', icon: '📋', to: '/ung-vien/ung-tuyen' },
        { label: 'Việc đã lưu', icon: '❤️', to: '/ung-vien/da-luu' },
      ],
    },
    {
      title: 'Hồ sơ',
      items: [
        { label: 'Hồ sơ của tôi', icon: '👤', to: '/ung-vien/ho-so' },
        { label: 'Quản lý CV', icon: '📄', to: '/ung-vien/cv' },
      ],
    },
    {
      title: 'Thông báo',
      items: [{ label: 'Thông báo', icon: '🔔', to: '/ung-vien/thong-bao', badge: 3 }],
    },
  ],

  NHA_TUYEN_DUNG: [
    {
      items: [{ label: 'Tổng quan', icon: '🏠', to: '/nha-tuyen-dung/dashboard' }],
    },
    {
      title: 'Tuyển dụng',
      items: [
        { label: 'Tin tuyển dụng', icon: '📢', to: '/nha-tuyen-dung/tin-tuyen-dung' },
        { label: 'Ứng viên nộp', icon: '📥', to: '/nha-tuyen-dung/ung-vien', badge: 5 },
        { label: 'Hồ sơ ứng viên', icon: '🗂️', to: '/nha-tuyen-dung/ho-so' },
      ],
    },
    {
      title: 'Công ty',
      items: [
        { label: 'Thông tin công ty', icon: '🏢', to: '/nha-tuyen-dung/cong-ty' },
        { label: 'Gói dịch vụ', icon: '⭐', to: '/nha-tuyen-dung/goi-dich-vu' },
      ],
    },
    {
      title: 'Khác',
      items: [{ label: 'Thông báo', icon: '🔔', to: '/nha-tuyen-dung/thong-bao' }],
    },
  ],

  QUAN_TRI_VIEN: [
    {
      items: [{ label: 'Tổng quan', icon: '🏠', to: '/admin/dashboard' }],
    },
    {
      title: 'Quản lý',
      items: [
        { label: 'Người dùng', icon: '👥', to: '/admin/nguoi-dung' },
        { label: 'Tin tuyển dụng', icon: '📢', to: '/admin/tin-tuyen-dung' },
        { label: 'Công ty', icon: '🏢', to: '/admin/cong-ty' },
      ],
    },
    {
      title: 'Hệ thống',
      items: [
        { label: 'Báo cáo', icon: '📊', to: '/admin/bao-cao' },
        { label: 'Cài đặt', icon: '⚙️', to: '/admin/cai-dat' },
        { label: 'Nhật ký', icon: '📝', to: '/admin/nhat-ky' },
      ],
    },
  ],
};

const ROLE_LABEL: Record<VaiTroTaiKhoan, string> = {
  UNG_VIEN: 'Ứng viên',
  NHA_TUYEN_DUNG: 'Nhà tuyển dụng',
  QUAN_TRI_VIEN: 'Quản trị viên',
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
    navigate('/dang-nhap', { replace: true });
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
