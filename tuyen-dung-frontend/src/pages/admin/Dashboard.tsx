import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import MainLayout from '@/layouts/MainLayout';
import AppDataTable, { type AppDataColumn } from '@/components/common/AppDataTable';
import StatCard from '@/components/common/StatCard';
import AdminRoleNav from '@/components/admin/AdminRoleNav';
import { ROUTES } from '@/constants/routes';
import { companyService } from '@/services/modules/company.module';
import { jobService } from '@/services/modules/job.module';
import { notificationService } from '@/services/modules/notification.module';
import { skillService } from '@/services/modules/skill.module';
import type { CompanyItem } from '@/types/company.types';
import type { JobPosting } from '@/types/job.types';
import a from '@/assets/styles/admin-console.module.css';

function mapError(error: unknown): string {
  return (
    (error as { response?: { data?: { message?: string } } })?.response?.data?.message ??
    'Không thể tải dashboard admin.'
  );
}

function formatDate(value?: string): string {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleDateString('vi-VN');
}

export default function AdminDashboard() {
  const [jobs, setJobs] = useState<JobPosting[]>([]);
  const [companies, setCompanies] = useState<CompanyItem[]>([]);
  const [skillCount, setSkillCount] = useState(0);
  const [unreadNotifications, setUnreadNotifications] = useState(0);
  const [totalActiveJobs, setTotalActiveJobs] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let mounted = true;

    const loadData = async () => {
      setLoading(true);
      setError('');
      try {
        const [activeJobsPage, companyList, skillList, unreadCount] = await Promise.all([
          jobService.getActiveJobs(0, 5),
          companyService.getAllCompanies(),
          skillService.getAllSkills(),
          notificationService.countUnread(),
        ]);

        if (!mounted) return;
        setJobs(activeJobsPage.content);
        setTotalActiveJobs(activeJobsPage.totalElements);
        setCompanies(companyList);
        setSkillCount(skillList.length);
        setUnreadNotifications(unreadCount);
      } catch (err) {
        if (!mounted) return;
        setError(mapError(err));
      } finally {
        if (mounted) setLoading(false);
      }
    };

    void loadData();
    return () => {
      mounted = false;
    };
  }, []);

  const statCards = useMemo(
    () => [
      { icon: '📢', label: 'Tin đang mở', value: totalActiveJobs, accent: 'indigo', trend: 'neutral' },
      { icon: '🏢', label: 'Tổng công ty', value: companies.length, accent: 'sky', trend: 'neutral' },
      { icon: '🧠', label: 'Kỹ năng hệ thống', value: skillCount, accent: 'amber', trend: 'neutral' },
      { icon: '🔔', label: 'Thông báo chưa đọc', value: unreadNotifications, accent: 'emerald', trend: 'neutral' },
    ],
    [totalActiveJobs, companies.length, skillCount, unreadNotifications]
  );

  const jobColumns: AppDataColumn<JobPosting>[] = [
    {
      key: 'tieuDe',
      header: 'Vị trí',
      render: (row) => <strong>{row.tieuDe}</strong>,
    },
    {
      key: 'tenCongTy',
      header: 'Công ty',
      width: '180px',
      render: (row) => row.tenCongTy,
    },
    {
      key: 'hanNop',
      header: 'Hạn nộp',
      width: '140px',
      render: (row) => formatDate(row.hanNop),
    },
  ];

  const quickActions = [
    { icon: '👥', label: 'Người dùng', to: ROUTES.admin.users },
    { icon: '📢', label: 'Tin tuyển dụng', to: ROUTES.admin.jobs },
    { icon: '🏢', label: 'Công ty', to: ROUTES.admin.companies },
    { icon: '🧠', label: 'Kỹ năng', to: ROUTES.admin.skills },
  ];

  return (
    <MainLayout title="Admin Dashboard" breadcrumb="Trang chủ / Quản trị">
      <div className={a.page}>
        <AdminRoleNav />

        <section className={a.hero}>
          <h2>Trung tâm điều hành quản trị</h2>
          <p>
            Giao diện admin được gom theo luồng xử lý rõ ràng, bám sát các endpoint backend đang có.
          </p>
          <div className={a.heroActions}>
            <Link className={`${a.btn} ${a.btnPrimary}`} to={ROUTES.admin.jobs}>Quản lý tin tuyển dụng</Link>
            <Link className={`${a.btn} ${a.btnGhost}`} to={ROUTES.admin.companies}>Quản lý công ty</Link>
            <Link className={`${a.btn} ${a.btnGhost}`} to={ROUTES.admin.skills}>Quản lý kỹ năng</Link>
          </div>
        </section>

        <section className={a.metrics}>
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
        </section>

        {loading ? <div className={a.notice}>Đang tải dữ liệu admin...</div> : null}
        {error ? <div className={`${a.notice} ${a.noticeError}`}>{error}</div> : null}

        <div className={a.grid2}>
          <section className={a.surface}>
            <div className={a.sectionHead}>
              <h3 className={a.sectionTitle}>Tin tuyển dụng gần nhất</h3>
              <Link className={a.link} to={ROUTES.admin.jobs}>Đi tới màn quản lý</Link>
            </div>
            <p className={a.sectionHint}>Dữ liệu lấy từ API jobs active, hiển thị nhanh để admin xử lý.</p>
            <AppDataTable
              columns={jobColumns}
              data={jobs}
              rowKey={(row) => String(row.id)}
              emptyMessage="Không có tin đang mở."
            />
          </section>

          <div className={a.stack}>
            <section className={a.surface}>
              <div className={a.sectionHead}>
                <h3 className={a.sectionTitle}>Thao tác nhanh</h3>
                <p className={a.sectionHint}>Đi theo thứ tự từ dữ liệu nền đến kiểm duyệt</p>
              </div>
              <div className={a.list}>
                {quickActions.map((item) => (
                  <Link key={item.label} className={a.listItem} to={item.to}>
                    <div className={a.listTitle}>{item.icon} {item.label}</div>
                    <div className={a.muted}>Mở trang xử lý chi tiết</div>
                  </Link>
                ))}
              </div>
            </section>

            <section className={a.surface}>
              <div className={a.sectionHead}>
                <h3 className={a.sectionTitle}>Công ty gần nhất</h3>
                <Link className={a.link} to={ROUTES.admin.companies}>Mở danh sách công ty</Link>
              </div>
              <div className={a.chips}>
                <span className={a.chip}>Tổng công ty: {companies.length}</span>
                <span className={a.chip}>Thông báo chưa đọc: {unreadNotifications}</span>
              </div>
              {companies.length === 0 ? (
                <div className={a.notice}>Chưa có công ty nào.</div>
              ) : (
                <div className={a.list}>
                  {companies.slice(0, 5).map((company) => (
                    <div key={company.id} className={a.listItem}>
                      <div className={a.listTitle}>{company.tenCongTy}</div>
                      <div className={a.muted}>MST: {company.maSoThue || 'Chưa cập nhật'} · Tin mở: {company.soTinDangMo}</div>
                    </div>
                  ))}
                </div>
              )}
            </section>
          </div>
        </div>
      </div>
    </MainLayout>
  );
}
