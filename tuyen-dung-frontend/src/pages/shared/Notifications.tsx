import { useCallback, useEffect, useMemo, useState } from 'react';
import MainLayout from '@/layouts/MainLayout';
import AppDataTable, { type AppDataColumn } from '@/components/common/AppDataTable';
import { useAuthStore } from '@/store/auth.store';
import { notificationService } from '@/services/modules/notification.module';
import type { NotificationItem } from '@/types/notification.types';
import s from '@/assets/styles/shared-pages.module.css';

function mapError(error: unknown): string {
  return (
    (error as { response?: { data?: { message?: string } } })?.response?.data?.message ??
    'Không thể tải thông báo.'
  );
}

function formatDate(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString('vi-VN');
}

export default function NotificationsPage() {
  const role = useAuthStore((state) => state.user?.vaiTro);
  const [data, setData] = useState<NotificationItem[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const breadcrumb = useMemo(() => {
    if (role === 'NHA_TUYEN_DUNG') return 'Trang chủ / Nhà tuyển dụng / Thông báo';
    return 'Trang chủ / Ứng viên / Thông báo';
  }, [role]);

  const fetchNotifications = useCallback(async () => {
    setLoading(true);
    setError('');

    try {
      const [pageResult, count] = await Promise.all([
        notificationService.getMyNotifications(page, 10),
        notificationService.countUnread(),
      ]);
      setData(pageResult.content);
      setTotalPages(Math.max(pageResult.totalPages, 1));
      setUnreadCount(count);
    } catch (err) {
      setError(mapError(err));
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    void fetchNotifications();
  }, [fetchNotifications]);

  const markOne = async (id: number) => {
    await notificationService.markAsRead(id);
    setData((prev) => prev.map((item) => (item.id === id ? { ...item, daDoc: true } : item)));
    setUnreadCount((prev) => Math.max(0, prev - 1));
  };

  const markAll = async () => {
    await notificationService.markAllAsRead();
    setData((prev) => prev.map((item) => ({ ...item, daDoc: true })));
    setUnreadCount(0);
  };

  const columns: AppDataColumn<NotificationItem>[] = [
    {
      key: 'tieuDe',
      header: 'Tiêu đề',
      render: (row) => (
        <div style={{ display: 'grid', gap: 4 }}>
          <strong>{row.tieuDe}</strong>
          <span style={{ color: '#64748b', fontSize: 12 }}>{row.noiDung}</span>
        </div>
      ),
    },
    {
      key: 'loaiThongBao',
      header: 'Loại',
      width: '160px',
      render: (row) => row.loaiThongBao,
    },
    {
      key: 'ngayTao',
      header: 'Thời gian',
      width: '180px',
      render: (row) => formatDate(row.ngayTao),
    },
    {
      key: 'daDoc',
      header: 'Trạng thái',
      width: '160px',
      render: (row) => (row.daDoc ? 'Đã đọc' : 'Chưa đọc'),
    },
    {
      key: 'actions',
      header: 'Tác vụ',
      align: 'center',
      width: '140px',
      render: (row) => (
        <button
          type="button"
          className={`${s.btn} ${s.btnGhost}`}
          disabled={row.daDoc}
          onClick={() => void markOne(row.id)}
        >
          Đánh dấu đọc
        </button>
      ),
    },
  ];

  return (
    <MainLayout title="Thông báo" breadcrumb={breadcrumb}>
      <div className={s.stack}>
        <div className={s.badges}>
          <span className={s.badge}>Chưa đọc: {unreadCount}</span>
          <span className={s.badge}>Tổng trang: {totalPages}</span>
        </div>

        <div className={s.actions}>
          <button type="button" className={`${s.btn} ${s.btnGhost}`} onClick={() => void fetchNotifications()}>
            Làm mới
          </button>
          <button type="button" className={`${s.btn} ${s.btnPrimary}`} onClick={() => void markAll()}>
            Đánh dấu tất cả đã đọc
          </button>
        </div>

        {loading ? <div className={s.alert}>Đang tải thông báo...</div> : null}
        {error ? <div className={`${s.alert} ${s.alertError}`}>{error}</div> : null}

        <AppDataTable
          columns={columns}
          data={data}
          rowKey={(row) => String(row.id)}
          emptyMessage="Không có thông báo nào trong trang này."
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
            disabled={page >= totalPages - 1}
            onClick={() => setPage((prev) => prev + 1)}
          >
            Trang sau →
          </button>
        </div>
      </div>
    </MainLayout>
  );
}
