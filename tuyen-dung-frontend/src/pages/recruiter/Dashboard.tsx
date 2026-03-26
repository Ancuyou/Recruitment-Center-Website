import MainLayout from '@/layouts/MainLayout';
import StatCard from '@/components/common/StatCard';
import { useAuthStore } from '@/store/auth.store';
import s from '@/assets/styles/dashboard.module.css';

// ─── Mock data ────────────────────────────────────────────────────
const STATS = [
  { icon: '📢', label: 'Tin đang tuyển', value: 8, accent: 'indigo', trend: 'neutral' },
  { icon: '📥', label: 'Hồ sơ mới nhận', value: 34, accent: 'sky', trend: 'up', trendValue: '+12 hôm nay' },
  { icon: '👁️', label: 'Lượt xem tin', value: 1240, accent: 'amber', trend: 'up', trendValue: '+80 tuần này' },
  { icon: '✅', label: 'Đã tuyển thành công', value: 5, accent: 'emerald', trend: 'up', trendValue: 'Tháng này' },
] as const;

const NEW_CANDIDATES = [
  { id: 1, name: 'Nguyễn Văn A', role: 'Frontend Developer', exp: '3 năm', time: '2 giờ trước', match: 92 },
  { id: 2, name: 'Trần Thị B', role: 'UI/UX Designer', exp: '2 năm', time: '5 giờ trước', match: 85 },
  { id: 3, name: 'Lê Văn C', role: 'Backend Developer', exp: '4 năm', time: '1 ngày trước', match: 78 },
  { id: 4, name: 'Phạm Thị D', role: 'Product Manager', exp: '5 năm', time: '2 ngày trước', match: 71 },
];

const ACTIVE_JOBS = [
  { id: 1, title: 'Senior Frontend Developer', applications: 12, views: 340, deadline: '15/04/2026' },
  { id: 2, title: 'Backend Engineer (Java)', applications: 8, views: 210, deadline: '20/04/2026' },
  { id: 3, title: 'UI/UX Designer', applications: 14, views: 480, deadline: '10/04/2026' },
];

const QUICK_ACTIONS = [
  { icon: '📢', label: 'Đăng tin tuyển dụng' },
  { icon: '🔍', label: 'Tìm ứng viên' },
  { icon: '🏢', label: 'Cập nhật công ty' },
  { icon: '📊', label: 'Xem báo cáo' },
];

// ─── Component ────────────────────────────────────────────────────
export default function RecruiterDashboard() {
  const user = useAuthStore((st) => st.user);

  return (
    <MainLayout title="Dashboard" breadcrumb="Trang chủ / Nhà tuyển dụng">
      <div className={s.page}>

        {/* Banner */}
        <div className={s.banner} style={{ background: 'linear-gradient(135deg, #0369a1 0%, #0ea5e9 100%)' }}>
          <div className={s.bannerBg} />
          <div className={s.bannerBg2} />
          <div className={s.bannerText}>
            <h2>Chào {user?.hoTen?.split(' ').pop() ?? 'bạn'} 🏢</h2>
            <p>
              {user?.tenCongTy
                ? `Quản lý tuyển dụng tại ${user.tenCongTy}`
                : 'Tìm kiếm nhân tài phù hợp cho doanh nghiệp của bạn'}
            </p>
          </div>
          <div className={s.bannerEmoji}>🏆</div>
        </div>

        {/* Stats */}
        <div className={s.statsGrid}>
          {STATS.map((st) => (
            <StatCard
              key={st.label}
              icon={st.icon}
              label={st.label}
              value={st.value}
              accent={st.accent as never}
              trend={st.trend as never}
              trendValue={(st as { trendValue?: string }).trendValue}
            />
          ))}
        </div>

        {/* Row */}
        <div className={s.rowTwo}>

          {/* New candidates */}
          <div className={s.card}>
            <div className={s.sectionHead}>
              <span className={s.cardTitle}>Ứng viên mới nộp hồ sơ</span>
              <button className={s.seeAll}>Xem tất cả →</button>
            </div>
            <div className={s.activityList}>
              {NEW_CANDIDATES.map((c) => (
                <div key={c.id} className={s.activityItem}>
                  <div className={s.activityIcon}>👤</div>
                  <div className={s.activityContent}>
                    <div className={s.activityTitle}>{c.name} — {c.role}</div>
                    <div className={s.activitySub}>{c.exp} kinh nghiệm · {c.time}</div>
                  </div>
                  <span
                    className={s.jobStatus}
                    style={{
                      background: c.match >= 85 ? '#dcfce7' : c.match >= 70 ? '#fef3c7' : '#fee2e2',
                      color: c.match >= 85 ? '#166534' : c.match >= 70 ? '#92400e' : '#991b1b',
                    }}
                  >
                    {c.match}% phù hợp
                  </span>
                </div>
              ))}
            </div>
          </div>

          {/* Right column */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-6)' }}>
            {/* Quick actions */}
            <div className={s.card}>
              <span className={s.cardTitle}>Thao tác nhanh</span>
              <div className={s.quickActions} style={{ marginTop: 'var(--space-4)' }}>
                {QUICK_ACTIONS.map((a) => (
                  <button key={a.label} className={s.quickBtn}>
                    <span>{a.icon}</span>
                    <span>{a.label}</span>
                  </button>
                ))}
              </div>
            </div>

            {/* Active job posts */}
            <div className={s.card}>
              <div className={s.sectionHead}>
                <span className={s.cardTitle}>Tin đang hoạt động</span>
                <button className={s.seeAll}>Quản lý →</button>
              </div>
              <table className={s.simpleTable}>
                <thead>
                  <tr>
                    <th>Vị trí</th>
                    <th>Hồ sơ</th>
                    <th>Hạn nộp</th>
                  </tr>
                </thead>
                <tbody>
                  {ACTIVE_JOBS.map((job) => (
                    <tr key={job.id}>
                      <td>{job.title}</td>
                      <td>
                        <span className={`${s.jobStatus} ${s.statusReview}`}>{job.applications}</span>
                      </td>
                      <td style={{ fontSize: 12, color: 'var(--color-text-muted)' }}>{job.deadline}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>

      </div>
    </MainLayout>
  );
}
