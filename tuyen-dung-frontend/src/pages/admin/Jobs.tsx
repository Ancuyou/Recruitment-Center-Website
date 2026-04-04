import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import MainLayout from '@/layouts/MainLayout';
import AppDataTable, { type AppDataColumn } from '@/components/common/AppDataTable';
import AdminRoleNav from '@/components/admin/AdminRoleNav';
import { ROUTES } from '@/constants/routes';
import { jobService } from '@/services/modules/job.module';
import type { PageResponse } from '@/types/api.types';
import type { JobPosting, JobSkill, KhuVuc } from '@/types/job.types';
import a from '@/assets/styles/admin-console.module.css';

function mapError(error: unknown): string {
  return (
    (error as { response?: { data?: { message?: string } } })?.response?.data?.message ??
    'Không thể tải danh sách tin tuyển dụng.'
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

function mapKhuVucLabel(value: KhuVuc): string {
  return value.replace(/_/g, ' ');
}

function toPublicJobDetailPath(jobId: number): string {
  return ROUTES.public.jobDetail.replace(':id', String(jobId));
}

export default function AdminJobsPage() {
  const [page, setPage] = useState(0);
  const [keyword, setKeyword] = useState('');
  const [selectedJobId, setSelectedJobId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [error, setError] = useState('');
  const [detailError, setDetailError] = useState('');
  const [message, setMessage] = useState('');
  const [selectedJob, setSelectedJob] = useState<JobPosting | null>(null);
  const [selectedLocations, setSelectedLocations] = useState<KhuVuc[]>([]);
  const [selectedSkills, setSelectedSkills] = useState<JobSkill[]>([]);
  const [pageData, setPageData] = useState<PageResponse<JobPosting>>({
    content: [],
    totalElements: 0,
    totalPages: 0,
    number: 0,
    size: 10,
  });

  const fetchJobs = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await jobService.getActiveJobs(page, 10);
      setPageData(data);
      setSelectedJobId((previous) => {
        if (previous != null && data.content.some((job) => job.id === previous)) return previous;
        return data.content[0]?.id ?? null;
      });
    } catch (err) {
      setError(mapError(err));
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    void fetchJobs();
  }, [fetchJobs]);

  const fetchJobDetail = useCallback(async (jobId: number) => {
    setDetailLoading(true);
    setDetailError('');
    try {
      const [jobDetail, locations, skills] = await Promise.all([
        jobService.getJobById(jobId),
        jobService.getJobLocations(jobId),
        jobService.getJobSkills(jobId),
      ]);
      setSelectedJob(jobDetail);
      setSelectedLocations(locations);
      setSelectedSkills(skills);
    } catch (err) {
      setDetailError(mapError(err));
      setSelectedJob(null);
      setSelectedLocations([]);
      setSelectedSkills([]);
    } finally {
      setDetailLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!selectedJobId) {
      setSelectedJob(null);
      setSelectedLocations([]);
      setSelectedSkills([]);
      setDetailError('');
      return;
    }
    void fetchJobDetail(selectedJobId);
  }, [selectedJobId, fetchJobDetail]);

  const visibleJobs = useMemo(() => {
    const normalized = keyword.trim().toLowerCase();
    if (!normalized) return pageData.content;
    return pageData.content.filter((job) =>
      [job.tieuDe, job.tenCongTy, job.tenNhaTuyenDung]
        .filter(Boolean)
        .join(' ')
        .toLowerCase()
        .includes(normalized)
    );
  }, [keyword, pageData.content]);

  const handleSelectJob = (jobId: number) => {
    setSelectedJobId(jobId);
    setMessage('');
    setError('');
  };

  const handleCloseJob = async (jobId: number) => {
    const confirmed = window.confirm('Bạn có chắc muốn đóng tin tuyển dụng này?');
    if (!confirmed) return;

    setLoading(true);
    setError('');
    setMessage('');
    try {
      await jobService.closeJob(jobId);
      setMessage('Đóng tin tuyển dụng thành công.');
      await fetchJobs();
    } catch (err) {
      setError(mapError(err));
    } finally {
      setLoading(false);
    }
  };

  const columns: AppDataColumn<JobPosting>[] = [
    {
      key: 'tieuDe',
      header: 'Tin tuyển dụng',
      render: (row) => (
        <div style={{ display: 'grid', gap: 4 }}>
          <strong>{row.tieuDe}</strong>
          <span className={a.muted}>{row.tenCongTy} · {row.tenNhaTuyenDung}</span>
          {selectedJobId === row.id ? <span className={a.chip}>Đang xem chi tiết</span> : null}
        </div>
      ),
    },
    {
      key: 'trangThaiLabel',
      header: 'Trạng thái',
      width: '140px',
      render: (row) => row.trangThaiLabel || 'N/A',
    },
    {
      key: 'soLuongDon',
      header: 'Số đơn',
      width: '100px',
      render: (row) => row.soLuongDon,
    },
    {
      key: 'hanNop',
      header: 'Hạn nộp',
      width: '140px',
      render: (row) => formatDate(row.hanNop),
    },
    {
      key: 'actions',
      header: 'Tác vụ',
      width: '260px',
      align: 'center',
      render: (row) => (
        <div className={a.toolbar} style={{ justifyContent: 'center' }}>
          <button
            type="button"
            className={`${a.btn} ${a.btnGhost}`}
            onClick={() => handleSelectJob(row.id)}
            disabled={loading}
          >
            Chi tiết
          </button>
          <button
            type="button"
            className={`${a.btn} ${a.btnDanger}`}
            onClick={() => void handleCloseJob(row.id)}
            disabled={loading}
          >
            Đóng tin
          </button>
        </div>
      ),
    },
  ];

  return (
    <MainLayout title="Tin tuyển dụng" breadcrumb="Trang chủ / Admin / Tin tuyển dụng">
      <div className={a.page}>
        <AdminRoleNav />

        <section className={a.surface}>
          <div className={a.sectionHead}>
            <h3 className={a.sectionTitle}>Điều phối tin tuyển dụng</h3>
            <div className={a.chips}>
              <span className={a.chip}>Tổng tin trang hiện tại: {pageData.content.length}</span>
              <span className={a.chip}>Tổng tin đang mở: {pageData.totalElements}</span>
              {selectedJobId ? <span className={a.chip}>Đang chọn ID: {selectedJobId}</span> : null}
            </div>
          </div>
          <div className={a.toolbar}>
            <input
              className={a.input}
              style={{ maxWidth: 320 }}
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder="Lọc nhanh theo vị trí/công ty/người đăng"
            />
            <button type="button" className={`${a.btn} ${a.btnGhost}`} onClick={() => void fetchJobs()}>
              Làm mới dữ liệu
            </button>
          </div>
        </section>

        {loading ? <div className={a.notice}>Đang tải dữ liệu...</div> : null}
        {error ? <div className={`${a.notice} ${a.noticeError}`}>{error}</div> : null}
        {message ? <div className={`${a.notice} ${a.noticeSuccess}`}>{message}</div> : null}

        <section className={a.surface}>
          <div className={a.sectionHead}>
            <h3 className={a.sectionTitle}>Danh sách tin tuyển dụng đang mở</h3>
            {keyword.trim() ? <span className={a.chip}>Kết quả lọc: {visibleJobs.length}</span> : null}
          </div>
          <AppDataTable
            columns={columns}
            data={visibleJobs}
            rowKey={(row) => String(row.id)}
            emptyMessage="Không có tin tuyển dụng đang mở."
          />
        </section>

        <section className={a.surface}>
          <div className={a.sectionHead}>
            <h3 className={a.sectionTitle}>Chi tiết tin tuyển dụng</h3>
            {selectedJob ? (
              <Link
                className={a.link}
                to={toPublicJobDetailPath(selectedJob.id)}
                target="_blank"
                rel="noreferrer"
              >
                Mở bản public (tab mới)
              </Link>
            ) : null}
          </div>

          {!selectedJobId ? <div className={a.notice}>Hãy chọn một tin trong bảng để xem chi tiết.</div> : null}
          {selectedJobId && detailLoading ? <div className={a.notice}>Đang tải chi tiết tin tuyển dụng...</div> : null}
          {selectedJobId && detailError ? <div className={`${a.notice} ${a.noticeError}`}>{detailError}</div> : null}

          {selectedJobId && !detailLoading && !detailError && selectedJob ? (
            <>
              <div className={a.kvGrid}>
                <div className={a.kvItem}>
                  <span className={a.kvLabel}>Tiêu đề</span>
                  <span className={a.kvValue}>{selectedJob.tieuDe}</span>
                </div>
                <div className={a.kvItem}>
                  <span className={a.kvLabel}>Doanh nghiệp</span>
                  <span className={a.kvValue}>{selectedJob.tenCongTy}</span>
                </div>
                <div className={a.kvItem}>
                  <span className={a.kvLabel}>Nhà tuyển dụng</span>
                  <span className={a.kvValue}>{selectedJob.tenNhaTuyenDung}</span>
                </div>
                <div className={a.kvItem}>
                  <span className={a.kvLabel}>Mức lương</span>
                  <span className={a.kvValue}>{formatSalary(selectedJob.mucLuongMin, selectedJob.mucLuongMax)}</span>
                </div>
                <div className={a.kvItem}>
                  <span className={a.kvLabel}>Hình thức làm việc</span>
                  <span className={a.kvValue}>{selectedJob.hinhThucLamViec || 'Chưa cập nhật'}</span>
                </div>
                <div className={a.kvItem}>
                  <span className={a.kvLabel}>Cấp bậc yêu cầu</span>
                  <span className={a.kvValue}>{selectedJob.capBacYeuCau || 'Chưa cập nhật'}</span>
                </div>
                <div className={a.kvItem}>
                  <span className={a.kvLabel}>Hạn nộp</span>
                  <span className={a.kvValue}>{formatDate(selectedJob.hanNop)}</span>
                </div>
                <div className={a.kvItem}>
                  <span className={a.kvLabel}>Số đơn ứng tuyển</span>
                  <span className={a.kvValue}>{String(selectedJob.soLuongDon)}</span>
                </div>
              </div>

              <div className={a.grid2}>
                <div className={a.list}>
                  <div className={a.listItem}>
                    <div className={a.listTitle}>Mô tả công việc</div>
                    <div className={a.muted}>{selectedJob.moTaCongViec || 'Chưa có mô tả công việc.'}</div>
                  </div>
                  <div className={a.listItem}>
                    <div className={a.listTitle}>Yêu cầu ứng viên</div>
                    <div className={a.muted}>{selectedJob.yeuCauUngVien || 'Chưa có yêu cầu ứng viên.'}</div>
                  </div>
                  <div className={a.listItem}>
                    <div className={a.listTitle}>Địa điểm hiển thị</div>
                    <div className={a.muted}>{selectedJob.diaDiem || 'Nhiều khu vực'}</div>
                  </div>
                </div>

                <div className={a.list}>
                  <div className={a.listItem}>
                    <div className={a.listTitle}>Khu vực áp dụng</div>
                    {selectedLocations.length === 0 ? (
                      <div className={a.muted}>Chưa có dữ liệu khu vực.</div>
                    ) : (
                      <div className={a.chips}>
                        {selectedLocations.map((location) => (
                          <span key={location} className={a.chip}>{mapKhuVucLabel(location)}</span>
                        ))}
                      </div>
                    )}
                  </div>

                  <div className={a.listItem}>
                    <div className={a.listTitle}>Kỹ năng yêu cầu</div>
                    {selectedSkills.length === 0 ? (
                      <div className={a.muted}>Tin chưa có kỹ năng yêu cầu.</div>
                    ) : (
                      <div className={a.list}>
                        {selectedSkills.map((skill) => (
                          <div key={skill.id} className={a.listItem}>
                            <div className={a.listTitle}>{skill.tenKyNang}</div>
                            <div className={a.muted}>Mức {skill.yeucau}/5 {skill.moTa ? `· ${skill.moTa}` : ''}</div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </>
          ) : null}
        </section>

        <div className={a.toolbar}>
          <button
            type="button"
            className={`${a.btn} ${a.btnGhost}`}
            disabled={page === 0 || loading}
            onClick={() => setPage((prev) => Math.max(0, prev - 1))}
          >
            ← Trang trước
          </button>
          <button
            type="button"
            className={`${a.btn} ${a.btnGhost}`}
            disabled={loading || page >= Math.max(pageData.totalPages - 1, 0)}
            onClick={() => setPage((prev) => prev + 1)}
          >
            Trang sau →
          </button>
        </div>
      </div>
    </MainLayout>
  );
}
