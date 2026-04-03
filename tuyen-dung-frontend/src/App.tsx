import { BrowserRouter, Routes, Route, Navigate, Link } from 'react-router-dom';
import { ProtectedRoute, GuestRoute } from '@/components/common/RouteGuards';
import { ROUTES } from '@/constants/routes';

// ── Auth pages ────────────────────────────────────────────────────
import Login from '@/pages/auth/Login';
import Register from '@/pages/auth/Register';
import ForgotPassword from '@/pages/auth/ForgotPassword';
import ResetPassword from '@/pages/auth/ResetPassword';
import VerifyEmail from '@/pages/auth/VerifyEmail';

// ── Public pages ──────────────────────────────────────────────────
import JobsPage from '@/pages/public/Jobs';
import JobDetailPage from '@/pages/public/JobDetail';

// ── Dashboard pages ───────────────────────────────────────────────
import CandidateDashboard from '@/pages/candidate/Dashboard';
import CandidateApplicationsPage from '@/pages/candidate/Applications';
import CandidateSavedJobsPage from '@/pages/candidate/SavedJobs';
import CandidateCvManagementPage from '@/pages/candidate/CvManagement';
import RecruiterDashboard from '@/pages/recruiter/Dashboard';
import RecruiterJobsManagementPage from '@/pages/recruiter/JobsManagement';
import RecruiterApplicantsRoutePage from '@/pages/recruiter/Applicants';
import RecruiterCandidateProfilesPage from '@/pages/recruiter/CandidateProfiles';
import RecruiterCompanyProfilePage from '@/pages/recruiter/CompanyProfile';
import RecruiterPlansPage from '@/pages/recruiter/Plans';
import AdminDashboard from '@/pages/admin/Dashboard';
import PlaceholderPage from '@/pages/common/PlaceholderPage';

// ── Shared pages ──────────────────────────────────────────────────
import ProfilePage from '@/pages/shared/Profile';
import NotificationsPage from '@/pages/shared/Notifications';

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

// ─── App ─────────────────────────────────────────────────────────
export default function App() {
  return (
    <BrowserRouter>
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
            element={<JobsPage />}
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
          <Route
            path={ROUTES.admin.users}
            element={<PlaceholderPage title="Người dùng" breadcrumb="Trang chủ / Admin / Người dùng" />}
          />
          <Route
            path={ROUTES.admin.jobs}
            element={<PlaceholderPage title="Tin tuyển dụng" breadcrumb="Trang chủ / Admin / Tin tuyển dụng" />}
          />
          <Route
            path={ROUTES.admin.companies}
            element={<PlaceholderPage title="Công ty" breadcrumb="Trang chủ / Admin / Công ty" />}
          />
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
    </BrowserRouter>
  );
}
