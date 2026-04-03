import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import MainLayout from '@/layouts/MainLayout';
import AppDataTable, { type AppDataColumn } from '@/components/common/AppDataTable';
import { ROUTES } from '@/constants/routes';
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

export default function CandidateApplicationsPage() {
  const [page, setPage] = useState(0);
  const [pageData, setPageData] = useState<PageResponse<ApplicationItem>>({
    content: [],
    totalElements: 0,
    totalPages: 0,
    number: 0,
    size: 10,
  });
  const [selectedId, setSelectedId] = useState<number | null>(null);
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
  }, [selectedId, pageData.content]);

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

        <div className={s.grid2}>
          <section className={s.card}>
            <h3 className={s.cardTitle}>Chi tiết đơn ứng tuyển</h3>
            {detailLoading ? <div className={s.alert}>Đang tải chi tiết...</div> : null}
            {detailError ? <div className={`${s.alert} ${s.alertError}`}>{detailError}</div> : null}
            {!detailLoading && !detailError && detail ? (
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
            ) : null}
            {!detailLoading && !detailError && !detail ? (
              <div className={s.alert}>Chọn một đơn để xem chi tiết.</div>
            ) : null}
          </section>

          <section className={s.card}>
            <h3 className={s.cardTitle}>Lịch phỏng vấn của đơn</h3>
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
                  width: '160px',
                  render: (row: InterviewItem) => formatDate(row.thoiGianBatDau),
                },
                {
                  key: 'diaDiemHoacLink',
                  header: 'Địa điểm/Link',
                  width: '180px',
                  render: (row: InterviewItem) => row.diaDiemHoacLink || '-',
                },
              ]}
              data={interviews}
              rowKey={(row) => String(row.id)}
              emptyMessage="Chưa có lịch phỏng vấn cho đơn này."
            />
          </section>
        </div>

        <section className={s.card}>
          <h3 className={s.cardTitle}>Lịch sử trạng thái</h3>
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
                width: '180px',
                render: (row: ApplicationStatusHistoryItem) => `${row.nguoiThucHien} (${row.vaiTro})`,
              },
              {
                key: 'ghiChu',
                header: 'Ghi chú',
                render: (row: ApplicationStatusHistoryItem) => row.ghiChu || '-',
              },
            ]}
            data={history}
            rowKey={(row) => String(row.id)}
            emptyMessage="Chưa có lịch sử thay đổi cho đơn này."
          />
        </section>
      </div>
    </MainLayout>
  );
}
