import { NavLink } from 'react-router-dom';
import { ROUTES } from '@/constants/routes';
import s from './AdminRoleNav.module.css';

const items = [
  { label: 'Tổng quan', to: ROUTES.admin.dashboard },
  { label: 'Người dùng', to: ROUTES.admin.users },
  { label: 'Tin tuyển dụng', to: ROUTES.admin.jobs },
  { label: 'Công ty', to: ROUTES.admin.companies },
  { label: 'Kỹ năng', to: ROUTES.admin.skills },
];

export default function AdminRoleNav() {
  return (
    <nav className={s.nav} aria-label="Điều hướng quản trị">
      {items.map((item) => (
        <NavLink
          key={item.to}
          to={item.to}
          className={({ isActive }) => `${s.link} ${isActive ? s.active : ''}`}
        >
          {item.label}
        </NavLink>
      ))}
    </nav>
  );
}