import { useCallback, useEffect, useMemo, useState } from 'react';
import MainLayout from '@/layouts/MainLayout';
import AppDataTable, { type AppDataColumn } from '@/components/common/AppDataTable';
import { applicationService } from '@/services/modules/application.module';
import { jobService } from '@/services/modules/job.module';
import type { PageResponse } from '@/types/api.types';
import type {
  ApplicationItem,
  ApplicationStatusHistoryItem,
  InterviewItem,
  InterviewRescheduleRequest,
  InterviewUpsertRequest,
  TrangThaiDon,
  UpdateApplicationStatusRequest,
} from '@/types/application.types';
import type { JobPosting } from '@/types/job.types';
import s from '@/assets/styles/recruiter-workflow.module.css';

type ViewMode = 'applicants' | 'profiles';

type StatusFormState = {
  trangThaiMoi: string;
  ghiChu: string;
};

type InterviewFormState = {
  tieuDeVong: string;
  thoiGianBatDau: string;
  thoiGianKetThuc: string;
  hinhThuc: 'ONLINE' | 'OFFLINE';
  diaDiemHoacLink: string;
};

const DEFAULT_STATUS_FORM: StatusFormState = {
  trangThaiMoi: '2',
  ghiChu: '',
};

const DEFAULT_INTERVIEW_FORM: InterviewFormState = {
  tieuDeVong: '',
  thoiGianBatDau: '',
  thoiGianKetThuc: '',
  hinhThuc: 'ONLINE',
  diaDiemHoacLink: '',
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
  return date.toLocaleString('vi-VN');
}

function toDateTimeInput(value?: string): string {
  if (!value) return '';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value.length >= 16 ? value.slice(0, 16) : value;
  }
  const yyyy = date.getFullYear();
  const mm = String(date.getMonth() + 1).padStart(2, '0');
  const dd = String(date.getDate()).padStart(2, '0');
  const hh = String(date.getHours()).padStart(2, '0');
  const mi = String(date.getMinutes()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}T${hh}:${mi}`;
}

export function RecruiterApplicantsPage({ viewMode = 'applicants' }: { viewMode?: ViewMode }) {
  const [jobs, setJobs] = useState<JobPosting[]>([]);
  const [selectedJobId, setSelectedJobId] = useState<number | null>(null);
  const [page, setPage] = useState(0);
  const [pageData, setPageData] = useState<PageResponse<ApplicationItem>>({
    content: [],
    totalElements: 0,
    totalPages: 0,
    number: 0,
    size: 10,
  });

  const [selectedApplicationId, setSelectedApplicationId] = useState<number | null>(null);
  const [selectedApplication, setSelectedApplication] = useState<ApplicationItem | null>(null);
  const [statusHistory, setStatusHistory] = useState<ApplicationStatusHistoryItem[]>([]);
  const [cvSnapshotUrl, setCvSnapshotUrl] = useState('');

  const [interviews, setInterviews] = useState<InterviewItem[]>([]);
  const [selectedInterviewId, setSelectedInterviewId] = useState<number | null>(null);

  const [statusForm, setStatusForm] = useState<StatusFormState>(DEFAULT_STATUS_FORM);
  const [interviewForm, setInterviewForm] = useState<InterviewFormState>(DEFAULT_INTERVIEW_FORM);

  const [loading, setLoading] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [detailError, setDetailError] = useState('');
  const [message, setMessage] = useState('');

  const isProfileMode = useMemo(() => viewMode === 'profiles', [viewMode]);

  const fetchJobs = useCallback(async () => {
    try {
      const data = await jobService.getMyJobs();
      setJobs(data);
      if (data.length === 0) {
        setSelectedJobId(null);
      } else if (!selectedJobId || !data.some((job) => job.id === selectedJobId)) {
        setSelectedJobId(data[0].id);
      }
    } catch (err) {
      setError(mapError(err, 'Không thể tải danh sách tin tuyển dụng của bạn.'));
    }
  }, [selectedJobId]);

  useEffect(() => {
    void fetchJobs();
  }, [fetchJobs]);

  const fetchApplications = useCallback(async () => {
    if (!selectedJobId) {
      setPageData({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 10 });
      return;
    }

    setLoading(true);
    setError('');
    try {
      const data = await applicationService.getRecruiterApplications(selectedJobId, page, 10);
      setPageData(data);
      if (!selectedApplicationId || !data.content.some((app) => app.id === selectedApplicationId)) {
        setSelectedApplicationId(data.content[0]?.id ?? null);
      }
    } catch (err) {
      setError(mapError(err, 'Không thể tải danh sách ứng viên nộp đơn.'));
    } finally {
      setLoading(false);
    }
  }, [selectedApplicationId, selectedJobId, page]);

  useEffect(() => {
    void fetchApplications();
  }, [fetchApplications]);

  const fetchApplicationBundle = useCallback(async (applicationId: number) => {
    setDetailLoading(true);
    setDetailError('');

    try {
      const [detailData, historyData, interviewData] = await Promise.all([
        applicationService.getApplicationDetail(applicationId),
        applicationService.getApplicationHistory(applicationId, 0, 10),
        applicationService.getInterviewsByApplication(applicationId),
      ]);

      setSelectedApplication(detailData);
      setStatusHistory(historyData.content);
      setInterviews(interviewData);

      try {
        const snapshot = await applicationService.getCvSnapshotUrl(applicationId);
        setCvSnapshotUrl(snapshot || '');
      } catch {
        setCvSnapshotUrl('');
      }
    } catch (err) {
      setDetailError(mapError(err, 'Không thể tải chi tiết hồ sơ ứng tuyển.'));
      setSelectedApplication(null);
      setStatusHistory([]);
      setInterviews([]);
      setCvSnapshotUrl('');
    } finally {
      setDetailLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!selectedApplicationId) {
      setSelectedApplication(null);
      setStatusHistory([]);
      setInterviews([]);
      setCvSnapshotUrl('');
      return;
    }
    void fetchApplicationBundle(selectedApplicationId);
  }, [selectedApplicationId, fetchApplicationBundle]);

  const handleChangeJob = (jobIdRaw: string) => {
    const jobId = Number(jobIdRaw);
    setSelectedJobId(Number.isInteger(jobId) ? jobId : null);
    setSelectedApplicationId(null);
    setSelectedInterviewId(null);
    setPage(0);
  };

  const handlePickInterview = (item: InterviewItem) => {
    setSelectedInterviewId(item.id);
    setInterviewForm({
      tieuDeVong: item.tieuDeVong,
      thoiGianBatDau: toDateTimeInput(item.thoiGianBatDau),
      thoiGianKetThuc: toDateTimeInput(item.thoiGianKetThuc),
      hinhThuc: item.hinhThuc,
      diaDiemHoacLink: item.diaDiemHoacLink || '',
    });
  };

  const resetInterviewForm = () => {
    setSelectedInterviewId(null);
    setInterviewForm(DEFAULT_INTERVIEW_FORM);
  };

  const handleUpdateStatus = async () => {
    if (!selectedApplicationId) {
      setError('Hãy chọn một đơn ứng tuyển trước.');
      return;
    }

    setSaving(true);
    setMessage('');
    setError('');

    try {
      const payload: UpdateApplicationStatusRequest = {
        trangThaiMoi: Number(statusForm.trangThaiMoi) as TrangThaiDon,
        ghiChu: statusForm.ghiChu.trim() || undefined,
      };
      await applicationService.updateApplicationStatus(selectedApplicationId, payload);
      setMessage('Cập nhật trạng thái đơn thành công.');
      await fetchApplications();
      await fetchApplicationBundle(selectedApplicationId);
    } catch (err) {
      setError(mapError(err, 'Không thể cập nhật trạng thái đơn.'));
    } finally {
      setSaving(false);
    }
  };

  const handleReject = async () => {
    if (!selectedApplicationId) {
      setError('Hãy chọn một đơn ứng tuyển trước.');
      return;
    }

    setSaving(true);
    setMessage('');
    setError('');

    try {
      await applicationService.rejectApplication(selectedApplicationId, statusForm.ghiChu.trim() || undefined);
      setMessage('Từ chối đơn thành công.');
      await fetchApplications();
      await fetchApplicationBundle(selectedApplicationId);
    } catch (err) {
      setError(mapError(err, 'Không thể từ chối đơn ứng tuyển.'));
    } finally {
      setSaving(false);
    }
  };

  const buildInterviewPayload = (): InterviewUpsertRequest | null => {
    if (!selectedApplicationId) return null;
    if (!interviewForm.tieuDeVong.trim()) return null;
    if (!interviewForm.thoiGianBatDau || !interviewForm.thoiGianKetThuc) return null;

    return {
      donUngTuyenId: selectedApplicationId,
      tieuDeVong: interviewForm.tieuDeVong.trim(),
      thoiGianBatDau: interviewForm.thoiGianBatDau,
      thoiGianKetThuc: interviewForm.thoiGianKetThuc,
      hinhThuc: interviewForm.hinhThuc,
      diaDiemHoacLink: interviewForm.diaDiemHoacLink.trim() || undefined,
    };
  };

  const handleCreateInterview = async () => {
    const payload = buildInterviewPayload();
    if (!payload) {
      setError('Vui lòng điền đầy đủ thông tin lịch phỏng vấn hợp lệ.');
      return;
    }

    setSaving(true);
    setMessage('');
    setError('');

    try {
      await applicationService.createInterview(payload);
      setMessage('Tạo lịch phỏng vấn thành công.');
      resetInterviewForm();
      if (selectedApplicationId) {
        await fetchApplicationBundle(selectedApplicationId);
      }
    } catch (err) {
      setError(mapError(err, 'Không thể tạo lịch phỏng vấn.'));
    } finally {
      setSaving(false);
    }
  };

  const handleUpdateInterview = async () => {
    if (!selectedInterviewId) {
      setError('Hãy chọn một lịch phỏng vấn để cập nhật.');
      return;
    }

    const payload = buildInterviewPayload();
    if (!payload) {
      setError('Vui lòng điền đầy đủ thông tin lịch phỏng vấn hợp lệ.');
      return;
    }

    setSaving(true);
    setMessage('');
    setError('');

    try {
      await applicationService.updateInterview(selectedInterviewId, payload);
      setMessage('Cập nhật lịch phỏng vấn thành công.');
      if (selectedApplicationId) {
        await fetchApplicationBundle(selectedApplicationId);
      }
    } catch (err) {
      setError(mapError(err, 'Không thể cập nhật lịch phỏng vấn.'));
    } finally {
      setSaving(false);
    }
  };

  const handleRescheduleInterview = async () => {
    if (!selectedInterviewId) {
      setError('Hãy chọn một lịch phỏng vấn để dời lịch.');
      return;
    }
    if (!interviewForm.thoiGianBatDau || !interviewForm.thoiGianKetThuc) {
      setError('Vui lòng nhập thời gian bắt đầu và kết thúc mới.');
      return;
    }

    setSaving(true);
    setMessage('');
    setError('');

    try {
      const payload: InterviewRescheduleRequest = {
        thoiGianBatDau: interviewForm.thoiGianBatDau,
        thoiGianKetThuc: interviewForm.thoiGianKetThuc,
        diaDiemHoacLink: interviewForm.diaDiemHoacLink.trim() || undefined,
      };
      await applicationService.rescheduleInterview(selectedInterviewId, payload);
      setMessage('Dời lịch phỏng vấn thành công.');
      if (selectedApplicationId) {
        await fetchApplicationBundle(selectedApplicationId);
      }
    } catch (err) {
      setError(mapError(err, 'Không thể dời lịch phỏng vấn.'));
    } finally {
      setSaving(false);
    }
  };

  const handleCancelInterview = async () => {
    if (!selectedInterviewId) {
      setError('Hãy chọn một lịch phỏng vấn để hủy.');
      return;
    }

    const confirmed = window.confirm('Bạn có chắc muốn hủy lịch phỏng vấn này?');
    if (!confirmed) return;

    setSaving(true);
    setMessage('');
    setError('');

    try {
      await applicationService.cancelInterview(selectedInterviewId);
      setMessage('Hủy lịch phỏng vấn thành công.');
      resetInterviewForm();
      if (selectedApplicationId) {
        await fetchApplicationBundle(selectedApplicationId);
      }
    } catch (err) {
      setError(mapError(err, 'Không thể hủy lịch phỏng vấn.'));
    } finally {
      setSaving(false);
    }
  };

  const pageTitle = isProfileMode ? 'Hồ sơ ứng viên' : 'Ứng viên nộp';
  const breadcrumb = isProfileMode
    ? 'Trang chủ / Nhà tuyển dụng / Hồ sơ ứng viên'
    : 'Trang chủ / Nhà tuyển dụng / Ứng viên nộp';

  const columns: AppDataColumn<ApplicationItem>[] = [
    {
      key: 'tenUngVien',
      header: 'Ứng viên',
      render: (row) => (
        <div style={{ display: 'grid', gap: 4 }}>
          <strong>{row.tenUngVien}</strong>
          <span className={s.meta}>{row.emailUngVien}</span>
        </div>
      ),
    },
    {
      key: 'trangThai',
      header: 'Trạng thái',
      width: '170px',
      render: (row) => (
        <span className={`${s.statusPill} ${s[`status${row.trangThai}` as keyof typeof s]}`}>
          {row.trangThaiLabel}
        </span>
      ),
    },
    {
      key: 'ngayNop',
      header: 'Ngày nộp',
      width: '170px',
      render: (row) => formatDate(row.ngayNop),
    },
    {
      key: 'cvUrl',
      header: 'CV',
      width: '120px',
      align: 'center',
      render: (row) =>
        row.cvUrl ? (
          <a className={s.inlineLink} href={row.cvUrl} target="_blank" rel="noreferrer">
            Mở CV
          </a>
        ) : (
          '-'
        ),
    },
    {
      key: 'actions',
      header: 'Tác vụ',
      width: '120px',
      align: 'center',
      render: (row) => (
        <button
          type="button"
          className={`${s.btn} ${s.btnGhost}`}
          onClick={() => setSelectedApplicationId(row.id)}
        >
          Xem
        </button>
      ),
    },
  ];

  return (
    <MainLayout title={pageTitle} breadcrumb={breadcrumb}>
      <div className={s.stack}>
        <div className={s.topBar}>
          <div className={s.tags}>
            <span className={s.tag}>Tổng đơn: {pageData.totalElements}</span>
            <span className={s.tag}>Tin đang chọn: {selectedJobId ?? 'Chưa chọn'}</span>
          </div>
          <div className={s.actions}>
            <div className={s.field} style={{ minWidth: 260 }}>
              <label className={s.label}>Chọn tin tuyển dụng</label>
              <select
                className={s.select}
                value={selectedJobId ?? ''}
                onChange={(e) => handleChangeJob(e.target.value)}
              >
                <option value="">-- Chọn tin --</option>
                {jobs.map((job) => (
                  <option key={job.id} value={job.id}>{job.tieuDe}</option>
                ))}
              </select>
            </div>
          </div>
        </div>

        {loading ? <div className={s.alert}>Đang tải danh sách ứng viên...</div> : null}
        {error ? <div className={`${s.alert} ${s.alertError}`}>{error}</div> : null}
        {message ? <div className={`${s.alert} ${s.alertSuccess}`}>{message}</div> : null}

        <section className={s.card}>
          <h3 className={s.cardTitle}>Danh sách đơn ứng tuyển theo tin</h3>
          <AppDataTable
            columns={columns}
            data={pageData.content}
            rowKey={(row) => String(row.id)}
            emptyMessage="Chưa có đơn ứng tuyển cho tin này."
          />
          <div className={s.actions}>
            <button
              type="button"
              className={`${s.btn} ${s.btnGhost}`}
              disabled={page === 0}
              onClick={() => setPage((prev) => Math.max(0, prev - 1))}
            >
              ← Trang trước
            </button>
            <button
              type="button"
              className={`${s.btn} ${s.btnGhost}`}
              disabled={page >= Math.max(pageData.totalPages - 1, 0)}
              onClick={() => setPage((prev) => prev + 1)}
            >
              Trang sau →
            </button>
          </div>
        </section>

        <div className={s.grid2}>
          <section className={s.card}>
            <h3 className={s.cardTitle}>Chi tiết ứng viên / đơn</h3>
            {detailLoading ? <div className={s.alert}>Đang tải chi tiết...</div> : null}
            {detailError ? <div className={`${s.alert} ${s.alertError}`}>{detailError}</div> : null}
            {!detailLoading && !detailError && selectedApplication ? (
              <>
                <div className={s.tags}>
                  <span className={`${s.statusPill} ${s[`status${selectedApplication.trangThai}` as keyof typeof s]}`}>
                    {selectedApplication.trangThaiLabel}
                  </span>
                  <span className={s.tag}>{selectedApplication.tenUngVien}</span>
                  <span className={s.tag}>{selectedApplication.tenCongTy}</span>
                </div>
                <div className={s.field}>
                  <label className={s.label}>Thư ngỏ</label>
                  <div className={s.alert}>{selectedApplication.thuNgo || 'Không có thư ngỏ'}</div>
                </div>
                <div className={s.field}>
                  <label className={s.label}>CV snapshot (D7)</label>
                  {cvSnapshotUrl ? (
                    <a className={s.inlineLink} href={cvSnapshotUrl} target="_blank" rel="noreferrer">
                      Mở ảnh snapshot CV
                    </a>
                  ) : (
                    <span className={s.meta}>Không có snapshot hoặc không đủ quyền truy cập.</span>
                  )}
                </div>

                {!isProfileMode ? (
                  <>
                    <div className={s.field}>
                      <label className={s.label}>Cập nhật trạng thái đơn</label>
                      <select
                        className={s.select}
                        value={statusForm.trangThaiMoi}
                        onChange={(e) => setStatusForm((prev) => ({ ...prev, trangThaiMoi: e.target.value }))}
                      >
                        <option value="2">Đang xem xét</option>
                        <option value="3">Phỏng vấn</option>
                        <option value="4">Đã offer</option>
                        <option value="5">Từ chối</option>
                      </select>
                    </div>
                    <div className={s.field}>
                      <label className={s.label}>Ghi chú</label>
                      <textarea
                        className={s.textarea}
                        value={statusForm.ghiChu}
                        onChange={(e) => setStatusForm((prev) => ({ ...prev, ghiChu: e.target.value }))}
                        placeholder="Ghi chú nội bộ hoặc phản hồi cho ứng viên"
                      />
                    </div>
                    <div className={s.actions}>
                      <button
                        type="button"
                        className={`${s.btn} ${s.btnPrimary}`}
                        disabled={saving}
                        onClick={() => void handleUpdateStatus()}
                      >
                        Cập nhật trạng thái
                      </button>
                      <button
                        type="button"
                        className={`${s.btn} ${s.btnDanger}`}
                        disabled={saving}
                        onClick={() => void handleReject()}
                      >
                        Từ chối đơn
                      </button>
                    </div>
                  </>
                ) : null}
              </>
            ) : null}
            {!detailLoading && !detailError && !selectedApplication ? (
              <div className={s.alert}>Chọn một đơn để xem chi tiết ứng viên.</div>
            ) : null}
          </section>

          <section className={s.card}>
            <h3 className={s.cardTitle}>Lịch phỏng vấn (D9-D14)</h3>
            <AppDataTable
              columns={[
                {
                  key: 'tieuDeVong',
                  header: 'Vòng',
                  render: (row: InterviewItem) => (
                    <div style={{ display: 'grid', gap: 4 }}>
                      <strong>{row.tieuDeVong}</strong>
                      <span className={s.meta}>{row.hinhThuc} · {row.trangThai}</span>
                    </div>
                  ),
                },
                {
                  key: 'thoiGianBatDau',
                  header: 'Bắt đầu',
                  width: '170px',
                  render: (row: InterviewItem) => formatDate(row.thoiGianBatDau),
                },
                {
                  key: 'actions',
                  header: 'Tác vụ',
                  width: '110px',
                  align: 'center',
                  render: (row: InterviewItem) => (
                    <button
                      type="button"
                      className={`${s.btn} ${s.btnGhost}`}
                      onClick={() => handlePickInterview(row)}
                    >
                      Chọn
                    </button>
                  ),
                },
              ]}
              data={interviews}
              rowKey={(row) => String(row.id)}
              emptyMessage="Chưa có lịch phỏng vấn cho đơn này."
            />

            {!isProfileMode ? (
              <>
                <div className={s.grid2}>
                  <div className={s.field}>
                    <label className={s.label}>Tiêu đề vòng</label>
                    <input
                      className={s.input}
                      value={interviewForm.tieuDeVong}
                      onChange={(e) => setInterviewForm((prev) => ({ ...prev, tieuDeVong: e.target.value }))}
                      placeholder="Ví dụ: Phỏng vấn kỹ thuật"
                    />
                  </div>
                  <div className={s.field}>
                    <label className={s.label}>Hình thức</label>
                    <select
                      className={s.select}
                      value={interviewForm.hinhThuc}
                      onChange={(e) => setInterviewForm((prev) => ({ ...prev, hinhThuc: e.target.value as InterviewFormState['hinhThuc'] }))}
                    >
                      <option value="ONLINE">Online</option>
                      <option value="OFFLINE">Offline</option>
                    </select>
                  </div>
                </div>

                <div className={s.grid2}>
                  <div className={s.field}>
                    <label className={s.label}>Thời gian bắt đầu</label>
                    <input
                      className={s.input}
                      type="datetime-local"
                      value={interviewForm.thoiGianBatDau}
                      onChange={(e) => setInterviewForm((prev) => ({ ...prev, thoiGianBatDau: e.target.value }))}
                    />
                  </div>
                  <div className={s.field}>
                    <label className={s.label}>Thời gian kết thúc</label>
                    <input
                      className={s.input}
                      type="datetime-local"
                      value={interviewForm.thoiGianKetThuc}
                      onChange={(e) => setInterviewForm((prev) => ({ ...prev, thoiGianKetThuc: e.target.value }))}
                    />
                  </div>
                </div>

                <div className={s.field}>
                  <label className={s.label}>Địa điểm / Link</label>
                  <input
                    className={s.input}
                    value={interviewForm.diaDiemHoacLink}
                    onChange={(e) => setInterviewForm((prev) => ({ ...prev, diaDiemHoacLink: e.target.value }))}
                    placeholder="Phòng họp 3 hoặc https://..."
                  />
                </div>

                <div className={s.actions}>
                  <button type="button" className={`${s.btn} ${s.btnGhost}`} disabled={saving} onClick={resetInterviewForm}>
                    Làm mới form
                  </button>
                  <button
                    type="button"
                    className={`${s.btn} ${s.btnPrimary}`}
                    disabled={saving}
                    onClick={() => void handleCreateInterview()}
                  >
                    Tạo lịch
                  </button>
                  <button
                    type="button"
                    className={`${s.btn} ${s.btnGhost}`}
                    disabled={saving || selectedInterviewId === null}
                    onClick={() => void handleUpdateInterview()}
                  >
                    Cập nhật lịch
                  </button>
                  <button
                    type="button"
                    className={`${s.btn} ${s.btnGhost}`}
                    disabled={saving || selectedInterviewId === null}
                    onClick={() => void handleRescheduleInterview()}
                  >
                    Dời lịch
                  </button>
                  <button
                    type="button"
                    className={`${s.btn} ${s.btnDanger}`}
                    disabled={saving || selectedInterviewId === null}
                    onClick={() => void handleCancelInterview()}
                  >
                    Hủy lịch
                  </button>
                </div>
              </>
            ) : null}
          </section>
        </div>

        <section className={s.card}>
          <h3 className={s.cardTitle}>Lịch sử thay đổi trạng thái</h3>
          <AppDataTable
            columns={[
              {
                key: 'thoiGianChuyen',
                header: 'Thời gian',
                width: '180px',
                render: (row: ApplicationStatusHistoryItem) => formatDate(row.thoiGianChuyen),
              },
              {
                key: 'trangThaiMoiLabel',
                header: 'Trạng thái mới',
                width: '170px',
                render: (row: ApplicationStatusHistoryItem) => row.trangThaiMoiLabel,
              },
              {
                key: 'nguoiThucHien',
                header: 'Người thực hiện',
                width: '190px',
                render: (row: ApplicationStatusHistoryItem) => `${row.nguoiThucHien} (${row.vaiTro})`,
              },
              {
                key: 'ghiChu',
                header: 'Ghi chú',
                render: (row: ApplicationStatusHistoryItem) => row.ghiChu || '-',
              },
            ]}
            data={statusHistory}
            rowKey={(row) => String(row.id)}
            emptyMessage="Chưa có lịch sử thay đổi cho đơn ứng tuyển này."
          />
        </section>
      </div>
    </MainLayout>
  );
}

export default function RecruiterApplicantsRoutePage() {
  return <RecruiterApplicantsPage viewMode="applicants" />;
}
