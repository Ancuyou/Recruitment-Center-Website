import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import MainLayout from '@/layouts/MainLayout';
import AppDataTable, { type AppDataColumn } from '@/components/common/AppDataTable';
import { ROUTES } from '@/constants/routes';
import { useDraftHistory } from '@/hooks/useDraftHistory';
import { applicationService } from '@/services/modules/application.module';
import type { PageResponse } from '@/types/api.types';
import type {
  ApplicationItem,
  ApplicationStatusHistoryItem,
  InterviewItem,
} from '@/types/application.types';
import s from '@/assets/styles/candidate-workflow.module.css';

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

const STATUS_STEPS: Array<{ id: ApplicationItem['trangThai']; label: string }> = [
  { id: 1, label: 'Đã nộp' },
  { id: 2, label: 'Đang xem xét' },
  { id: 3, label: 'Phỏng vấn' },
  { id: 4, label: 'Đã offer' },
  { id: 5, label: 'Từ chối' },
];

export default function CandidateApplicationsPage() {
  const [page, setPage] = useState(0);
  const [pageData, setPageData] = useState<PageResponse<ApplicationItem>>({
    content: [],
    totalElements: 0,
    totalPages: 0,
    number: 0,
    size: 10,
  });
  const {
    value: selectedId,
    setValue: setSelectedId,
  } = useDraftHistory<number | null>({
    storageKey: 'draft.candidate.applications.selectedId',
    initialValue: null,
  });
  const [detail, setDetail] = useState<ApplicationItem | null>(null);
  const [history, setHistory] = useState<ApplicationStatusHistoryItem[]>([]);
  const [interviews, setInterviews] = useState<InterviewItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [error, setError] = useState('');
  const [detailError, setDetailError] = useState('');

  const fetchApplications = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await applicationService.getCandidateApplications(page, 10);
      setPageData(data);
    } catch (err) {
      setError(mapError(err, 'Không thể tải danh sách đơn ứng tuyển.'));
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    void fetchApplications();
  }, [fetchApplications]);

  useEffect(() => {
    if (!selectedId && pageData.content.length > 0) {
      setSelectedId(pageData.content[0].id);
    }
  }, [selectedId, pageData.content, setSelectedId]);

  const fetchDetailBundle = useCallback(async (applicationId: number) => {
    setDetailLoading(true);
    setDetailError('');
    try {
      const [detailData, historyData, interviewsData] = await Promise.all([
        applicationService.getApplicationDetail(applicationId),
        applicationService.getApplicationHistory(applicationId, 0, 10),
        applicationService.getInterviewsByApplication(applicationId),
      ]);

      setDetail(detailData);
      setHistory(historyData.content);
      setInterviews(interviewsData);
    } catch (err) {
      setDetailError(mapError(err, 'Không thể tải chi tiết đơn ứng tuyển.'));
    } finally {
      setDetailLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!selectedId) {
      setDetail(null);
      setHistory([]);
      setInterviews([]);
      return;
    }
    void fetchDetailBundle(selectedId);
  }, [selectedId, fetchDetailBundle]);

  const columns: AppDataColumn<ApplicationItem>[] = [
    {
      key: 'tieuDeTin',
      header: 'Tin tuyển dụng',
      render: (row) => (
        <div style={{ display: 'grid', gap: 4 }}>
          <strong>{row.tieuDeTin}</strong>
          <span className={s.meta}>{row.tenCongTy}</span>
        </div>
      ),
    },
    {
      key: 'trangThaiLabel',
      header: 'Trạng thái',
      width: '180px',
      render: (row) => (
        <span className={`${s.statusPill} ${s[`status${row.trangThai}` as keyof typeof s]}`}>
          {row.trangThaiLabel}
        </span>
      ),
    },
    {
      key: 'ngayNop',
      header: 'Ngày nộp',
      width: '180px',
      render: (row) => formatDate(row.ngayNop),
    },
    {
      key: 'actions',
      header: 'Tác vụ',
      width: '150px',
      align: 'center',
      render: (row) => (
        <button
          type="button"
          className={`${s.btn} ${s.btnGhost}`}
          onClick={() => setSelectedId(row.id)}
        >
          Xem chi tiết
        </button>
      ),
    },
  ];

  const selectedTag = useMemo(() => {
    if (!detail) return '';
    return `${detail.tieuDeTin} · ${detail.tenCongTy}`;
  }, [detail]);

  const historyTimeline = useMemo(
    () => [...history].sort((a, b) => new Date(b.thoiGianChuyen).getTime() - new Date(a.thoiGianChuyen).getTime()),
    [history]
  );

  const interviewTimeline = useMemo(
    () => [...interviews].sort((a, b) => new Date(a.thoiGianBatDau).getTime() - new Date(b.thoiGianBatDau).getTime()),
    [interviews]
  );

  const originalJobPath = useMemo(() => {
    if (!detail) return '';
    return ROUTES.candidate.jobDetail.replace(':id', String(detail.tinTuyenDungId));
  }, [detail]);

  const isStatusStepActive = useCallback((stepId: ApplicationItem['trangThai']): boolean => {
    if (!detail) return false;
    if (detail.trangThai === 5) {
      return stepId === 1 || stepId === 2 || stepId === 3 || stepId === 5;
    }
    return stepId <= detail.trangThai && stepId !== 5;
  }, [detail]);

  return (
    <MainLayout title="Đã ứng tuyển" breadcrumb="Trang chủ / Ứng viên / Đã ứng tuyển">
      <div className={s.stack}>
        <div className={s.topBar}>
          <div className={s.tags}>
            <span className={s.tag}>Tổng đơn: {pageData.totalElements}</span>
            {selectedTag ? <span className={s.tag}>Đang xem: {selectedTag}</span> : null}
          </div>
          <div className={s.actions}>
            <button type="button" className={`${s.btn} ${s.btnGhost}`} onClick={() => void fetchApplications()}>
              Làm mới
            </button>
            <Link to={ROUTES.candidate.jobs} className={s.inlineLink}>
              Tìm thêm việc làm
            </Link>
          </div>
        </div>

        {loading ? <div className={s.alert}>Đang tải danh sách đơn...</div> : null}
        {error ? <div className={`${s.alert} ${s.alertError}`}>{error}</div> : null}

        <div className={s.panelLayout}>
          <aside className={s.leftPanel}>
            <section className={s.card}>
              <h3 className={s.cardTitle}>Danh sách đơn ứng tuyển</h3>
              <AppDataTable
                columns={columns}
                data={pageData.content}
                rowKey={(row) => String(row.id)}
                emptyMessage="Bạn chưa có đơn ứng tuyển nào."
              />

              <div className={s.actions}>
                <button
                  type="button"
                  className={`${s.btn} ${s.btnGhost}`}
                  disabled={page === 0}
                  onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
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

          <section className={s.rightPanel}>
            <section className={s.card}>
              <h3 className={s.cardTitle}>Chi tiết đơn ứng tuyển</h3>
              {detailLoading ? <div className={s.alert}>Đang tải chi tiết...</div> : null}
              {detailError ? <div className={`${s.alert} ${s.alertError}`}>{detailError}</div> : null}
              {!detailLoading && !detailError && detail ? (
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

                  <div className={s.detailRows}>
                    <p><strong>Vị trí:</strong> {detail.tieuDeTin}</p>
                    <p><strong>Công ty:</strong> {detail.tenCongTy}</p>
                    <p><strong>Email ứng viên:</strong> {detail.emailUngVien}</p>
                    <p><strong>Ngày nộp:</strong> {formatDate(detail.ngayNop)}</p>
                    <p>
                      <strong>Trạng thái:</strong>{' '}
                      <span className={`${s.statusPill} ${s[`status${detail.trangThai}` as keyof typeof s]}`}>
                        {detail.trangThaiLabel}
                      </span>
                    </p>
                    <p><strong>Thư ngỏ:</strong> {detail.thuNgo || 'Không có thư ngỏ'}</p>
                    <p>
                      <strong>CV:</strong>{' '}
                      {detail.cvUrl ? (
                        <a className={s.inlineLink} href={detail.cvUrl} target="_blank" rel="noreferrer">
                          Mở file CV
                        </a>
                      ) : (
                        'Chưa có URL file CV'
                      )}
                    </p>
                  </div>

                  {originalJobPath ? (
                    <div className={s.actions}>
                      <Link to={originalJobPath} className={s.inlineLink}>
                        Xem tin gốc
                      </Link>
                    </div>
                  ) : null}
                </>
              ) : null}
              {!detailLoading && !detailError && !detail ? (
                <div className={s.alert}>Chọn một đơn để xem chi tiết.</div>
              ) : null}
            </section>

            <section className={s.card}>
              <h3 className={s.cardTitle}>Timeline trạng thái</h3>
              {historyTimeline.length === 0 ? (
                <div className={s.alert}>Chưa có lịch sử thay đổi cho đơn này.</div>
              ) : (
                <div className={s.timelineList}>
                  {historyTimeline.map((row) => (
                    <article key={row.id} className={s.timelineItem}>
                      <strong>{row.trangThaiMoiLabel}</strong>
                      <span className={s.meta}>{formatDate(row.thoiGianChuyen)}</span>
                      <span className={s.meta}>{row.nguoiThucHien} ({row.vaiTro})</span>
                      <span className={s.timelineTime}>{row.ghiChu || 'Không có ghi chú'}</span>
                    </article>
                  ))}
                </div>
              )}
            </section>

            <section className={s.card}>
              <h3 className={s.cardTitle}>Lịch phỏng vấn của đơn</h3>
              {interviewTimeline.length === 0 ? (
                <div className={s.alert}>Chưa có lịch phỏng vấn cho đơn này.</div>
              ) : (
                <div className={s.timelineList}>
                  {interviewTimeline.map((row) => (
                    <article key={row.id} className={s.timelineItem}>
                      <strong>{row.tieuDeVong}</strong>
                      <span className={s.meta}>{formatDate(row.thoiGianBatDau)} → {formatDate(row.thoiGianKetThuc)}</span>
                      <span className={s.meta}>{row.hinhThuc} · {row.trangThai}</span>
                      <span className={s.timelineTime}>{row.diaDiemHoacLink || 'Chưa có địa điểm/link'}</span>
                    </article>
                  ))}
                </div>
              )}
            </section>
          </section>
        </div>
      </div>
    </MainLayout>
  );
}
