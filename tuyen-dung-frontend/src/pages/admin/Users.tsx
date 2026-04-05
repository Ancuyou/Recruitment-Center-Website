import { useCallback, useEffect, useMemo, useState } from 'react';
import MainLayout from '@/layouts/MainLayout';
import AdminRoleNav from '@/components/admin/AdminRoleNav';
import AppDataTable, { type AppDataColumn } from '@/components/common/AppDataTable';
import { companyService } from '@/services/modules/company.module';
import { userService } from '@/services/modules/user.module';
import type { GioiTinh, VaiTroTaiKhoan } from '@/types/auth.types';
import type { CompanyItem } from '@/types/company.types';
import type {
  AdminUserCreateRequest,
  AdminUserResponse,
  AdminUserUpdateRequest,
} from '@/types/user.types';
import a from '@/assets/styles/admin-console.module.css';

type UserFormState = {
  email: string;
  matKhau: string;
  vaiTro: VaiTroTaiKhoan;
  laKichHoat: boolean;
  hoTen: string;
  soDienThoai: string;
  ngaySinh: string;
  gioiTinh: '' | GioiTinh;
  congTyId: string;
  chucVu: string;
};

type RoleFilter = '' | VaiTroTaiKhoan;
type StatusFilter = 'ALL' | 'ACTIVE' | 'LOCKED';

const DEFAULT_FORM: UserFormState = {
  email: '',
  matKhau: '',
  vaiTro: 'UNG_VIEN',
  laKichHoat: true,
  hoTen: '',
  soDienThoai: '',
  ngaySinh: '',
  gioiTinh: '',
  congTyId: '',
  chucVu: '',
};

const CANDIDATE_PHONE_REGEX = /^\d{10,11}$/;

function mapError(error: unknown, fallback: string): string {
  return (
    (error as { response?: { data?: { message?: string } } })?.response?.data?.message ?? fallback
  );
}

function formatDateTime(value?: string): string {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString('vi-VN');
}

function toFormState(user: AdminUserResponse): UserFormState {
  return {
    email: user.email,
    matKhau: '',
    vaiTro: user.vaiTro,
    laKichHoat: user.laKichHoat,
    hoTen: user.hoTen ?? '',
    soDienThoai: user.soDienThoai ?? '',
    ngaySinh: user.ngaySinh ?? '',
    gioiTinh: user.gioiTinh ?? '',
    congTyId: user.congTyId ? String(user.congTyId) : '',
    chucVu: user.chucVu ?? '',
  };
}

export default function AdminUsersPage() {
  const [companies, setCompanies] = useState<CompanyItem[]>([]);
  const [users, setUsers] = useState<AdminUserResponse[]>([]);
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null);

  const [keyword, setKeyword] = useState('');
  const [roleFilter, setRoleFilter] = useState<RoleFilter>('');
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL');

  const [formMode, setFormMode] = useState<'create' | 'edit'>('create');
  const [form, setForm] = useState<UserFormState>(DEFAULT_FORM);

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const selectedUser = useMemo(
    () => users.find((item) => item.taiKhoanId === selectedUserId) ?? null,
    [users, selectedUserId]
  );

  const statusFilterValue = useMemo(() => {
    if (statusFilter === 'ACTIVE') return true;
    if (statusFilter === 'LOCKED') return false;
    return undefined;
  }, [statusFilter]);

  const fetchUsers = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await userService.listAdminUsers({
        keyword: keyword.trim() || undefined,
        vaiTro: roleFilter || undefined,
        laKichHoat: statusFilterValue,
      });
      const visibleUsers = data.filter((u) => u.vaiTro !== 'ADMIN');
      setUsers(visibleUsers);
      setSelectedUserId((current) =>
        current != null && !visibleUsers.some((u) => u.taiKhoanId === current) ? null : current
      );
    } catch (err) {
      setError(mapError(err, 'Không thể tải danh sách người dùng.'));
    } finally {
      setLoading(false);
    }
  }, [keyword, roleFilter, statusFilterValue]);

  useEffect(() => {
    void fetchUsers();
  }, [fetchUsers]);

  useEffect(() => {
    const fetchCompanies = async () => {
      try {
        const data = await companyService.getAllCompanies();
        setCompanies(data ?? []);
      } catch {
        setCompanies([]);
      }
    };

    void fetchCompanies();
  }, []);

  const handleSelectUser = (user: AdminUserResponse) => {
    setSelectedUserId(user.taiKhoanId);
    setFormMode('edit');
    setForm(toFormState(user));
    setMessage('');
    setError('');
  };

  const resetForm = () => {
    setFormMode('create');
    setSelectedUserId(null);
    setForm(DEFAULT_FORM);
    setMessage('');
    setError('');
  };

  const requireValue = (value: string, fieldName: string): string => {
    const trimmed = value.trim();
    if (!trimmed) {
      throw new Error(`${fieldName} không được để trống.`);
    }
    return trimmed;
  };

  const optionalValue = (value: string): string | undefined => {
    const trimmed = value.trim();
    return trimmed ? trimmed : undefined;
  };

  const validateCandidatePhone = (phone?: string) => {
    if (!phone) return;
    if (!CANDIDATE_PHONE_REGEX.test(phone)) {
      throw new Error('Số điện thoại ứng viên phải gồm 10-11 chữ số.');
    }
  };

  const buildCreatePayload = (): AdminUserCreateRequest => {
    const payload: AdminUserCreateRequest = {
      email: requireValue(form.email, 'Email'),
      matKhau: requireValue(form.matKhau, 'Mật khẩu'),
      vaiTro: form.vaiTro,
      laKichHoat: form.laKichHoat,
    };

    if (form.vaiTro === 'UNG_VIEN') {
      payload.hoTen = requireValue(form.hoTen, 'Họ tên ứng viên');
      payload.soDienThoai = optionalValue(form.soDienThoai);
      validateCandidatePhone(payload.soDienThoai);
      payload.ngaySinh = optionalValue(form.ngaySinh);
      payload.gioiTinh = form.gioiTinh || undefined;
      return payload;
    }

    if (form.vaiTro === 'NHA_TUYEN_DUNG') {
      payload.hoTen = requireValue(form.hoTen, 'Họ tên nhà tuyển dụng');
      payload.soDienThoai = optionalValue(form.soDienThoai);
      payload.chucVu = optionalValue(form.chucVu);
      const congTyIdText = requireValue(form.congTyId, 'Công ty');
      const congTyId = Number(congTyIdText);
      if (!Number.isInteger(congTyId) || congTyId <= 0) {
        throw new Error('Công ty không hợp lệ.');
      }
      payload.congTyId = congTyId;
      return payload;
    }

    return payload;
  };

  const buildUpdatePayload = (): AdminUserUpdateRequest => {
    const payload: AdminUserUpdateRequest = {
      email: optionalValue(form.email),
    };

    if (form.vaiTro === 'UNG_VIEN') {
      payload.hoTen = optionalValue(form.hoTen);
      payload.soDienThoai = optionalValue(form.soDienThoai);
      validateCandidatePhone(payload.soDienThoai);
      payload.ngaySinh = optionalValue(form.ngaySinh);
      payload.gioiTinh = form.gioiTinh || undefined;
      return payload;
    }

    if (form.vaiTro === 'NHA_TUYEN_DUNG') {
      payload.hoTen = optionalValue(form.hoTen);
      payload.soDienThoai = optionalValue(form.soDienThoai);
      payload.chucVu = optionalValue(form.chucVu);
      const congTyId = optionalValue(form.congTyId);
      if (congTyId) {
        const parsed = Number(congTyId);
        if (!Number.isInteger(parsed) || parsed <= 0) {
          throw new Error('Công ty không hợp lệ.');
        }
        payload.congTyId = parsed;
      }
    }

    return payload;
  };

  const handleCreateUser = async () => {
    setSaving(true);
    setError('');
    setMessage('');
    try {
      const payload = buildCreatePayload();
      const created = await userService.createAdminUser(payload);
      setMessage('Tạo tài khoản người dùng thành công.');
      await fetchUsers();
      handleSelectUser(created);
    } catch (err) {
      const fallback = err instanceof Error ? err.message : 'Không thể tạo người dùng.';
      setError(mapError(err, fallback));
    } finally {
      setSaving(false);
    }
  };

  const handleUpdateUser = async () => {
    if (!selectedUserId) {
      setError('Hãy chọn người dùng cần cập nhật.');
      return;
    }

    setSaving(true);
    setError('');
    setMessage('');
    try {
      const payload = buildUpdatePayload();
      const updated = await userService.updateAdminUser(selectedUserId, payload);
      setMessage('Cập nhật người dùng thành công.');
      await fetchUsers();
      handleSelectUser(updated);
    } catch (err) {
      const fallback = err instanceof Error ? err.message : 'Không thể cập nhật người dùng.';
      setError(mapError(err, fallback));
    } finally {
      setSaving(false);
    }
  };

  const handleToggleLock = async (user: AdminUserResponse) => {
    setSaving(true);
    setError('');
    setMessage('');

    try {
      if (user.laKichHoat) {
        await userService.lockAdminUser(user.taiKhoanId);
        setMessage(`Đã khóa tài khoản #${user.taiKhoanId}.`);
      } else {
        await userService.unlockAdminUser(user.taiKhoanId);
        setMessage(`Đã mở khóa tài khoản #${user.taiKhoanId}.`);
      }
      await fetchUsers();
    } catch (err) {
      setError(mapError(err, 'Không thể cập nhật trạng thái khóa tài khoản.'));
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteUser = async (user: AdminUserResponse) => {
    const confirmed = window.confirm(`Bạn có chắc muốn xóa tài khoản #${user.taiKhoanId}?`);
    if (!confirmed) return;

    setSaving(true);
    setError('');
    setMessage('');
    try {
      await userService.deleteAdminUser(user.taiKhoanId);
      setMessage(`Đã xóa tài khoản #${user.taiKhoanId}.`);
      await fetchUsers();
      if (selectedUserId === user.taiKhoanId) {
        resetForm();
      }
    } catch (err) {
      setError(mapError(err, 'Không thể xóa tài khoản người dùng.'));
    } finally {
      setSaving(false);
    }
  };

  const columns: AppDataColumn<AdminUserResponse>[] = useMemo(
    () => [
      {
        key: 'taiKhoanId',
        header: 'ID',
        width: '90px',
        render: (row) => String(row.taiKhoanId),
      },
      {
        key: 'email',
        header: 'Email',
        render: (row) => row.email,
      },
      {
        key: 'vaiTro',
        header: 'Vai trò',
        width: '160px',
        render: (row) => row.vaiTro,
      },
      {
        key: 'laKichHoat',
        header: 'Trạng thái',
        width: '130px',
        render: (row) => (row.laKichHoat ? 'Đang hoạt động' : 'Đã khóa'),
      },
      {
        key: 'hoTen',
        header: 'Thông tin',
        render: (row) => (
          <div style={{ display: 'grid', gap: 4 }}>
            <strong>{row.hoTen || '-'}</strong>
            <span className={a.muted}>{row.tenCongTy || row.soDienThoai || '-'}</span>
          </div>
        ),
      },
      {
        key: 'actions',
        header: 'Tác vụ',
        width: '300px',
        render: (row) => (
          <div className={a.tableActions}>
            <button
              type="button"
              className={`${a.btn} ${a.btnCompact} ${a.btnGhost}`}
              onClick={() => handleSelectUser(row)}
            >
              Sửa
            </button>
            <button
              type="button"
              className={`${a.btn} ${a.btnCompact} ${row.laKichHoat ? a.btnWarn : a.btnSuccess}`}
              disabled={saving}
              onClick={() => void handleToggleLock(row)}
            >
              {row.laKichHoat ? 'Khóa' : 'Mở khóa'}
            </button>
            <button
              type="button"
              className={`${a.btn} ${a.btnCompact} ${a.btnDanger}`}
              disabled={saving}
              onClick={() => void handleDeleteUser(row)}
            >
              Xóa
            </button>
          </div>
        ),
      },
    ],
    [saving]
  );

  return (
    <MainLayout title="Người dùng" breadcrumb="Trang chủ / Admin / Người dùng">
      <div className={a.page}>
        <AdminRoleNav />

        <section className={a.hero}>
          <h2>Quản trị người dùng (CRUD + Lock/Unlock)</h2>
          <p>
            Admin có thể tạo, xem, cập nhật, xóa và khóa/mở khóa tài khoản người dùng theo vai trò.
          </p>
        </section>

        <section className={a.surface}>
          <div className={a.sectionHead}>
            <h3 className={a.sectionTitle}>Bộ lọc danh sách người dùng</h3>
            <span className={a.chip}>API: /api/admin/users</span>
          </div>

          <div className={a.toolbar}>
            <input
              className={a.input}
              style={{ maxWidth: 280 }}
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder="Tìm theo ID, email, họ tên..."
            />
            <select
              className={a.select}
              style={{ maxWidth: 220 }}
              value={roleFilter}
              onChange={(e) => setRoleFilter(e.target.value as RoleFilter)}
            >
              <option value="">Tất cả vai trò</option>
              <option value="UNG_VIEN">Ứng viên</option>
              <option value="NHA_TUYEN_DUNG">Nhà tuyển dụng</option>
            </select>
            <select
              className={a.select}
              style={{ maxWidth: 220 }}
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value as StatusFilter)}
            >
              <option value="ALL">Tất cả trạng thái</option>
              <option value="ACTIVE">Đang hoạt động</option>
              <option value="LOCKED">Đã khóa</option>
            </select>
            <button
              type="button"
              className={`${a.btn} ${a.btnPrimary}`}
              disabled={loading}
              onClick={() => void fetchUsers()}
            >
              Làm mới
            </button>
          </div>
        </section>

        {loading ? <div className={a.notice}>Đang tải danh sách người dùng...</div> : null}
        {error ? <div className={`${a.notice} ${a.noticeError}`}>{error}</div> : null}
        {message ? <div className={`${a.notice} ${a.noticeSuccess}`}>{message}</div> : null}

        <div className={a.grid2}>
          <section className={a.surface}>
            <div className={a.sectionHead}>
              <h3 className={a.sectionTitle}>Danh sách người dùng</h3>
              <span className={a.chip}>Tổng: {users.length}</span>
            </div>

            <AppDataTable
              columns={columns}
              data={users}
              rowKey={(row) => String(row.taiKhoanId)}
              emptyMessage="Chưa có người dùng phù hợp điều kiện lọc."
            />
          </section>

          <section className={a.surface}>
            <div className={a.sectionHead}>
              <h3 className={a.sectionTitle}>
                {formMode === 'create' ? 'Tạo tài khoản mới' : `Cập nhật tài khoản #${selectedUserId ?? ''}`}
              </h3>
              <div className={a.sectionActions}>
                <button type="button" className={`${a.btn} ${a.btnGhost}`} onClick={resetForm}>
                  Tạo mới
                </button>
              </div>
            </div>

            <div className={a.stack}>
              <div className={a.field}>
                <label className={a.muted}>Email</label>
                <input
                  className={a.input}
                  value={form.email}
                  onChange={(e) => setForm((prev) => ({ ...prev, email: e.target.value }))}
                  placeholder="user@example.com"
                />
              </div>

              {formMode === 'create' ? (
                <div className={a.field}>
                  <label className={a.muted}>Mật khẩu</label>
                  <input
                    className={a.input}
                    type="password"
                    value={form.matKhau}
                    onChange={(e) => setForm((prev) => ({ ...prev, matKhau: e.target.value }))}
                    placeholder="Tối thiểu 6 ký tự"
                  />
                </div>
              ) : null}

              <div className={a.toolbar}>
                <div className={a.field} style={{ minWidth: 220 }}>
                  <label className={a.muted}>Vai trò</label>
                  <select
                    className={a.select}
                    value={form.vaiTro}
                    disabled={formMode === 'edit'}
                    onChange={(e) => setForm((prev) => ({ ...prev, vaiTro: e.target.value as VaiTroTaiKhoan }))}
                  >
                    <option value="UNG_VIEN">Ứng viên</option>
                    <option value="NHA_TUYEN_DUNG">Nhà tuyển dụng</option>
                  </select>
                </div>

                {formMode === 'create' ? (
                  <label className={a.checkItem} style={{ marginTop: 24 }}>
                    <input
                      type="checkbox"
                      checked={form.laKichHoat}
                      onChange={(e) => setForm((prev) => ({ ...prev, laKichHoat: e.target.checked }))}
                    />
                    Kích hoạt ngay sau khi tạo
                  </label>
                ) : null}
              </div>

              <>
                <div className={a.field}>
                  <label className={a.muted}>Họ tên</label>
                  <input
                    className={a.input}
                    value={form.hoTen}
                    onChange={(e) => setForm((prev) => ({ ...prev, hoTen: e.target.value }))}
                    placeholder="Nhập họ tên"
                  />
                </div>

                <div className={a.field}>
                  <label className={a.muted}>Số điện thoại</label>
                  <input
                    className={a.input}
                    value={form.soDienThoai}
                    onChange={(e) => setForm((prev) => ({ ...prev, soDienThoai: e.target.value }))}
                    placeholder="Ví dụ: 0902123456"
                  />
                  {form.vaiTro === 'UNG_VIEN' ? (
                    <small className={a.muted}>Ứng viên: chỉ chấp nhận 10-11 chữ số.</small>
                  ) : null}
                </div>
              </>

              {form.vaiTro === 'UNG_VIEN' ? (
                <div className={a.toolbar}>
                  <div className={a.field} style={{ minWidth: 200 }}>
                    <label className={a.muted}>Ngày sinh</label>
                    <input
                      className={a.input}
                      type="date"
                      value={form.ngaySinh}
                      onChange={(e) => setForm((prev) => ({ ...prev, ngaySinh: e.target.value }))}
                    />
                  </div>
                  <div className={a.field} style={{ minWidth: 200 }}>
                    <label className={a.muted}>Giới tính</label>
                    <select
                      className={a.select}
                      value={form.gioiTinh}
                      onChange={(e) => setForm((prev) => ({ ...prev, gioiTinh: e.target.value as '' | GioiTinh }))}
                    >
                      <option value="">Không xác định</option>
                      <option value="NAM">Nam</option>
                      <option value="NU">Nữ</option>
                      <option value="KHAC">Khác</option>
                    </select>
                  </div>
                </div>
              ) : null}

              {form.vaiTro === 'NHA_TUYEN_DUNG' ? (
                <>
                  <div className={a.field}>
                    <label className={a.muted}>Chức vụ</label>
                    <input
                      className={a.input}
                      value={form.chucVu}
                      onChange={(e) => setForm((prev) => ({ ...prev, chucVu: e.target.value }))}
                      placeholder="Ví dụ: HR Manager"
                    />
                  </div>
                  <div className={a.field}>
                    <label className={a.muted}>Công ty</label>
                    <select
                      className={a.select}
                      value={form.congTyId}
                      onChange={(e) => setForm((prev) => ({ ...prev, congTyId: e.target.value }))}
                    >
                      <option value="">Chọn công ty</option>
                      {companies.map((company) => (
                        <option key={company.id} value={String(company.id)}>
                          {company.tenCongTy}
                        </option>
                      ))}
                    </select>
                  </div>
                </>
              ) : null}

              <div className={a.formActions}>
                {formMode === 'create' ? (
                  <button
                    type="button"
                    className={`${a.btn} ${a.btnPrimary}`}
                    disabled={saving}
                    onClick={() => void handleCreateUser()}
                  >
                    Tạo người dùng
                  </button>
                ) : (
                  <button
                    type="button"
                    className={`${a.btn} ${a.btnPrimary}`}
                    disabled={saving || !selectedUserId}
                    onClick={() => void handleUpdateUser()}
                  >
                    Lưu thay đổi
                  </button>
                )}
              </div>
            </div>
          </section>
        </div>

        {selectedUser ? (
          <section className={a.surface}>
            <div className={a.sectionHead}>
              <h3 className={a.sectionTitle}>Chi tiết người dùng đang chọn</h3>
              <span className={a.chip}>User ID: {selectedUser.taiKhoanId}</span>
            </div>
            <div className={a.kvGrid}>
              <div className={a.kvItem}>
                <span className={a.kvLabel}>Email</span>
                <span className={a.kvValue}>{selectedUser.email}</span>
              </div>
              <div className={a.kvItem}>
                <span className={a.kvLabel}>Vai trò</span>
                <span className={a.kvValue}>{selectedUser.vaiTro}</span>
              </div>
              <div className={a.kvItem}>
                <span className={a.kvLabel}>Trạng thái</span>
                <span className={a.kvValue}>{selectedUser.laKichHoat ? 'Đang hoạt động' : 'Đã khóa'}</span>
              </div>
              <div className={a.kvItem}>
                <span className={a.kvLabel}>Họ tên</span>
                <span className={a.kvValue}>{selectedUser.hoTen || '-'}</span>
              </div>
              <div className={a.kvItem}>
                <span className={a.kvLabel}>Công ty</span>
                <span className={a.kvValue}>{selectedUser.tenCongTy || '-'}</span>
              </div>
              <div className={a.kvItem}>
                <span className={a.kvLabel}>Cập nhật lần cuối</span>
                <span className={a.kvValue}>{formatDateTime(selectedUser.ngayCapNhat)}</span>
              </div>
            </div>
          </section>
        ) : null}
      </div>
    </MainLayout>
  );
}
