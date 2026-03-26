import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ProtectedRoute, GuestRoute } from '@/components/common/RouteGuards';

// ── Auth pages ────────────────────────────────────────────────────
import Login from '@/pages/auth/Login';
import Register from '@/pages/auth/Register';

// ── Dashboard pages ───────────────────────────────────────────────
import CandidateDashboard from '@/pages/candidate/Dashboard';
import RecruiterDashboard from '@/pages/recruiter/Dashboard';
import AdminDashboard from '@/pages/admin/Dashboard';

// ── Misc pages ────────────────────────────────────────────────────
const Unauthorized = () => (
  <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', fontFamily: 'Inter, sans-serif', gap: 12 }}>
    <div style={{ fontSize: 64 }}>🚫</div>
    <h2 style={{ fontSize: 24, fontWeight: 700 }}>Không có quyền truy cập</h2>
    <p style={{ color: '#64748b' }}>Bạn không có quyền truy cập trang này.</p>
    <a href="/dang-nhap" style={{ marginTop: 8, color: '#6366f1', fontWeight: 600 }}>← Quay lại đăng nhập</a>
  </div>
);

const NotFound = () => (
  <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', fontFamily: 'Inter, sans-serif', gap: 12 }}>
    <div style={{ fontSize: 64 }}>🔍</div>
    <h2 style={{ fontSize: 24, fontWeight: 700 }}>404 — Trang không tồn tại</h2>
    <a href="/" style={{ marginTop: 8, color: '#6366f1', fontWeight: 600 }}>← Về trang chủ</a>
  </div>
);

// ─── App ─────────────────────────────────────────────────────────
export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Root: redirect to login */}
        <Route path="/" element={<Navigate to="/dang-nhap" replace />} />

        {/* ── Guest-only (redirect out if authenticated) ── */}
        <Route element={<GuestRoute />}>
          <Route path="/dang-nhap" element={<Login />} />
          <Route path="/dang-ky" element={<Register />} />
        </Route>

        {/* ── Candidate routes ── */}
        <Route element={<ProtectedRoute allowedRoles={['UNG_VIEN']} />}>
          <Route path="/ung-vien/dashboard" element={<CandidateDashboard />} />
          {/* Thêm các route ứng viên tại đây khi backend sẵn sàng */}
          {/* <Route path="/ung-vien/tim-viec" element={<JobSearch />} /> */}
          {/* <Route path="/ung-vien/ho-so" element={<Profile />} /> */}
        </Route>

        {/* ── Recruiter routes ── */}
        <Route element={<ProtectedRoute allowedRoles={['NHA_TUYEN_DUNG']} />}>
          <Route path="/nha-tuyen-dung/dashboard" element={<RecruiterDashboard />} />
          {/* Thêm các route nhà tuyển dụng tại đây khi backend sẵn sàng */}
          {/* <Route path="/nha-tuyen-dung/tin-tuyen-dung" element={<JobManagement />} /> */}
        </Route>

        {/* ── Admin routes ── */}
        <Route element={<ProtectedRoute allowedRoles={['QUAN_TRI_VIEN']} />}>
          <Route path="/admin/dashboard" element={<AdminDashboard />} />
          {/* Thêm các route admin tại đây khi backend sẵn sàng */}
          {/* <Route path="/admin/nguoi-dung" element={<UserManagement />} /> */}
        </Route>

        {/* ── Misc ── */}
        <Route path="/unauthorized" element={<Unauthorized />} />
        <Route path="*" element={<NotFound />} />
      </Routes>
    </BrowserRouter>
  );
}
