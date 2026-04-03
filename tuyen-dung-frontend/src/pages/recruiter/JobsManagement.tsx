import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import MainLayout from '@/layouts/MainLayout';
import AppDataTable, { type AppDataColumn } from '@/components/common/AppDataTable';
import { ROUTES } from '@/constants/routes';
import { jobService } from '@/services/modules/job.module';
import { lookupService } from '@/services/modules/lookup.module';
import type { JobPosting, JobStatistics, JobUpsertRequest, KhuVuc } from '@/types/job.types';
import type { LookupItem } from '@/types/lookup.types';
import s from '@/assets/styles/recruiter-workflow.module.css';

type JobFormState = {
  tieuDe: string;
  moTaCongViec: string;
  yeuCauUngVien: string;
  mucLuongMin: string;
  mucLuongMax: string;
  diaDiem: string;
  capBacYeuCau: JobUpsertRequest['capBacYeuCau'];
  hinhThucLamViec: JobUpsertRequest['hinhThucLamViec'];
  hanNop: string;
};

const DEFAULT_FORM: JobFormState = {
  tieuDe: '',
  moTaCongViec: '',
  yeuCauUngVien: '',
  mucLuongMin: '',
  mucLuongMax: '',
  diaDiem: '',
  capBacYeuCau: 'JUNIOR',
  hinhThucLamViec: 'OFFICE',
  hanNop: '',
};

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

function buildPayload(form: JobFormState): JobUpsertRequest | null {
  if (!form.tieuDe.trim()) return null;
  if (!form.moTaCongViec.trim()) return null;
  if (!form.yeuCauUngVien.trim()) return null;
  if (!form.hanNop) return null;

  const min = form.mucLuongMin ? Number(form.mucLuongMin) : undefined;
  const max = form.mucLuongMax ? Number(form.mucLuongMax) : undefined;

  if ((min != null && Number.isNaN(min)) || (max != null && Number.isNaN(max))) {
    return null;
  }
  if (min != null && max != null && min > max) {
    return null;
  }

  return {
    tieuDe: form.tieuDe.trim(),
    moTaCongViec: form.moTaCongViec.trim(),
    yeuCauUngVien: form.yeuCauUngVien.trim(),
    mucLuongMin: min,
    mucLuongMax: max,
    diaDiem: form.diaDiem.trim() || undefined,
    capBacYeuCau: form.capBacYeuCau,
    hinhThucLamViec: form.hinhThucLamViec,
    hanNop: form.hanNop,
  };
}

function toFormState(job: JobPosting): JobFormState {
  return {
    tieuDe: job.tieuDe,
    moTaCongViec: job.moTaCongViec,
    yeuCauUngVien: job.yeuCauUngVien,
    mucLuongMin: job.mucLuongMin != null ? String(job.mucLuongMin) : '',
    mucLuongMax: job.mucLuongMax != null ? String(job.mucLuongMax) : '',
    diaDiem: job.diaDiem || '',
    capBacYeuCau: job.capBacYeuCau || 'JUNIOR',
    hinhThucLamViec: job.hinhThucLamViec || 'OFFICE',
    hanNop: job.hanNop || '',
  };
}

export default function RecruiterJobsManagementPage() {
  const [jobs, setJobs] = useState<JobPosting[]>([]);
  const [locationOptions, setLocationOptions] = useState<LookupItem[]>([]);
  const [selectedLocations, setSelectedLocations] = useState<string[]>([]);
  const [selectedJobId, setSelectedJobId] = useState<number | null>(null);
  const [editingJobId, setEditingJobId] = useState<number | null>(null);
  const [statistics, setStatistics] = useState<JobStatistics | null>(null);
  const [form, setForm] = useState<JobFormState>(DEFAULT_FORM);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [statsLoading, setStatsLoading] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const fetchJobs = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await jobService.getMyJobs();
      setJobs(data);
      if (data.length === 0) {
        setSelectedJobId(null);
      } else if (!selectedJobId || !data.some((job) => job.id === selectedJobId)) {
        setSelectedJobId(data[0].id);
      }
    } catch (err) {
      setError(mapError(err, 'Không thể tải danh sách tin tuyển dụng.'));
    } finally {
      setLoading(false);
    }
  }, [selectedJobId]);

  const fetchLocationCatalog = useCallback(async () => {
    try {
      const locations = await lookupService.getLocations();
      setLocationOptions(locations);
    } catch {
      setLocationOptions([]);
    }
  }, []);

  useEffect(() => {
    void fetchJobs();
    void fetchLocationCatalog();
  }, [fetchJobs, fetchLocationCatalog]);

  const fetchSelectedJobExtras = useCallback(async (jobId: number) => {
    setStatsLoading(true);
    try {
      const [stats, locations] = await Promise.all([
        jobService.getJobStatistics(jobId),
        jobService.getJobLocations(jobId),
      ]);
      setStatistics(stats);
      setSelectedLocations(locations);
    } catch {
      setStatistics(null);
      setSelectedLocations([]);
    } finally {
      setStatsLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!selectedJobId) {
      setStatistics(null);
      setSelectedLocations([]);
      return;
    }
    void fetchSelectedJobExtras(selectedJobId);
  }, [selectedJobId, fetchSelectedJobExtras]);

  const handlePickJob = (job: JobPosting) => {
    setSelectedJobId(job.id);
    setEditingJobId(job.id);
    setForm(toFormState(job));
    setMessage('');
    setError('');
  };

  const handleResetForm = () => {
    setEditingJobId(null);
    setForm(DEFAULT_FORM);
    setSelectedLocations([]);
    setMessage('');
    setError('');
  };

  const handleToggleLocation = (value: string) => {
    setSelectedLocations((prev) =>
      prev.includes(value) ? prev.filter((item) => item !== value) : [...prev, value]
    );
  };

  const handleSubmit = async () => {
    const payload = buildPayload(form);
    if (!payload) {
      setError('Vui lòng điền đầy đủ thông tin hợp lệ trước khi lưu tin tuyển dụng.');
      return;
    }

    setSaving(true);
    setMessage('');
    setError('');

    try {
      if (editingJobId) {
        await jobService.updateJob(editingJobId, payload);
        await jobService.updateJobLocations(editingJobId, selectedLocations as KhuVuc[]);
        setMessage('Cập nhật tin tuyển dụng thành công.');
        setSelectedJobId(editingJobId);
      } else {
        const created = await jobService.createJob(payload);
        if (selectedLocations.length > 0) {
          await jobService.updateJobLocations(created.id, selectedLocations as KhuVuc[]);
        }
        setMessage('Đăng tin tuyển dụng thành công.');
        setSelectedJobId(created.id);
        setEditingJobId(created.id);
      }
      await fetchJobs();
    } catch (err) {
      setError(mapError(err, 'Không thể lưu tin tuyển dụng.'));
    } finally {
      setSaving(false);
    }
  };

  const handleCloseJob = async () => {
    if (!selectedJobId) {
      setError('Hãy chọn một tin để đóng.');
      return;
    }

    const confirmed = window.confirm('Bạn có chắc muốn đóng tin tuyển dụng này?');
    if (!confirmed) return;

    setSaving(true);
    setMessage('');
    setError('');
    try {
      await jobService.closeJob(selectedJobId);
      setMessage('Đóng tin tuyển dụng thành công.');
      await fetchJobs();
      setEditingJobId(null);
      setForm(DEFAULT_FORM);
    } catch (err) {
      setError(mapError(err, 'Không thể đóng tin tuyển dụng.'));
    } finally {
      setSaving(false);
    }
  };

  const selectedJobTag = useMemo(() => {
    const selected = jobs.find((job) => job.id === selectedJobId);
    return selected ? `${selected.tieuDe} (${selected.tenCongTy})` : '';
  }, [jobs, selectedJobId]);

  const columns: AppDataColumn<JobPosting>[] = [
    {
      key: 'tieuDe',
      header: 'Tin tuyển dụng',
      render: (row) => (
        <div style={{ display: 'grid', gap: 4 }}>
          <strong>{row.tieuDe}</strong>
          <span className={s.meta}>{row.tenCongTy}</span>
        </div>
      ),
    },
    {
      key: 'trangThaiLabel',
      header: 'Trạng thái',
      width: '150px',
      render: (row) => row.trangThaiLabel || 'Không rõ',
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
      align: 'center',
      width: '120px',
      render: (row) => (
        <button type="button" className={`${s.btn} ${s.btnGhost}`} onClick={() => handlePickJob(row)}>
          Chọn
        </button>
      ),
    },
  ];

  return (
    <MainLayout title="Tin tuyển dụng" breadcrumb="Trang chủ / Nhà tuyển dụng / Tin tuyển dụng">
      <div className={s.stack}>
        <div className={s.topBar}>
          <div className={s.tags}>
            <span className={s.tag}>Tổng tin: {jobs.length}</span>
            {selectedJobTag ? <span className={s.tag}>Đang chọn: {selectedJobTag}</span> : null}
          </div>
          <div className={s.actions}>
            <button type="button" className={`${s.btn} ${s.btnGhost}`} onClick={() => void fetchJobs()}>
              Làm mới
            </button>
            <Link className={s.inlineLink} to={ROUTES.recruiter.applicants}>
              Xem ứng viên nộp
            </Link>
          </div>
        </div>

        {loading ? <div className={s.alert}>Đang tải danh sách tin...</div> : null}
        {error ? <div className={`${s.alert} ${s.alertError}`}>{error}</div> : null}
        {message ? <div className={`${s.alert} ${s.alertSuccess}`}>{message}</div> : null}

        <section className={s.card}>
          <h3 className={s.cardTitle}>Danh sách tin của bạn</h3>
          <AppDataTable
            columns={columns}
            data={jobs}
            rowKey={(row) => String(row.id)}
            emptyMessage="Bạn chưa có tin tuyển dụng nào."
          />
        </section>

        <div className={s.grid2}>
          <section className={s.card}>
            <h3 className={s.cardTitle}>{editingJobId ? 'Chỉnh sửa tin tuyển dụng' : 'Đăng tin tuyển dụng mới'}</h3>

            <div className={s.field}>
              <label className={s.label}>Tiêu đề</label>
              <input
                className={s.input}
                value={form.tieuDe}
                onChange={(e) => setForm((prev) => ({ ...prev, tieuDe: e.target.value }))}
                placeholder="Ví dụ: Senior Frontend Developer"
              />
            </div>

            <div className={s.field}>
              <label className={s.label}>Mô tả công việc</label>
              <textarea
                className={s.textarea}
                value={form.moTaCongViec}
                onChange={(e) => setForm((prev) => ({ ...prev, moTaCongViec: e.target.value }))}
              />
            </div>

            <div className={s.field}>
              <label className={s.label}>Yêu cầu ứng viên</label>
              <textarea
                className={s.textarea}
                value={form.yeuCauUngVien}
                onChange={(e) => setForm((prev) => ({ ...prev, yeuCauUngVien: e.target.value }))}
              />
            </div>

            <div className={s.grid2}>
              <div className={s.field}>
                <label className={s.label}>Lương tối thiểu</label>
                <input
                  className={s.input}
                  type="number"
                  min={0}
                  value={form.mucLuongMin}
                  onChange={(e) => setForm((prev) => ({ ...prev, mucLuongMin: e.target.value }))}
                />
              </div>
              <div className={s.field}>
                <label className={s.label}>Lương tối đa</label>
                <input
                  className={s.input}
                  type="number"
                  min={0}
                  value={form.mucLuongMax}
                  onChange={(e) => setForm((prev) => ({ ...prev, mucLuongMax: e.target.value }))}
                />
              </div>
            </div>

            <div className={s.grid2}>
              <div className={s.field}>
                <label className={s.label}>Cấp bậc</label>
                <select
                  className={s.select}
                  value={form.capBacYeuCau}
                  onChange={(e) => setForm((prev) => ({ ...prev, capBacYeuCau: e.target.value as JobFormState['capBacYeuCau'] }))}
                >
                  <option value="FRESHER">Fresher</option>
                  <option value="JUNIOR">Junior</option>
                  <option value="SENIOR">Senior</option>
                  <option value="LEAD">Lead</option>
                </select>
              </div>
              <div className={s.field}>
                <label className={s.label}>Hình thức làm việc</label>
                <select
                  className={s.select}
                  value={form.hinhThucLamViec}
                  onChange={(e) => setForm((prev) => ({ ...prev, hinhThucLamViec: e.target.value as JobFormState['hinhThucLamViec'] }))}
                >
                  <option value="OFFICE">Office</option>
                  <option value="HYBRID">Hybrid</option>
                  <option value="ONLINE">Online</option>
                </select>
              </div>
            </div>

            <div className={s.grid2}>
              <div className={s.field}>
                <label className={s.label}>Địa điểm text</label>
                <input
                  className={s.input}
                  value={form.diaDiem}
                  onChange={(e) => setForm((prev) => ({ ...prev, diaDiem: e.target.value }))}
                  placeholder="Ví dụ: Hà Nội"
                />
              </div>
              <div className={s.field}>
                <label className={s.label}>Hạn nộp</label>
                <input
                  className={s.input}
                  type="date"
                  value={form.hanNop}
                  onChange={(e) => setForm((prev) => ({ ...prev, hanNop: e.target.value }))}
                />
              </div>
            </div>

            <div className={s.field}>
              <label className={s.label}>Khu vực áp dụng (B20)</label>
              <div className={s.checkGrid}>
                {locationOptions.map((item) => (
                  <label key={item.value} className={s.checkItem}>
                    <input
                      type="checkbox"
                      checked={selectedLocations.includes(item.value)}
                      onChange={() => handleToggleLocation(item.value)}
                    />
                    <span>{item.label}</span>
                  </label>
                ))}
              </div>
            </div>

            <div className={s.actions}>
              <button type="button" className={`${s.btn} ${s.btnGhost}`} disabled={saving} onClick={handleResetForm}>
                Làm mới form
              </button>
              <button type="button" className={`${s.btn} ${s.btnPrimary}`} disabled={saving} onClick={() => void handleSubmit()}>
                {editingJobId ? 'Lưu cập nhật' : 'Đăng tin'}
              </button>
              <button
                type="button"
                className={`${s.btn} ${s.btnDanger}`}
                disabled={saving || !selectedJobId}
                onClick={() => void handleCloseJob()}
              >
                Đóng tin đã chọn
              </button>
            </div>
          </section>

          <section className={s.card}>
            <h3 className={s.cardTitle}>Thống kê tin đang chọn (B22)</h3>
            {statsLoading ? <div className={s.alert}>Đang tải thống kê...</div> : null}
            {!statsLoading && !statistics ? (
              <div className={s.alert}>Chọn một tin để xem thống kê ứng tuyển.</div>
            ) : null}
            {statistics ? (
              <div className={s.tags}>
                <span className={s.tag}>Tổng đơn: {statistics.tongSoDon}</span>
                <span className={s.tag}>Mới: {statistics.soMoi}</span>
                <span className={s.tag}>Review: {statistics.soReview}</span>
                <span className={s.tag}>Phỏng vấn: {statistics.soPhongVan}</span>
                <span className={s.tag}>Offer: {statistics.soOffer}</span>
                <span className={s.tag}>Từ chối: {statistics.soTuChoi}</span>
              </div>
            ) : null}

            <div className={s.alert}>
              Luồng quản lý ứng viên chi tiết ở trang{' '}
              <Link className={s.inlineLink} to={ROUTES.recruiter.applicants}>Ứng viên nộp</Link>.
            </div>
          </section>
        </div>
      </div>
    </MainLayout>
  );
}
