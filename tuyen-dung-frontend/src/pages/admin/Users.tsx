import { useState } from 'react';
import MainLayout from '@/layouts/MainLayout';
import AdminRoleNav from '@/components/admin/AdminRoleNav';
import { authService } from '@/services/modules/auth.module';
import type { UserInfoResponse } from '@/types/auth.types';
import a from '@/assets/styles/admin-console.module.css';

function mapError(error: unknown): string {
  return (
    (error as { response?: { data?: { message?: string } } })?.response?.data?.message ??
    'Không thể lấy thông tin người dùng.'
  );
}

export default function AdminUsersPage() {
  const [userIdInput, setUserIdInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [userInfo, setUserInfo] = useState<UserInfoResponse | null>(null);

  const handleLookup = async () => {
    const id = Number(userIdInput);
    if (!Number.isInteger(id) || id <= 0) {
      setError('Vui lòng nhập User ID hợp lệ.');
      setMessage('');
      setUserInfo(null);
      return;
    }

    setLoading(true);
    setError('');
    setMessage('');
    try {
      const data = await authService.getThongTin(id);
      setUserInfo(data);
      setMessage('Lấy thông tin người dùng thành công.');
    } catch (err) {
      setError(mapError(err));
      setUserInfo(null);
    } finally {
      setLoading(false);
    }
  };

  return (
    <MainLayout title="Người dùng" breadcrumb="Trang chủ / Admin / Người dùng">
      <div className={a.page}>
        <AdminRoleNav />

        <section className={a.hero}>
          <h2>Quản trị người dùng theo mã định danh</h2>
          <p>
            Hiện backend chỉ có endpoint tra cứu theo User ID, nên giao diện được tối ưu theo luồng tìm nhanh và kiểm tra thông tin.
          </p>
        </section>

        <section className={a.surface}>
          <div className={a.sectionHead}>
            <h3 className={a.sectionTitle}>Tra cứu người dùng</h3>
            <span className={a.chip}>API hiện có: GET /api/auth/thong-tin/{'{id}'}</span>
          </div>
          <div className={`${a.notice} ${a.noticeWarn}`}>
            Chưa có endpoint danh sách toàn bộ người dùng. Luồng hiện tại gồm nhập ID, tra cứu và kiểm tra vai trò cùng thông tin hồ sơ.
          </div>
          <div className={a.toolbar}>
            <input
              className={a.input}
              style={{ maxWidth: 260 }}
              value={userIdInput}
              onChange={(e) => setUserIdInput(e.target.value)}
              placeholder="Nhập User ID"
            />
            <button
              type="button"
              className={`${a.btn} ${a.btnPrimary}`}
              disabled={loading}
              onClick={() => void handleLookup()}
            >
              Tra cứu
            </button>
          </div>
        </section>

        {loading ? <div className={a.notice}>Đang tải dữ liệu người dùng...</div> : null}
        {error ? <div className={`${a.notice} ${a.noticeError}`}>{error}</div> : null}
        {message ? <div className={`${a.notice} ${a.noticeSuccess}`}>{message}</div> : null}

        <section className={a.surface}>
          <div className={a.sectionHead}>
            <h3 className={a.sectionTitle}>Kết quả tra cứu</h3>
            {userInfo ? <span className={a.chip}>User ID: {userInfo.taiKhoanId}</span> : null}
          </div>
          {!userInfo ? (
            <div className={a.notice}>Nhập User ID và bấm Tra cứu để xem chi tiết.</div>
          ) : (
            <div className={a.kvGrid}>
              <div className={a.kvItem}>
                <span className={a.kvLabel}>Tài khoản ID</span>
                <span className={a.kvValue}>{String(userInfo.taiKhoanId)}</span>
              </div>
              <div className={a.kvItem}>
                <span className={a.kvLabel}>Vai trò</span>
                <span className={a.kvValue}>{userInfo.vaiTro}</span>
              </div>
              <div className={a.kvItem}>
                <span className={a.kvLabel}>Email</span>
                <span className={a.kvValue}>{userInfo.email}</span>
              </div>
              <div className={a.kvItem}>
                <span className={a.kvLabel}>Họ tên</span>
                <span className={a.kvValue}>{userInfo.hoTen || 'Chưa có dữ liệu'}</span>
              </div>
              <div className={a.kvItem}>
                <span className={a.kvLabel}>Số điện thoại</span>
                <span className={a.kvValue}>{userInfo.soDienThoai || 'Chưa có dữ liệu'}</span>
              </div>
              <div className={a.kvItem}>
                <span className={a.kvLabel}>Tên công ty</span>
                <span className={a.kvValue}>{userInfo.tenCongTy || 'Không áp dụng'}</span>
              </div>
            </div>
          )}
        </section>
      </div>
    </MainLayout>
  );
}
