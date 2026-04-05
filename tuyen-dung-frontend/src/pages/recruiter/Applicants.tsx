import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import MainLayout from '@/layouts/MainLayout';
import AppDataTable, { type AppDataColumn } from '@/components/common/AppDataTable';
import { useDraftHistory } from '@/hooks/useDraftHistory';
import { ROUTES } from '@/constants/routes';
import { applicationService } from '@/services/modules/application.module';
import { jobService } from '@/services/modules/job.module';
import type { PageResponse } from '@/types/api.types';
import type {
  ApplicationItem,
  ApplicationStatusHistoryItem,
  InterviewItem,
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

const STATUS_STEPS: Array<{ id: TrangThaiDon; label: string }> = [
  { id: 1, label: 'Mới nộp' },
  { id: 2, label: 'Xem xét' },
  { id: 3, label: 'Phỏng vấn' },
  { id: 4, label: 'Offer' },
  { id: 5, label: 'Từ chối' },
];

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

function validateInterviewDateRange(startRaw: string, endRaw: string): string | null {
  const start = new Date(startRaw);
  const end = new Date(endRaw);
  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) {
    return 'Thời gian phỏng vấn không hợp lệ.';
  }

  const now = Date.now();
  if (start.getTime() <= now || end.getTime() <= now) {
    return 'Thời gian phỏng vấn phải ở tương lai.';
  }

  if (end.getTime() <= start.getTime()) {
    return 'Thời gian kết thúc phải sau thời gian bắt đầu.';
  }

  return null;
}

export function RecruiterApplicantsPage({ viewMode = 'applicants' }: { viewMode?: ViewMode }) {
  const [searchParams, setSearchParams] = useSearchParams();
  const querySelectionHandledRef = useRef(false);
  const [jobs, setJobs] = useState<JobPosting[]>([]);
  const {
    value: selectedJobId,
    setValue: setSelectedJobId,
  } = useDraftHistory<number | null>({
    storageKey: 'draft.recruiter.applicants.selectedJobId',
    initialValue: null,
  });
  const [page, setPage] = useState(0);
  const [pageData, setPageData] = useState<PageResponse<ApplicationItem>>({
    content: [],
    totalElements: 0,
    totalPages: 0,
    number: 0,
    size: 10,
  });

  const {
    value: selectedApplicationId,
    setValue: setSelectedApplicationId,
  } = useDraftHistory<number | null>({
    storageKey: 'draft.recruiter.applicants.selectedApplicationId',
    initialValue: null,
  });
  const [selectedApplication, setSelectedApplication] = useState<ApplicationItem | null>(null);
  const [statusHistory, setStatusHistory] = useState<ApplicationStatusHistoryItem[]>([]);
  const [cvSnapshotUrl, setCvSnapshotUrl] = useState('');

  const [interviews, setInterviews] = useState<InterviewItem[]>([]);
  const [selectedInterviewId, setSelectedInterviewId] = useState<number | null>(null);

  const [statusForm, setStatusForm] = useState<StatusFormState>(DEFAULT_STATUS_FORM);
  const [interviewForm, setInterviewForm] = useState<InterviewFormState>(DEFAULT_INTERVIEW_FORM);
  const [showInterviewForm, setShowInterviewForm] = useState(true);

  const [loading, setLoading] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [detailError, setDetailError] = useState('');
  const [message, setMessage] = useState('');

  const isProfileMode = useMemo(() => viewMode === 'profiles', [viewMode]);
  const isInterviewStage = useMemo(
    () => selectedApplication?.trangThai === 3,
    [selectedApplication?.trangThai]
  );

  const querySelectedJobId = useMemo(() => {
    const raw = searchParams.get('selectedJobId') ?? searchParams.get('jobId');
    const parsed = Number(raw);
    return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
  }, [searchParams]);

  const querySelectedApplicationId = useMemo(() => {
    const raw = searchParams.get('selectedApplicationId') ?? searchParams.get('applicationId');
    const parsed = Number(raw);
    return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
  }, [searchParams]);

  const fetchJobs = useCallback(async () => {
    try {
      const data = await jobService.getMyJobs();
      setJobs(data);
      if (data.length === 0) {
        setSelectedJobId(null);
      } else {
        const hasCurrent = selectedJobId != null && data.some((job) => job.id === selectedJobId);
        if (!hasCurrent) {
          setSelectedJobId(data[0].id);
        }
      }
    } catch (err) {
      setError(mapError(err, 'Không thể tải danh sách tin tuyển dụng của bạn.'));
    }
  }, [selectedJobId, setSelectedJobId]);

  useEffect(() => {
    if (querySelectionHandledRef.current) {
      return;
    }

    const hasQuerySelection = querySelectedJobId != null || querySelectedApplicationId != null;
    if (!hasQuerySelection) {
      return;
    }

    querySelectionHandledRef.current = true;

    let isMounted = true;
    const applyQuerySelection = async () => {
      let targetJobId = querySelectedJobId ?? null;

      if (querySelectedApplicationId != null) {
        try {
          const applicationDetail = await applicationService.getApplicationDetail(querySelectedApplicationId);
          if (!isMounted) return;
          targetJobId = applicationDetail.tinTuyenDungId;
        } catch {
          // Fallback to querySelectedJobId when cannot resolve from application detail.
        }

        if (!isMounted) return;
        setSelectedApplicationId(querySelectedApplicationId);
      }

      if (!isMounted) return;
      if (targetJobId != null) {
        setSelectedJobId(targetJobId);
        setPage(0);
      }

      const next = new URLSearchParams(searchParams);
      next.delete('selectedJobId');
      next.delete('jobId');
      next.delete('selectedApplicationId');
      next.delete('applicationId');
      setSearchParams(next, { replace: true });
    };

    void applyQuerySelection();

    return () => {
      isMounted = false;
    };
  }, [
    querySelectedApplicationId,
    querySelectedJobId,
    searchParams,
    setPage,
    setSearchParams,
    setSelectedApplicationId,
    setSelectedJobId,
  ]);

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
      if (selectedApplicationId == null) {
        setSelectedApplicationId(data.content[0]?.id ?? null);
      }
    } catch (err) {
      setError(mapError(err, 'Không thể tải danh sách ứng viên nộp đơn.'));
    } finally {
      setLoading(false);
    }
  }, [selectedApplicationId, selectedJobId, page, setSelectedApplicationId]);

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
    const isValidJobId = jobIdRaw.trim() !== '' && Number.isInteger(jobId) && jobId > 0;
    setSelectedJobId(isValidJobId ? jobId : null);
    setSelectedApplicationId(null);
    setSelectedInterviewId(null);
    setPage(0);
  };

  const handlePickInterview = async (item: InterviewItem) => {
    if (!isInterviewStage) {
      setError('Chỉ có thể chỉnh lịch khi đơn đang ở trạng thái Phỏng vấn.');
      return;
    }

    setDetailError('');
    try {
      const detail = await applicationService.getInterviewById(item.id);
      setSelectedInterviewId(detail.id);
      setInterviewForm({
        tieuDeVong: detail.tieuDeVong,
        thoiGianBatDau: toDateTimeInput(detail.thoiGianBatDau),
        thoiGianKetThuc: toDateTimeInput(detail.thoiGianKetThuc),
        hinhThuc: detail.hinhThuc,
        diaDiemHoacLink: detail.diaDiemHoacLink || '',
      });
    } catch (err) {
      setDetailError(mapError(err, 'Không thể tải chi tiết lịch phỏng vấn.'));
    }
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

    const dateError = validateInterviewDateRange(
      interviewForm.thoiGianBatDau,
      interviewForm.thoiGianKetThuc
    );
    if (dateError) {
      setError(dateError);
      return null;
    }

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
    if (!isInterviewStage) {
      setError('Chỉ có thể tạo lịch khi đơn đang ở trạng thái Phỏng vấn.');
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
    if (!isInterviewStage) {
      setError('Chỉ có thể cập nhật lịch khi đơn đang ở trạng thái Phỏng vấn.');
      return;
    }

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

  const handleCancelInterview = async () => {
    if (!isInterviewStage) {
      setError('Chỉ có thể hủy lịch khi đơn đang ở trạng thái Phỏng vấn.');
      return;
    }

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

  const timelineInterviews = useMemo(
    () => interviews
      .filter((item) => item.trangThai !== 'HUY')
      .sort((a, b) => new Date(a.thoiGianBatDau).getTime() - new Date(b.thoiGianBatDau).getTime()),
    [interviews]
  );

  const isStatusStepActive = useCallback((stepId: TrangThaiDon): boolean => {
    if (!selectedApplication) return false;
    if (selectedApplication.trangThai === 5) {
      return stepId === 1 || stepId === 2 || stepId === 3 || stepId === 5;
    }
    return stepId <= selectedApplication.trangThai && stepId !== 5;
  }, [selectedApplication]);

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

        <div className={s.panelLayout}>
          <aside className={s.leftPanel}>
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
          </aside>

          <section className={s.middlePanel}>
            <section className={s.card}>
              <h3 className={s.cardTitle}>Chi tiết ứng viên / đơn</h3>
              {detailLoading ? <div className={s.alert}>Đang tải chi tiết...</div> : null}
              {detailError ? <div className={`${s.alert} ${s.alertError}`}>{detailError}</div> : null}
              {!detailLoading && !detailError && selectedApplication ? (
                <>
                  <div className={s.statusStepper}>
                    {STATUS_STEPS.map((step) => (
                      <div
                        key={step.id}
                        className={`${s.statusStep} ${isStatusStepActive(step.id) ? s.statusStepActive : ''}`}
                      >
                        {step.label}
                      </div>
                    ))}
                  </div>

                  <div className={s.tags}>
                    <span className={`${s.statusPill} ${s[`status${selectedApplication.trangThai}` as keyof typeof s]}`}>
                      {selectedApplication.trangThaiLabel}
                    </span>
                    <span className={s.tag}>{selectedApplication.tenUngVien}</span>
                    <span className={s.tag}>{selectedApplication.tenCongTy}</span>
                    <span className={s.tag}>Tin: {selectedApplication.tieuDeTin}</span>
                  </div>

                  <div className={s.alert}>
                    <strong>Tin liên quan:</strong> {selectedApplication.tieuDeTin}
                    {' '}
                    <Link
                      className={s.inlineLink}
                      to={`${ROUTES.recruiter.jobs}?selectedJobId=${selectedApplication.tinTuyenDungId}`}
                    >
                      Mở tin này
                    </Link>
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
          </section>

          <section className={s.rightPanel}>
            <section className={s.card}>
              <h3 className={s.cardTitle}>Timeline phỏng vấn (D9-D14)</h3>
              {timelineInterviews.length === 0 ? (
                <div className={s.alert}>Chưa có lịch phỏng vấn cho đơn này.</div>
              ) : (
                <div className={s.timeline}>
                  {timelineInterviews.map((row) => (
                    <article key={row.id} className={s.timelineItem}>
                      <strong>{row.tieuDeVong}</strong>
                      <span className={s.meta}>{formatDate(row.thoiGianBatDau)} → {formatDate(row.thoiGianKetThuc)}</span>
                      <span className={s.meta}>{row.hinhThuc} · {row.trangThai}</span>
                      <span className={s.meta}>{row.diaDiemHoacLink || 'Chưa có địa điểm/link'}</span>
                      {!isProfileMode ? (
                        <button
                          type="button"
                          className={`${s.btn} ${s.btnGhost}`}
                          disabled={!isInterviewStage}
                          onClick={() => void handlePickInterview(row)}
                        >
                          Chọn để sửa
                        </button>
                      ) : null}
                    </article>
                  ))}
                </div>
              )}
            </section>

            {!isProfileMode ? (
              <section className={s.card}>
                <h3 className={s.cardTitle}>Form phỏng vấn</h3>
                {!isInterviewStage ? (
                  <div className={s.alert}>
                    Form phỏng vấn chỉ khả dụng khi trạng thái đơn là Phỏng vấn.
                  </div>
                ) : null}
                <div className={s.formAccordion}>
                  <button
                    type="button"
                    className={s.accordionToggle}
                    disabled={!isInterviewStage}
                    onClick={() => setShowInterviewForm((prev) => !prev)}
                  >
                    {showInterviewForm ? 'Ẩn form phỏng vấn' : 'Mở form phỏng vấn'}
                  </button>

                  {showInterviewForm ? (
                    <fieldset style={{ border: 'none', margin: 0, padding: 0, display: 'grid', gap: 10 }} disabled={!isInterviewStage || saving}>
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
                        <button type="button" className={`${s.btn} ${s.btnGhost}`} disabled={saving || !isInterviewStage} onClick={resetInterviewForm}>
                          Làm mới form
                        </button>
                        <button
                          type="button"
                          className={`${s.btn} ${s.btnPrimary}`}
                          disabled={saving || !isInterviewStage}
                          onClick={() => void handleCreateInterview()}
                        >
                          Tạo lịch
                        </button>
                        <button
                          type="button"
                          className={`${s.btn} ${s.btnGhost}`}
                          disabled={saving || !isInterviewStage || selectedInterviewId === null}
                          onClick={() => void handleUpdateInterview()}
                        >
                          Cập nhật lịch
                        </button>
                        <button
                          type="button"
                          className={`${s.btn} ${s.btnDanger}`}
                          disabled={saving || !isInterviewStage || selectedInterviewId === null}
                          onClick={() => void handleCancelInterview()}
                        >
                          Hủy lịch
                        </button>
                      </div>
                    </fieldset>
                  ) : null}
                </div>
              </section>
            ) : null}
          </section>
        </div>
      </div>
    </MainLayout>
  );
}

export default function RecruiterApplicantsRoutePage() {
  return <RecruiterApplicantsPage viewMode="applicants" />;
}
