import MainLayout from '@/layouts/MainLayout';
import StatCard from '@/components/common/StatCard';
import s from '@/assets/styles/dashboard.module.css';

// ─── Mock data ────────────────────────────────────────────────────
const STATS = [
  { icon: '👥', label: 'Tổng người dùng', value: '1,248', accent: 'indigo', trend: 'up', trendValue: '+24 hôm nay' },
  { icon: '🏢', label: 'Công ty đăng ký', value: 87, accent: 'sky', trend: 'up', trendValue: '+3 tháng này' },
  { icon: '📢', label: 'Tổng tin tuyển dụng', value: 342, accent: 'amber', trend: 'up', trendValue: '+18 tuần này' },
  { icon: '🎯', label: 'Kết nối thành công', value: 156, accent: 'emerald', trend: 'up', trendValue: 'Tháng này' },
] as const;

const RECENT_USERS = [
  { id: 1, name: 'Nguyễn Văn A', email: 'a@example.com', role: 'Ứng viên', joinedAt: '26/03/2026' },
  { id: 2, name: 'Công ty XYZ', email: 'hr@xyz.vn', role: 'Nhà tuyển dụng', joinedAt: '25/03/2026' },
  { id: 3, name: 'Trần Thị B', email: 'b@example.com', role: 'Ứng viên', joinedAt: '25/03/2026' },
  { id: 4, name: 'Tech Corp VN', email: 'hr@techcorp.vn', role: 'Nhà tuyển dụng', joinedAt: '24/03/2026' },
  { id: 5, name: 'Lê Văn C', email: 'c@example.com', role: 'Ứng viên', joinedAt: '24/03/2026' },
];

const RECENT_JOBS = [
  { id: 1, title: 'Senior Developer', company: 'FPT Software', applications: 23, status: 'Đang duyệt' },
  { id: 2, title: 'Data Analyst', company: 'VNG Corp', applications: 15, status: 'Đã duyệt' },
  { id: 3, title: 'Marketing Lead', company: 'Shopee VN', applications: 31, status: 'Đã duyệt' },
];

const SYSTEM_ACTIVITIES = [
  { icon: '🆕', title: 'Người dùng mới đăng ký', sub: 'Nguyễn Văn A — 10 phút trước' },
  { icon: '📢', title: 'Tin tuyển dụng mới cần duyệt', sub: 'FPT Software — 30 phút trước' },
  { icon: '🏢', title: 'Công ty mới đăng ký', sub: 'Tech Corp VN — 2 giờ trước' },
  { icon: '⚠️', title: 'Báo cáo vi phạm', sub: 'Người dùng ID #342 — 3 giờ trước' },
];

const QUICK_ACTIONS = [
  { icon: '✅', label: 'Duyệt tin mới' },
  { icon: '👥', label: 'Quản lý người dùng' },
  { icon: '📊', label: 'Báo cáo hệ thống' },
  { icon: '⚙️', label: 'Cài đặt hệ thống' },
];

// ─── Component ────────────────────────────────────────────────────
export default function AdminDashboard() {
  return (
    <MainLayout title="Admin Dashboard" breadcrumb="Trang chủ / Quản trị">
      <div className={s.page}>

        {/* Banner */}
        <div className={s.banner} style={{ background: 'linear-gradient(135deg, #1e1b4b 0%, #7c3aed 100%)' }}>
          <div className={s.bannerBg} />
          <div className={s.bannerBg2} />
          <div className={s.bannerText}>
            <h2>Bảng điều khiển quản trị 🛡️</h2>
            <p>Giám sát và quản lý toàn bộ hoạt động của hệ thống TuyenDungPro</p>
          </div>
          <div className={s.bannerEmoji}>⚙️</div>
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

          {/* Recent users */}
          <div className={s.card}>
            <div className={s.sectionHead}>
              <span className={s.cardTitle}>Người dùng mới đăng ký</span>
              <button className={s.seeAll}>Quản lý →</button>
            </div>
            <table className={s.simpleTable}>
              <thead>
                <tr>
                  <th>Tên</th>
                  <th>Vai trò</th>
                  <th>Ngày tham gia</th>
                </tr>
              </thead>
              <tbody>
                {RECENT_USERS.map((u) => (
                  <tr key={u.id}>
                    <td>
                      <div style={{ fontWeight: 600 }}>{u.name}</div>
                      <div style={{ fontSize: 12, color: 'var(--color-text-muted)' }}>{u.email}</div>
                    </td>
                    <td>
                      <span
                        className={s.jobStatus}
                        style={
                          u.role === 'Ứng viên'
                            ? { background: '#e0f2fe', color: '#0e7490' }
                            : { background: '#fef3c7', color: '#92400e' }
                        }
                      >
                        {u.role}
                      </span>
                    </td>
                    <td style={{ fontSize: 12, color: 'var(--color-text-muted)' }}>{u.joinedAt}</td>
                  </tr>
                ))}
              </tbody>
            </table>
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

            {/* System activity feed */}
            <div className={s.card}>
              <div className={s.sectionHead}>
                <span className={s.cardTitle}>Hoạt động hệ thống</span>
                <button className={s.seeAll}>Nhật ký →</button>
              </div>
              <div className={s.activityList}>
                {SYSTEM_ACTIVITIES.map((a, i) => (
                  <div key={i} className={s.activityItem}>
                    <div className={s.activityIcon}>{a.icon}</div>
                    <div className={s.activityContent}>
                      <div className={s.activityTitle}>{a.title}</div>
                      <div className={s.activitySub}>{a.sub}</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Jobs awaiting approval */}
            <div className={s.card}>
              <div className={s.sectionHead}>
                <span className={s.cardTitle}>Tin cần phê duyệt</span>
                <button className={s.seeAll}>Xem tất cả →</button>
              </div>
              <table className={s.simpleTable}>
                <thead>
                  <tr>
                    <th>Vị trí</th>
                    <th>Công ty</th>
                    <th>Hồ sơ</th>
                  </tr>
                </thead>
                <tbody>
                  {RECENT_JOBS.map((job) => (
                    <tr key={job.id}>
                      <td style={{ fontWeight: 500 }}>{job.title}</td>
                      <td style={{ color: 'var(--color-text-muted)', fontSize: 13 }}>{job.company}</td>
                      <td>
                        <span className={`${s.jobStatus} ${job.status === 'Đã duyệt' ? s.statusAccepted : s.statusPending}`}>
                          {job.status}
                        </span>
                      </td>
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
