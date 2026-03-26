import MainLayout from '@/layouts/MainLayout';
import StatCard from '@/components/common/StatCard';
import { useAuthStore } from '@/store/auth.store';
import s from '@/assets/styles/dashboard.module.css';

// ─── Mock data (replace with real API calls when backend is ready) ───────────
const STATS = [
  { icon: '📋', label: 'Đã ứng tuyển', value: 12, accent: 'indigo', trend: 'up', trendValue: '+3 tuần này' },
  { icon: '👁️', label: 'Hồ sơ được xem', value: 48, accent: 'sky', trend: 'up', trendValue: '+12 tuần này' },
  { icon: '📞', label: 'Lời mời phỏng vấn', value: 3, accent: 'emerald', trend: 'up', trendValue: 'Mới nhất' },
  { icon: '❤️', label: 'Việc đã lưu', value: 7, accent: 'rose', trend: 'neutral', trendValue: '' },
] as const;

const RECENT_APPLICATIONS = [
  { id: 1, title: 'Frontend Developer', company: 'Công ty ABC', time: '2 ngày trước', status: 'statusReview' },
  { id: 2, title: 'React Engineer', company: 'Tech Corp', time: '5 ngày trước', status: 'statusPending' },
  { id: 3, title: 'UI/UX Designer', company: 'Creative Studio', time: '1 tuần trước', status: 'statusAccepted' },
  { id: 4, title: 'Fullstack Developer', company: 'StartupX', time: '2 tuần trước', status: 'statusRejected' },
];

const STATUS_LABEL: Record<string, string> = {
  statusPending: 'Chờ xét duyệt',
  statusReview: 'Đang xem xét',
  statusAccepted: 'Được nhận',
  statusRejected: 'Từ chối',
};

const RECENT_JOBS = [
  { id: 1, icon: '💻', title: 'Senior React Developer', company: 'FPT Software', salary: '25-35 triệu', location: 'Hà Nội' },
  { id: 2, icon: '🚀', title: 'Product Manager', company: 'VNG Corp', salary: '30-50 triệu', location: 'TP.HCM' },
  { id: 3, icon: '🎨', title: 'UI/UX Designer', company: 'Grab Vietnam', salary: '20-30 triệu', location: 'TP.HCM' },
];

const QUICK_ACTIONS = [
  { icon: '🔍', label: 'Tìm việc ngay' },
  { icon: '📄', label: 'Cập nhật CV' },
  { icon: '👤', label: 'Hoàn thiện hồ sơ' },
  { icon: '🔔', label: 'Cài đặt thông báo' },
];

// ─── Component ───────────────────────────────────────────────────────────────
export default function CandidateDashboard() {
  const user = useAuthStore((st) => st.user);
  const greeting = getGreeting();

  return (
    <MainLayout title="Dashboard" breadcrumb="Trang chủ / Ứng viên">
      <div className={s.page}>

        {/* Welcome banner */}
        <div className={s.banner}>
          <div className={s.bannerBg} />
          <div className={s.bannerBg2} />
          <div className={s.bannerText}>
            <h2>{greeting}, {user?.hoTen?.split(' ').pop() ?? 'bạn'} 👋</h2>
            <p>Hôm nay là ngày tốt để tìm kiếm cơ hội mới. Hành trình của bạn bắt đầu từ đây!</p>
          </div>
          <div className={s.bannerEmoji}>🎯</div>
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
              trendValue={st.trendValue}
            />
          ))}
        </div>

        {/* Main content row */}
        <div className={s.rowTwo}>

          {/* Recent applications */}
          <div className={s.card}>
            <div className={s.sectionHead}>
              <span className={s.cardTitle}>Đơn ứng tuyển gần đây</span>
              <button className={s.seeAll}>Xem tất cả →</button>
            </div>
            <div className={s.jobList}>
              {RECENT_APPLICATIONS.map((app) => (
                <div key={app.id} className={s.jobItem}>
                  <div className={s.jobLogo}>💼</div>
                  <div className={s.jobInfo}>
                    <div className={s.jobTitle}>{app.title}</div>
                    <div className={s.jobMeta}>{app.company} · {app.time}</div>
                  </div>
                  <span className={`${s.jobStatus} ${s[app.status as keyof typeof s]}`}>
                    {STATUS_LABEL[app.status]}
                  </span>
                </div>
              ))}
            </div>
          </div>

          {/* Quick actions + recommended jobs */}
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

            {/* Recommended jobs */}
            <div className={s.card}>
              <div className={s.sectionHead}>
                <span className={s.cardTitle}>Việc làm gợi ý</span>
                <button className={s.seeAll}>Xem thêm →</button>
              </div>
              <div className={s.jobList}>
                {RECENT_JOBS.map((job) => (
                  <div key={job.id} className={s.jobItem}>
                    <div className={s.jobLogo}>{job.icon}</div>
                    <div className={s.jobInfo}>
                      <div className={s.jobTitle}>{job.title}</div>
                      <div className={s.jobMeta}>{job.company} · {job.location} · {job.salary}</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>

      </div>
    </MainLayout>
  );
}

function getGreeting() {
  const h = new Date().getHours();
  if (h < 12) return 'Chào buổi sáng';
  if (h < 18) return 'Chào buổi chiều';
  return 'Chào buổi tối';
}
