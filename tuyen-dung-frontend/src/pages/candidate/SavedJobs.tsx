import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import MainLayout from '@/layouts/MainLayout';
import { ROUTES } from '@/constants/routes';
import { savedJobsLocal } from '@/services/local/saved-jobs.local';
import { jobService } from '@/services/modules/job.module';
import type { JobPosting } from '@/types/job.types';
import s from '@/assets/styles/candidate-workflow.module.css';

function mapError(error: unknown): string {
  return (
    (error as { response?: { data?: { message?: string } } })?.response?.data?.message ??
    'Không thể tải danh sách việc đã lưu.'
  );
}

function formatSalary(min?: number, max?: number): string {
  if (min == null && max == null) return 'Thỏa thuận';
  if (min != null && max != null) {
    return `${Number(min).toLocaleString('vi-VN')} - ${Number(max).toLocaleString('vi-VN')} VND`;
  }
  if (min != null) return `Từ ${Number(min).toLocaleString('vi-VN')} VND`;
  return `Đến ${Number(max).toLocaleString('vi-VN')} VND`;
}

function toJobDetailPath(id: number): string {
  return ROUTES.public.jobDetail.replace(':id', String(id));
}

export default function CandidateSavedJobsPage() {
  const [savedIds, setSavedIds] = useState<number[]>([]);
  const [jobs, setJobs] = useState<JobPosting[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const loadSavedJobs = useCallback(async () => {
    setLoading(true);
    setError('');

    const ids = savedJobsLocal.listIds();
    setSavedIds(ids);

    if (ids.length === 0) {
      setJobs([]);
      setLoading(false);
      return;
    }

    try {
      const results = await Promise.allSettled(ids.map((id) => jobService.getJobById(id)));
      const loadedJobs = results
        .filter((result): result is PromiseFulfilledResult<JobPosting> => result.status === 'fulfilled')
        .map((result) => result.value);

      setJobs(loadedJobs);
    } catch (err) {
      setError(mapError(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadSavedJobs();
  }, [loadSavedJobs]);

  const handleRemove = (jobId: number) => {
    const next = savedJobsLocal.remove(jobId);
    setSavedIds(next);
    setJobs((prev) => prev.filter((job) => job.id !== jobId));
  };

  const handleClear = () => {
    savedJobsLocal.clear();
    setSavedIds([]);
    setJobs([]);
  };

  return (
    <MainLayout title="Việc đã lưu" breadcrumb="Trang chủ / Ứng viên / Việc đã lưu">
      <div className={s.stack}>
        <div className={s.topBar}>
          <div className={s.tags}>
            <span className={s.tag}>Đã lưu: {savedIds.length} tin</span>
            <span className={s.tag}>Lưu cục bộ do backend chưa có API Saved Jobs</span>
          </div>
          <div className={s.actions}>
            <button type="button" className={`${s.btn} ${s.btnGhost}`} onClick={() => void loadSavedJobs()}>
              Làm mới
            </button>
            <button
              type="button"
              className={`${s.btn} ${s.btnDanger}`}
              disabled={savedIds.length === 0}
              onClick={handleClear}
            >
              Xóa tất cả
            </button>
          </div>
        </div>

        {loading ? <div className={s.alert}>Đang tải việc đã lưu...</div> : null}
        {error ? <div className={`${s.alert} ${s.alertError}`}>{error}</div> : null}

        {jobs.length === 0 && !loading ? (
          <div className={s.alert}>
            Chưa có tin nào được lưu. Bạn có thể lưu từ trang{' '}
            <Link className={s.inlineLink} to={ROUTES.public.jobs}>Việc làm</Link>.
          </div>
        ) : null}

        <div className={s.jobList}>
          {jobs.map((job) => (
            <article key={job.id} className={s.jobCard}>
              <div className={s.jobHead}>
                <div>
                  <h3 className={s.jobTitle}>
                    <Link className={s.inlineLink} to={toJobDetailPath(job.id)}>
                      {job.tieuDe}
                    </Link>
                  </h3>
                  <p className={s.meta}>{job.tenCongTy} · {job.diaDiem || 'Nhiều khu vực'}</p>
                </div>
                <button type="button" className={`${s.btn} ${s.btnGhost}`} onClick={() => handleRemove(job.id)}>
                  Bỏ lưu
                </button>
              </div>

              <div className={s.tags}>
                <span className={s.tag}>Lương: {formatSalary(job.mucLuongMin, job.mucLuongMax)}</span>
                <span className={s.tag}>Cấp bậc: {job.capBacYeuCau || 'Không yêu cầu'}</span>
                <span className={s.tag}>Hình thức: {job.hinhThucLamViec || 'Linh hoạt'}</span>
              </div>
            </article>
          ))}
        </div>
      </div>
    </MainLayout>
  );
}
