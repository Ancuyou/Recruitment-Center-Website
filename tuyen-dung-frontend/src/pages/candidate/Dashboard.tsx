import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import MainLayout from '@/layouts/MainLayout';
import StatCard from '@/components/common/StatCard';
import { ROUTES } from '@/constants/routes';
import { useAuthStore } from '@/store/auth.store';
import { savedJobsLocal } from '@/services/local/saved-jobs.local';
import { dashboardService } from '@/services/modules/dashboard.module';
import { applicationService } from '@/services/modules/application.module';
import { jobService } from '@/services/modules/job.module';
import { matchingService } from '@/services/modules/matching.module';
import type { CandidateDashboardStats } from '@/types/dashboard.types';
import type { ApplicationItem } from '@/types/application.types';
import type { JobPosting } from '@/types/job.types';
import type { JobSuggestion } from '@/types/matching.types';
import s from '@/assets/styles/dashboard.module.css';

const EMPTY_STATS: CandidateDashboardStats = {
  tongSoDonDaNop: 0,
  soDonDangCho: 0,
  soLichPhongVan: 0,
  soThongBaoChuaDoc: 0,
};

function getGreeting() {
  const h = new Date().getHours();
  if (h < 12) return 'Chào buổi sáng';
  if (h < 18) return 'Chào buổi chiều';
  return 'Chào buổi tối';
}

function mapError(error: unknown): string {
  return (
    (error as { response?: { data?: { message?: string } } })?.response?.data?.message ??
    'Không thể tải dashboard ứng viên.'
  );
}

function formatDate(value?: string): string {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleDateString('vi-VN');
}

function formatSalary(min?: number, max?: number): string {
  if (min == null && max == null) return 'Thỏa thuận';
  if (min != null && max != null) {
    return `${Number(min).toLocaleString('vi-VN')} - ${Number(max).toLocaleString('vi-VN')} VND`;
  }
  if (min != null) return `Từ ${Number(min).toLocaleString('vi-VN')} VND`;
  return `Đến ${Number(max).toLocaleString('vi-VN')} VND`;
}

function toStatusClass(status: number): string {
  if (status === 1) return 'statusPending';
  if (status === 2 || status === 3) return 'statusReview';
  if (status === 4) return 'statusAccepted';
  return 'statusRejected';
}

function toCandidateJobDetailPath(jobId: number): string {
  return ROUTES.candidate.jobDetail.replace(':id', String(jobId));
}

export default function CandidateDashboard() {
  const user = useAuthStore((st) => st.user);
  const greeting = getGreeting();
  const [stats, setStats] = useState<CandidateDashboardStats>(EMPTY_STATS);
  const [recentApplications, setRecentApplications] = useState<ApplicationItem[]>([]);
  const [hotJobs, setHotJobs] = useState<JobPosting[]>([]);
  const [recommendedJobs, setRecommendedJobs] = useState<JobSuggestion[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const savedCount = useMemo(() => savedJobsLocal.listIds().length, []);

  useEffect(() => {
    let mounted = true;

    const loadDashboard = async () => {
      setLoading(true);
      setError('');
      try {
        const matchingPromise: Promise<JobSuggestion[]> = user?.taiKhoanId
          ? matchingService.suggestJobsForCandidate(user.taiKhoanId, 5).catch(() => [])
          : Promise.resolve([]);

        const [statsData, applicationsPage, jobsPage, matchingJobs] = await Promise.all([
          dashboardService.getCandidateDashboard(),
          applicationService.getCandidateApplications(0, 5),
          jobService.getActiveJobs(0, 5),
          matchingPromise,
        ]);
        if (!mounted) return;

        setStats(statsData);
        setRecentApplications(applicationsPage.content);
        setHotJobs(jobsPage.content);
        setRecommendedJobs(matchingJobs);
      } catch (err) {
        if (!mounted) return;
        setError(mapError(err));
      } finally {
        if (mounted) setLoading(false);
      }
    };

    void loadDashboard();
    return () => {
      mounted = false;
    };
  }, [user?.taiKhoanId]);

  const statCards = [
    { icon: '📋', label: 'Đã ứng tuyển', value: stats.tongSoDonDaNop, accent: 'indigo', trend: 'neutral' },
    { icon: '⏳', label: 'Đơn đang chờ', value: stats.soDonDangCho, accent: 'sky', trend: 'neutral' },
    { icon: '📞', label: 'Lịch phỏng vấn', value: stats.soLichPhongVan, accent: 'emerald', trend: 'neutral' },
    { icon: '❤️', label: 'Việc đã lưu', value: savedCount, accent: 'amber', trend: 'neutral' },
  ] as const;

  const quickActions = [
    { icon: '🔍', label: 'Tìm việc ngay', to: ROUTES.candidate.jobs },
    { icon: '📄', label: 'Cập nhật CV', to: ROUTES.candidate.cv },
    { icon: '👤', label: 'Hoàn thiện hồ sơ', to: ROUTES.candidate.profile },
    { icon: '🔔', label: 'Xem thông báo', to: ROUTES.candidate.notifications },
  ];

  return (
    <MainLayout title="Dashboard" breadcrumb="Trang chủ / Ứng viên">
      <div className={s.page}>
        <div className={s.banner}>
          <div className={s.bannerBg} />
          <div className={s.bannerBg2} />
          <div className={s.bannerText}>
            <h2>{greeting}, {user?.hoTen?.split(' ').pop() ?? 'bạn'} 👋</h2>
            <p>
              Theo dõi hành trình ứng tuyển của bạn theo dữ liệu backend thời gian thực.
            </p>
          </div>
          <div className={s.bannerEmoji}>🎯</div>
        </div>

        <div className={s.statsGrid}>
          {statCards.map((st) => (
            <StatCard
              key={st.label}
              icon={st.icon}
              label={st.label}
              value={st.value}
              accent={st.accent as never}
              trend={st.trend as never}
            />
          ))}
        </div>

        {loading ? <div className={s.card}>Đang tải dữ liệu dashboard...</div> : null}
        {error ? <div className={s.card} style={{ color: '#b91c1c' }}>{error}</div> : null}

        <div className={s.rowTwo}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-6)' }}>
            <div className={s.card}>
              <div className={s.sectionHead}>
                <span className={s.cardTitle}>Đơn ứng tuyển gần đây</span>
                <Link className={s.seeAll} to={ROUTES.candidate.applications}>Xem tất cả →</Link>
              </div>
              {recentApplications.length === 0 ? (
                <div className={s.empty}>
                  <span>📭</span>
                  <span>Bạn chưa có đơn ứng tuyển nào.</span>
                </div>
              ) : (
                <div className={s.jobList}>
                  {recentApplications.map((app) => (
                    <div key={app.id} className={s.jobItem}>
                      <div className={s.jobLogo}>💼</div>
                      <div className={s.jobInfo}>
                        <div className={s.jobTitle}>{app.tieuDeTin}</div>
                        <div className={s.jobMeta}>{app.tenCongTy} · {formatDate(app.ngayNop)}</div>
                      </div>
                      <span className={`${s.jobStatus} ${s[toStatusClass(app.trangThai) as keyof typeof s]}`}>
                        {app.trangThaiLabel}
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className={s.card}>
              <div className={s.sectionHead}>
                <span className={s.cardTitle}>Việc phù hợp theo hồ sơ</span>
                <Link className={s.seeAll} to={ROUTES.candidate.jobs}>Xem thêm →</Link>
              </div>
              {recommendedJobs.length === 0 ? (
                <div className={s.empty}>
                  <span>🧭</span>
                  <span>Chưa có gợi ý phù hợp từ matching.</span>
                </div>
              ) : (
                <div className={s.jobList}>
                  {recommendedJobs.map((job) => (
                    <div key={`${job.jobId}-${job.calculatedAt}`} className={s.jobItem}>
                      <div className={s.jobLogo}>✨</div>
                      <div className={s.jobInfo}>
                        <div className={s.jobTitle}>
                          <Link to={toCandidateJobDetailPath(job.jobId)} style={{ color: 'inherit', textDecoration: 'none' }}>
                            {job.tenVitri}
                          </Link>
                        </div>
                        <div className={s.jobMeta}>
                          {job.congTyName} · {job.diaDiem || 'Nhiều khu vực'} · Match {job.matchPercentage}%
                        </div>
                      </div>
                      <span className={`${s.jobStatus} ${s.statusAccepted}`}>{job.matchLevel}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-6)' }}>
            <div className={s.card}>
              <span className={s.cardTitle}>Thao tác nhanh</span>
              <div className={s.quickActions} style={{ marginTop: 'var(--space-4)' }}>
                {quickActions.map((a) => (
                  <Link key={a.label} className={s.quickBtn} to={a.to}>
                    <span>{a.icon}</span>
                    <span>{a.label}</span>
                  </Link>
                ))}
              </div>
            </div>

            <div className={s.card}>
              <div className={s.sectionHead}>
                <span className={s.cardTitle}>Việc làm nổi bật</span>
                <Link className={s.seeAll} to={ROUTES.candidate.jobs}>Xem thêm →</Link>
              </div>
              {hotJobs.length === 0 ? (
                <div className={s.empty}>
                  <span>📢</span>
                  <span>Chưa có tin tuyển dụng mở.</span>
                </div>
              ) : (
                <div className={s.jobList}>
                  {hotJobs.map((job) => (
                    <div key={job.id} className={s.jobItem}>
                      <div className={s.jobLogo}>🚀</div>
                      <div className={s.jobInfo}>
                        <div className={s.jobTitle}>{job.tieuDe}</div>
                        <div className={s.jobMeta}>
                          {job.tenCongTy} · {job.diaDiem || 'Nhiều khu vực'} · {formatSalary(job.mucLuongMin, job.mucLuongMax)}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </MainLayout>
  );
}
