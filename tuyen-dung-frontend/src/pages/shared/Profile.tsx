import { useEffect, useMemo, useState } from 'react';
import MainLayout from '@/layouts/MainLayout';
import AppFormSection from '@/components/common/AppFormSection';
import { useAuthStore } from '@/store/auth.store';
import { userService } from '@/services/modules/user.module';
import type { GioiTinh } from '@/types/auth.types';
import type {
  ChangePasswordRequest,
  UpdateCandidateProfileRequest,
  UpdateRecruiterProfileRequest,
  UploadAvatarRequest,
} from '@/types/user.types';
import s from '@/assets/styles/shared-pages.module.css';

type ProfileFormState = {
  hoTen: string;
  soDienThoai: string;
  gioiTinh: '' | GioiTinh;
  ngaySinh: string;
  chucVu: string;
};

const EMPTY_PROFILE: ProfileFormState = {
  hoTen: '',
  soDienThoai: '',
  gioiTinh: '',
  ngaySinh: '',
  chucVu: '',
};

function mapError(error: unknown, fallback: string): string {
  return (
    (error as { response?: { data?: { message?: string } } })?.response?.data?.message ?? fallback
  );
}

export default function ProfilePage() {
  const user = useAuthStore((state) => state.user);
  const updateUser = useAuthStore((state) => state.updateUser);

  const [profile, setProfile] = useState(EMPTY_PROFILE);
  const [avatarUrl, setAvatarUrl] = useState('');
  const [passwordState, setPasswordState] = useState({ oldPassword: '', newPassword: '' });
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const isCandidate = useMemo(() => user?.vaiTro === 'UNG_VIEN', [user?.vaiTro]);
  const isRecruiter = useMemo(() => user?.vaiTro === 'NHA_TUYEN_DUNG', [user?.vaiTro]);

  useEffect(() => {
    let mounted = true;

    const fetchProfile = async () => {
      setLoading(true);
      setError('');
      try {
        const data = await userService.getProfile();
        if (!mounted) return;

        setProfile({
          hoTen: data.hoTen ?? '',
          soDienThoai: data.soDienThoai ?? '',
          gioiTinh: (data.gioiTinh as ProfileFormState['gioiTinh']) ?? '',
          ngaySinh: data.ngaySinh ?? '',
          chucVu: data.chucVu ?? '',
        });
        setAvatarUrl(data.anhDaiDien ?? '');
      } catch (err) {
        if (!mounted) return;
        setError(mapError(err, 'Không thể tải hồ sơ.'));
      } finally {
        if (mounted) setLoading(false);
      }
    };

    void fetchProfile();
    return () => {
      mounted = false;
    };
  }, []);

  const handleProfileSubmit = async () => {
    setMessage('');
    setError('');

    try {
      if (isCandidate) {
        const payload: UpdateCandidateProfileRequest = {
          hoTen: profile.hoTen,
          soDienThoai: profile.soDienThoai || undefined,
          gioiTinh: profile.gioiTinh || undefined,
          ngaySinh: profile.ngaySinh || undefined,
        };
        const updated = await userService.updateCandidateProfile(payload);
        updateUser({ hoTen: updated.hoTen, anhDaiDien: updated.anhDaiDien });
      } else if (isRecruiter) {
        const payload: UpdateRecruiterProfileRequest = {
          hoTen: profile.hoTen,
          soDienThoai: profile.soDienThoai || undefined,
          chucVu: profile.chucVu || '',
        };
        const updated = await userService.updateRecruiterProfile(payload);
        updateUser({ hoTen: updated.hoTen, anhDaiDien: updated.anhDaiDien });
      }

      setMessage('Cập nhật hồ sơ thành công.');
    } catch (err) {
      setError(mapError(err, 'Không thể cập nhật hồ sơ.'));
    }
  };

  const handleAvatarSubmit = async () => {
    setMessage('');
    setError('');

    const payload: UploadAvatarRequest = { avatarUrl };
    try {
      const updated = await userService.uploadAvatar(payload);
      updateUser({ hoTen: updated.hoTen, anhDaiDien: updated.anhDaiDien });
      setMessage('Cập nhật ảnh đại diện thành công.');
    } catch (err) {
      setError(mapError(err, 'Không thể cập nhật ảnh đại diện.'));
    }
  };

  const handlePasswordSubmit = async () => {
    setMessage('');
    setError('');

    if (!passwordState.oldPassword || !passwordState.newPassword) {
      setError('Vui lòng nhập đầy đủ mật khẩu cũ và mật khẩu mới.');
      return;
    }

    const payload: ChangePasswordRequest = {
      oldPassword: passwordState.oldPassword,
      newPassword: passwordState.newPassword,
    };

    try {
      await userService.changePassword(payload);
      setPasswordState({ oldPassword: '', newPassword: '' });
      setMessage('Đổi mật khẩu thành công.');
    } catch (err) {
      setError(mapError(err, 'Không thể đổi mật khẩu.'));
    }
  };

  return (
    <MainLayout
      title="Hồ sơ cá nhân"
      breadcrumb={`Trang chủ / ${isRecruiter ? 'Nhà tuyển dụng' : 'Ứng viên'} / Hồ sơ`}
    >
      <div className={s.stack}>
        {loading ? <div className={s.alert}>Đang tải hồ sơ...</div> : null}
        {message ? <div className={`${s.alert} ${s.alertSuccess}`}>{message}</div> : null}
        {error ? <div className={`${s.alert} ${s.alertError}`}>{error}</div> : null}

        <AppFormSection title="Thông tin tài khoản" description="Dữ liệu dùng endpoint /api/users/profile">
          <div className={s.grid2}>
            <div className={s.field}>
              <label className={s.label}>Email</label>
              <input className={`${s.input} ${s.readOnly}`} value={user?.email ?? ''} readOnly />
            </div>
            <div className={s.field}>
              <label className={s.label}>Vai trò</label>
              <input className={`${s.input} ${s.readOnly}`} value={user?.vaiTro ?? ''} readOnly />
            </div>
            <div className={s.field}>
              <label className={s.label}>Họ tên</label>
              <input
                className={s.input}
                value={profile.hoTen}
                onChange={(e) => setProfile((prev) => ({ ...prev, hoTen: e.target.value }))}
              />
            </div>
            <div className={s.field}>
              <label className={s.label}>Số điện thoại</label>
              <input
                className={s.input}
                value={profile.soDienThoai}
                onChange={(e) => setProfile((prev) => ({ ...prev, soDienThoai: e.target.value }))}
              />
            </div>
            {isCandidate ? (
              <>
                <div className={s.field}>
                  <label className={s.label}>Giới tính</label>
                  <select
                    className={s.select}
                    value={profile.gioiTinh}
                    onChange={(e) => setProfile((prev) => ({ ...prev, gioiTinh: e.target.value as ProfileFormState['gioiTinh'] }))}
                  >
                    <option value="">Chưa chọn</option>
                    <option value="NAM">Nam</option>
                    <option value="NU">Nữ</option>
                    <option value="KHAC">Khác</option>
                  </select>
                </div>
                <div className={s.field}>
                  <label className={s.label}>Ngày sinh</label>
                  <input
                    className={s.input}
                    type="date"
                    value={profile.ngaySinh}
                    onChange={(e) => setProfile((prev) => ({ ...prev, ngaySinh: e.target.value }))}
                  />
                </div>
              </>
            ) : null}
            {isRecruiter ? (
              <div className={s.field}>
                <label className={s.label}>Chức vụ</label>
                <input
                  className={s.input}
                  value={profile.chucVu}
                  onChange={(e) => setProfile((prev) => ({ ...prev, chucVu: e.target.value }))}
                />
              </div>
            ) : null}
          </div>
          <div className={s.actions}>
            <button type="button" className={`${s.btn} ${s.btnPrimary}`} onClick={handleProfileSubmit}>
              Lưu hồ sơ
            </button>
          </div>
        </AppFormSection>

        <AppFormSection title="Ảnh đại diện" description="Backend hiện dùng URL string cho avatar">
          <div className={s.grid2}>
            <div className={s.field}>
              <label className={s.label}>Avatar URL</label>
              <input
                className={s.input}
                placeholder="https://..."
                value={avatarUrl}
                onChange={(e) => setAvatarUrl(e.target.value)}
              />
            </div>
            <div className={s.field}>
              <label className={s.label}>Xem nhanh</label>
              <div className={s.badges}>
                {avatarUrl ? <span className={s.badge}>URL hợp lệ để gửi backend</span> : <span className={s.badge}>Chưa có avatar</span>}
              </div>
            </div>
          </div>
          <div className={s.actions}>
            <button type="button" className={`${s.btn} ${s.btnPrimary}`} onClick={handleAvatarSubmit}>
              Cập nhật ảnh
            </button>
          </div>
        </AppFormSection>

        <AppFormSection title="Đổi mật khẩu" description="Yêu cầu mật khẩu cũ và mật khẩu mới">
          <div className={s.grid2}>
            <div className={s.field}>
              <label className={s.label}>Mật khẩu cũ</label>
              <input
                type="password"
                className={s.input}
                value={passwordState.oldPassword}
                onChange={(e) => setPasswordState((prev) => ({ ...prev, oldPassword: e.target.value }))}
              />
            </div>
            <div className={s.field}>
              <label className={s.label}>Mật khẩu mới</label>
              <input
                type="password"
                className={s.input}
                value={passwordState.newPassword}
                onChange={(e) => setPasswordState((prev) => ({ ...prev, newPassword: e.target.value }))}
              />
            </div>
          </div>
          <div className={s.actions}>
            <button type="button" className={`${s.btn} ${s.btnGhost}`} onClick={() => setPasswordState({ oldPassword: '', newPassword: '' })}>
              Xóa nội dung
            </button>
            <button type="button" className={`${s.btn} ${s.btnPrimary}`} onClick={handlePasswordSubmit}>
              Đổi mật khẩu
            </button>
          </div>
        </AppFormSection>
      </div>
    </MainLayout>
  );
}
