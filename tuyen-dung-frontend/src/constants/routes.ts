import type { VaiTroTaiKhoan } from '@/types/auth.types';

export const ROUTES = {
  root: '/',
  public: {
    jobs: '/viec-lam',
    jobDetail: '/viec-lam/:id',
  },
  auth: {
    login: '/dang-nhap',
    register: '/dang-ky',
    forgotPassword: '/quen-mat-khau',
    resetPassword: '/dat-lai-mat-khau',
    verifyEmail: '/xac-thuc-email',
    unauthorized: '/unauthorized',
  },
  candidate: {
    dashboard: '/ung-vien/dashboard',
    jobs: '/ung-vien/tim-viec',
    applications: '/ung-vien/ung-tuyen',
    savedJobs: '/ung-vien/da-luu',
    profile: '/ung-vien/ho-so',
    cv: '/ung-vien/cv',
    notifications: '/ung-vien/thong-bao',
  },
  recruiter: {
    dashboard: '/nha-tuyen-dung/dashboard',
    jobs: '/nha-tuyen-dung/tin-tuyen-dung',
    applicants: '/nha-tuyen-dung/ung-vien',
    candidateProfiles: '/nha-tuyen-dung/ho-so-ung-vien',
    profile: '/nha-tuyen-dung/ho-so',
    company: '/nha-tuyen-dung/cong-ty',
    plans: '/nha-tuyen-dung/goi-dich-vu',
    notifications: '/nha-tuyen-dung/thong-bao',
  },
  admin: {
    dashboard: '/admin/dashboard',
    users: '/admin/nguoi-dung',
    jobs: '/admin/tin-tuyen-dung',
    companies: '/admin/cong-ty',
    reports: '/admin/bao-cao',
    settings: '/admin/cai-dat',
    logs: '/admin/nhat-ky',
  },
} as const;

export const HOME_BY_ROLE: Record<VaiTroTaiKhoan, string> = {
  UNG_VIEN: ROUTES.candidate.dashboard,
  NHA_TUYEN_DUNG: ROUTES.recruiter.dashboard,
  ADMIN: ROUTES.admin.dashboard,
};

export function getHomePathByRole(role?: VaiTroTaiKhoan | null): string {
  if (!role) return ROUTES.auth.login;
  return HOME_BY_ROLE[role] ?? ROUTES.auth.login;
}
