import { Suspense, lazy } from 'react';
import { BrowserRouter, Routes, Route, Navigate, Link } from 'react-router-dom';
import { ProtectedRoute, GuestRoute } from '@/components/common/RouteGuards';
import { ROUTES } from '@/constants/routes';

// ── Auth pages ────────────────────────────────────────────────────
const Login = lazy(() => import('@/pages/auth/Login'));
const Register = lazy(() => import('@/pages/auth/Register'));
const ForgotPassword = lazy(() => import('@/pages/auth/ForgotPassword'));
const ResetPassword = lazy(() => import('@/pages/auth/ResetPassword'));
const VerifyEmail = lazy(() => import('@/pages/auth/VerifyEmail'));

// ── Public pages ──────────────────────────────────────────────────
const JobsPage = lazy(() => import('@/pages/public/Jobs'));
const JobDetailPage = lazy(() => import('@/pages/public/JobDetail'));

// ── Dashboard pages ───────────────────────────────────────────────
const CandidateDashboard = lazy(() => import('@/pages/candidate/Dashboard'));
const CandidateJobsPage = lazy(() => import('@/pages/candidate/Jobs'));
const CandidateJobDetailPage = lazy(() => import('@/pages/candidate/JobDetail'));
const CandidateApplicationsPage = lazy(() => import('@/pages/candidate/Applications'));
const CandidateSavedJobsPage = lazy(() => import('@/pages/candidate/SavedJobs'));
const CandidateCvManagementPage = lazy(() => import('@/pages/candidate/CvManagement'));
const RecruiterDashboard = lazy(() => import('@/pages/recruiter/Dashboard'));
const RecruiterJobsManagementPage = lazy(() => import('@/pages/recruiter/JobsManagement'));
const RecruiterApplicantsRoutePage = lazy(() => import('@/pages/recruiter/Applicants'));
const RecruiterCandidateProfilesPage = lazy(() => import('@/pages/recruiter/CandidateProfiles'));
const RecruiterCompanyProfilePage = lazy(() => import('@/pages/recruiter/CompanyProfile'));
const RecruiterPlansPage = lazy(() => import('@/pages/recruiter/Plans'));
const AdminDashboard = lazy(() => import('@/pages/admin/Dashboard'));
const AdminUsersPage = lazy(() => import('@/pages/admin/Users'));
const AdminJobsPage = lazy(() => import('@/pages/admin/Jobs'));
const AdminCompaniesPage = lazy(() => import('@/pages/admin/Companies'));
const AdminSkillsPage = lazy(() => import('@/pages/admin/Skills'));
const PlaceholderPage = lazy(() => import('@/pages/common/PlaceholderPage'));

// ── Shared pages ──────────────────────────────────────────────────
const ProfilePage = lazy(() => import('@/pages/shared/Profile'));
const NotificationsPage = lazy(() => import('@/pages/shared/Notifications'));

// ── Misc pages ────────────────────────────────────────────────────
const Unauthorized = () => (
  <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', fontFamily: 'Inter, sans-serif', gap: 12 }}>
    <div style={{ fontSize: 64 }}>🚫</div>
    <h2 style={{ fontSize: 24, fontWeight: 700 }}>Không có quyền truy cập</h2>
    <p style={{ color: '#64748b' }}>Bạn không có quyền truy cập trang này.</p>
    <Link to={ROUTES.auth.login} style={{ marginTop: 8, color: '#6366f1', fontWeight: 600 }}>← Quay lại đăng nhập</Link>
  </div>
);

const NotFound = () => (
  <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', fontFamily: 'Inter, sans-serif', gap: 12 }}>
    <div style={{ fontSize: 64 }}>🔍</div>
    <h2 style={{ fontSize: 24, fontWeight: 700 }}>404 — Trang không tồn tại</h2>
    <Link to={ROUTES.root} style={{ marginTop: 8, color: '#6366f1', fontWeight: 600 }}>← Về trang chủ</Link>
  </div>
);

const RouteFallback = () => (
  <div
    style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      fontFamily: 'Inter, sans-serif',
      color: '#334155',
      fontWeight: 600,
    }}
  >
    Đang tải trang...
  </div>
);

// ─── App ─────────────────────────────────────────────────────────
export default function App() {
  return (
    <BrowserRouter>
      <Suspense fallback={<RouteFallback />}>
        <Routes>
          {/* Root: redirect to login */}
          <Route path={ROUTES.root} element={<Navigate to={ROUTES.auth.login} replace />} />

          {/* ── Guest-only (redirect out if authenticated) ── */}
          <Route element={<GuestRoute />}>
            <Route path={ROUTES.auth.login} element={<Login />} />
            <Route path={ROUTES.auth.register} element={<Register />} />
            <Route path={ROUTES.auth.forgotPassword} element={<ForgotPassword />} />
            <Route path={ROUTES.auth.resetPassword} element={<ResetPassword />} />
            <Route path={ROUTES.auth.verifyEmail} element={<VerifyEmail />} />
          </Route>

          {/* ── Public routes ── */}
          <Route path={ROUTES.public.jobs} element={<JobsPage />} />
          <Route path={ROUTES.public.jobDetail} element={<JobDetailPage />} />

          {/* ── Candidate routes ── */}
          <Route element={<ProtectedRoute allowedRoles={['UNG_VIEN']} />}>
            <Route path={ROUTES.candidate.dashboard} element={<CandidateDashboard />} />
            <Route
              path={ROUTES.candidate.jobs}
              element={<CandidateJobsPage />}
            />
            <Route
              path={ROUTES.candidate.jobDetail}
              element={<CandidateJobDetailPage />}
            />
            <Route
              path={ROUTES.candidate.applications}
              element={<CandidateApplicationsPage />}
            />
            <Route
              path={ROUTES.candidate.savedJobs}
              element={<CandidateSavedJobsPage />}
            />
            <Route
              path={ROUTES.candidate.profile}
              element={<ProfilePage />}
            />
            <Route
              path={ROUTES.candidate.cv}
              element={<CandidateCvManagementPage />}
            />
            <Route
              path={ROUTES.candidate.notifications}
              element={<NotificationsPage />}
            />
          </Route>

          {/* ── Recruiter routes ── */}
          <Route element={<ProtectedRoute allowedRoles={['NHA_TUYEN_DUNG']} />}>
            <Route path={ROUTES.recruiter.dashboard} element={<RecruiterDashboard />} />
            <Route
              path={ROUTES.recruiter.jobs}
              element={<RecruiterJobsManagementPage />}
            />
            <Route
              path={ROUTES.recruiter.applicants}
              element={<RecruiterApplicantsRoutePage />}
            />
            <Route
              path={ROUTES.recruiter.candidateProfiles}
              element={<RecruiterCandidateProfilesPage />}
            />
            <Route
              path={ROUTES.recruiter.profile}
              element={<ProfilePage />}
            />
            <Route
              path={ROUTES.recruiter.company}
              element={<RecruiterCompanyProfilePage />}
            />
            <Route
              path={ROUTES.recruiter.plans}
              element={<RecruiterPlansPage />}
            />
            <Route
              path={ROUTES.recruiter.notifications}
              element={<NotificationsPage />}
            />
          </Route>

          {/* ── Admin routes ── */}
          <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
            <Route path={ROUTES.admin.dashboard} element={<AdminDashboard />} />
            <Route path={ROUTES.admin.users} element={<AdminUsersPage />} />
            <Route path={ROUTES.admin.jobs} element={<AdminJobsPage />} />
            <Route path={ROUTES.admin.companies} element={<AdminCompaniesPage />} />
            <Route path={ROUTES.admin.skills} element={<AdminSkillsPage />} />
            <Route
              path={ROUTES.admin.reports}
              element={<PlaceholderPage title="Báo cáo" breadcrumb="Trang chủ / Admin / Báo cáo" />}
            />
            <Route
              path={ROUTES.admin.settings}
              element={<PlaceholderPage title="Cài đặt" breadcrumb="Trang chủ / Admin / Cài đặt" />}
            />
            <Route
              path={ROUTES.admin.logs}
              element={<PlaceholderPage title="Nhật ký" breadcrumb="Trang chủ / Admin / Nhật ký" />}
            />
          </Route>

          {/* ── Misc ── */}
          <Route path={ROUTES.auth.unauthorized} element={<Unauthorized />} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </Suspense>
    </BrowserRouter>
  );
}
