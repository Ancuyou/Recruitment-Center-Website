import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import MainLayout from '@/layouts/MainLayout';
import StatCard from '@/components/common/StatCard';
import { ROUTES } from '@/constants/routes';
import { useAuthStore } from '@/store/auth.store';
import { dashboardService } from '@/services/modules/dashboard.module';
import { jobService } from '@/services/modules/job.module';
import { applicationService } from '@/services/modules/application.module';
import type { RecruiterDashboardStats } from '@/types/dashboard.types';
import type { JobPosting } from '@/types/job.types';
import type { ApplicationItem } from '@/types/application.types';
import s from '@/assets/styles/dashboard.module.css';

function mapError(error: unknown, fallback: string): string {
  return (
    (error as { response?: { data?: { message?: string } } })?.response?.data?.message ?? fallback
  );
}

function formatDate(value?: string): string {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleDateString('vi-VN');
}

const EMPTY_STATS: RecruiterDashboardStats = {
  tongSoTinDangMo: 0,
  tongSoDonUngTuyen: 0,
  soDonMoi: 0,
  soLichPhongVanSapToi: 0,
};

// ─── Component ────────────────────────────────────────────────────
export default function RecruiterDashboard() {
  const user = useAuthStore((st) => st.user);
  const [stats, setStats] = useState<RecruiterDashboardStats>(EMPTY_STATS);
  const [jobs, setJobs] = useState<JobPosting[]>([]);
  const [recentApplications, setRecentApplications] = useState<ApplicationItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadDashboard = async () => {
      setLoading(true);
      setError('');
      try {
        const [statsData, myJobs] = await Promise.all([
          dashboardService.getRecruiterDashboard(),
          jobService.getMyJobs(),
        ]);

        setStats(statsData);
        setJobs(myJobs);

        if (myJobs.length > 0) {
          const appPage = await applicationService.getRecruiterApplications(myJobs[0].id, 0, 5);
          setRecentApplications(appPage.content);
        } else {
          setRecentApplications([]);
        }
      } catch (err) {
        setError(mapError(err, 'Không thể tải dữ liệu dashboard nhà tuyển dụng.'));
      } finally {
        setLoading(false);
      }
    };

    void loadDashboard();
  }, []);

  const statCards = useMemo(
    () => [
      {
        icon: '📢',
        label: 'Tin đang tuyển',
        value: stats.tongSoTinDangMo,
        accent: 'indigo',
        trend: 'neutral',
      },
      {
        icon: '📨',
        label: 'Tổng đơn ứng tuyển',
        value: stats.tongSoDonUngTuyen,
        accent: 'sky',
        trend: 'neutral',
      },
      {
        icon: '📥',
        label: 'Hồ sơ mới',
        value: stats.soDonMoi,
        accent: 'amber',
        trend: stats.soDonMoi > 0 ? 'up' : 'neutral',
      },
      {
        icon: '📅',
        label: 'Phỏng vấn sắp tới',
        value: stats.soLichPhongVanSapToi,
        accent: 'emerald',
        trend: 'neutral',
      },
    ],
    [stats]
  );

  const quickActions = [
    { icon: '📢', label: 'Đăng tin tuyển dụng', path: ROUTES.recruiter.jobs },
    { icon: '📥', label: 'Quản lý ứng viên', path: ROUTES.recruiter.applicants },
    { icon: '🏢', label: 'Cập nhật công ty', path: ROUTES.recruiter.company },
    { icon: '👤', label: 'Hồ sơ ứng viên', path: ROUTES.recruiter.candidateProfiles },
  ];

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
          {statCards.map((st) => (
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

        {loading ? <div className={s.card}>Đang tải dữ liệu dashboard...</div> : null}
        {error ? <div className={s.card} style={{ color: '#b91c1c' }}>{error}</div> : null}

        {/* Row */}
        <div className={s.rowTwo}>

          {/* New candidates */}
          <div className={s.card}>
            <div className={s.sectionHead}>
              <span className={s.cardTitle}>Ứng viên mới nộp hồ sơ</span>
              <Link className={s.seeAll} to={ROUTES.recruiter.applicants}>Xem tất cả →</Link>
            </div>
            {recentApplications.length === 0 ? (
              <div className={s.empty}>
                <span>📭</span>
                <span>Chưa có ứng viên mới.</span>
              </div>
            ) : (
              <div className={s.activityList}>
                {recentApplications.map((item) => (
                  <div key={item.id} className={s.activityItem}>
                    <div className={s.activityIcon}>👤</div>
                    <div className={s.activityContent}>
                      <div className={s.activityTitle}>{item.tenUngVien} — {item.tieuDeTin}</div>
                      <div className={s.activitySub}>{item.emailUngVien} · {formatDate(item.ngayNop)}</div>
                    </div>
                    <span className={`${s.jobStatus} ${s.statusReview}`}>{item.trangThaiLabel}</span>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Right column */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-6)' }}>
            {/* Quick actions */}
            <div className={s.card}>
              <span className={s.cardTitle}>Thao tác nhanh</span>
              <div className={s.quickActions} style={{ marginTop: 'var(--space-4)' }}>
                {quickActions.map((a) => (
                  <Link key={a.label} className={s.quickBtn} to={a.path}>
                    <span>{a.icon}</span>
                    <span>{a.label}</span>
                  </Link>
                ))}
              </div>
            </div>

            {/* Active job posts */}
            <div className={s.card}>
              <div className={s.sectionHead}>
                <span className={s.cardTitle}>Tin đang hoạt động</span>
                <Link className={s.seeAll} to={ROUTES.recruiter.jobs}>Quản lý →</Link>
              </div>
              {jobs.length === 0 ? (
                <div className={s.empty}>
                  <span>📢</span>
                  <span>Bạn chưa có tin tuyển dụng nào.</span>
                </div>
              ) : (
                <table className={s.simpleTable}>
                  <thead>
                    <tr>
                      <th>Vị trí</th>
                      <th>Hồ sơ</th>
                      <th>Hạn nộp</th>
                    </tr>
                  </thead>
                  <tbody>
                    {jobs.slice(0, 5).map((job) => (
                      <tr key={job.id}>
                        <td>{job.tieuDe}</td>
                        <td>
                          <span className={`${s.jobStatus} ${s.statusReview}`}>{job.soLuongDon}</span>
                        </td>
                        <td style={{ fontSize: 12, color: 'var(--color-text-muted)' }}>{formatDate(job.hanNop)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>

      </div>
    </MainLayout>
  );
}
