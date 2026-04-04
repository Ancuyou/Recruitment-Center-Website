import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import AuthLayout from '@/layouts/AuthLayout';
import { ROUTES, getHomePathByRole } from '@/constants/routes';
import { authService } from '@/services/auth.service';
import { useAuthStore } from '@/store/auth.store';
import s from '@/assets/styles/auth.module.css';

// ── Tab types ───────────────────────────────────────────────────
type Tab = 'ung-vien' | 'nha-tuyen-dung';

// ── Candidate schema ────────────────────────────────────────────
const ungVienSchema = z.object({
  hoTen: z.string().min(2, 'Họ tên phải có ít nhất 2 ký tự'),
  email: z.string().email('Email không hợp lệ'),
  matKhau: z.string().min(6, 'Mật khẩu phải có ít nhất 6 ký tự'),
  xacNhanMatKhau: z.string(),
  soDienThoai: z.string().regex(/^\d{10,11}$/, 'Số điện thoại không hợp lệ').optional().or(z.literal('')),
}).refine((d) => d.matKhau === d.xacNhanMatKhau, {
  message: 'Mật khẩu xác nhận không khớp',
  path: ['xacNhanMatKhau'],
});
type UngVienForm = z.infer<typeof ungVienSchema>;

// ── Recruiter schema ─────────────────────────────────────────────
const nhaTuyenDungSchema = z.object({
  hoTen: z.string().min(2, 'Họ tên phải có ít nhất 2 ký tự'),
  email: z.string().email('Email không hợp lệ'),
  matKhau: z.string().min(6, 'Mật khẩu phải có ít nhất 6 ký tự'),
  xacNhanMatKhau: z.string(),
  chucVu: z.string().min(1, 'Vui lòng nhập chức vụ'),
  tenCongTy: z.string().min(1, 'Vui lòng nhập tên công ty'),
  maSoThue: z.string().regex(/^\d{10,14}$/, 'Mã số thuế phải gồm 10-14 chữ số'),
  soDienThoai: z.string().optional(),
  nganhNghe: z.string().optional(),
  website: z.string().optional(),
}).refine((d) => d.matKhau === d.xacNhanMatKhau, {
  message: 'Mật khẩu xác nhận không khớp',
  path: ['xacNhanMatKhau'],
});
type NhaTuyenDungForm = z.infer<typeof nhaTuyenDungSchema>;

// ════════════════════════════════════════════════════════════════
export default function Register() {
  const [tab, setTab] = useState<Tab>('ung-vien');
  return (
    <AuthLayout title="Tạo tài khoản" subtitle="Tham gia cộng đồng TuyenDungPro ngay hôm nay">
      {/* Tab switcher */}
      <div className={s.tabs}>
        <button className={`${s.tab} ${tab === 'ung-vien' ? s.active : ''}`} onClick={() => setTab('ung-vien')}>
          🙋 Ứng viên
        </button>
        <button className={`${s.tab} ${tab === 'nha-tuyen-dung' ? s.active : ''}`} onClick={() => setTab('nha-tuyen-dung')}>
          🏢 Nhà tuyển dụng
        </button>
      </div>

      {tab === 'ung-vien' ? <UngVienForm /> : <NhaTuyenDungForm />}

      <div className={s.footerLink} style={{ marginTop: 16 }}>
        Đã có tài khoản? <Link to={ROUTES.auth.login}>Đăng nhập</Link>
      </div>
    </AuthLayout>
  );
}

// ── Candidate form ───────────────────────────────────────────────
function UngVienForm() {
  const navigate = useNavigate();
  const setAuth = useAuthStore((s) => s.setAuth);
  const [showPwd, setShowPwd] = useState(false);
  const [serverError, setServerError] = useState('');

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<UngVienForm>({
    resolver: zodResolver(ungVienSchema),
  });

  const onSubmit = async (data: UngVienForm) => {
    setServerError('');
    try {
      const { xacNhanMatKhau: _skip, ...payload } = data;
      void _skip;
      const res = await authService.dangKyUngVien({ ...payload, soDienThoai: payload.soDienThoai || undefined });
      setAuth(res);
      navigate(getHomePathByRole(res.vaiTro), { replace: true });
    } catch (err: unknown) {
      setServerError(
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Đăng ký thất bại'
      );
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className={s.form} noValidate>
      {serverError && <div className={s.alertError}>⚠️ {serverError}</div>}

      <div className={s.formRow}>
        <Field label="Họ và tên" error={errors.hoTen?.message}>
          <span className={s.inputIcon}>👤</span>
          <input {...register('hoTen')} className={`${s.input} ${errors.hoTen ? s.error : ''}`} placeholder="Nguyễn Văn A" />
        </Field>
        <Field label="Số điện thoại" error={errors.soDienThoai?.message}>
          <span className={s.inputIcon}>📱</span>
          <input {...register('soDienThoai')} className={`${s.input} ${errors.soDienThoai ? s.error : ''}`} placeholder="0901234567" />
        </Field>
      </div>

      <Field label="Email" error={errors.email?.message}>
        <span className={s.inputIcon}>📧</span>
        <input {...register('email')} type="email" className={`${s.input} ${errors.email ? s.error : ''}`} placeholder="email@example.com" />
      </Field>

      <div className={s.formRow}>
        <Field label="Mật khẩu" error={errors.matKhau?.message}>
          <span className={s.inputIcon}>🔒</span>
          <input {...register('matKhau')} type={showPwd ? 'text' : 'password'} className={`${s.input} ${errors.matKhau ? s.error : ''}`} placeholder="Tối thiểu 6 ký tự" />
          <button type="button" className={s.eyeBtn} onClick={() => setShowPwd((v) => !v)}>{showPwd ? '🙈' : '👁️'}</button>
        </Field>
        <Field label="Xác nhận mật khẩu" error={errors.xacNhanMatKhau?.message}>
          <span className={s.inputIcon}>🔒</span>
          <input {...register('xacNhanMatKhau')} type="password" className={`${s.input} ${errors.xacNhanMatKhau ? s.error : ''}`} placeholder="Nhập lại mật khẩu" />
        </Field>
      </div>

      <button type="submit" className={s.submitBtn} disabled={isSubmitting}>
        {isSubmitting ? <span className={s.spinner} /> : null}
        {isSubmitting ? 'Đang đăng ký…' : 'Đăng ký tài khoản ứng viên'}
      </button>
    </form>
  );
}

// ── Recruiter form ───────────────────────────────────────────────
function NhaTuyenDungForm() {
  const navigate = useNavigate();
  const setAuth = useAuthStore((s) => s.setAuth);
  const [showPwd, setShowPwd] = useState(false);
  const [serverError, setServerError] = useState('');

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<NhaTuyenDungForm>({
    resolver: zodResolver(nhaTuyenDungSchema),
  });

  const onSubmit = async (data: NhaTuyenDungForm) => {
    setServerError('');
    try {
      const { xacNhanMatKhau: _skip, ...payload } = data;
      void _skip;
      const res = await authService.dangKyNhaTuyenDung(payload);
      setAuth(res);
      navigate(getHomePathByRole(res.vaiTro), { replace: true });
    } catch (err: unknown) {
      setServerError(
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Đăng ký thất bại'
      );
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className={s.form} noValidate>
      {serverError && <div className={s.alertError}>⚠️ {serverError}</div>}

      <p className={s.sectionLabel}>Thông tin cá nhân</p>
      <div className={s.formRow}>
        <Field label="Họ và tên" error={errors.hoTen?.message}>
          <span className={s.inputIcon}>👤</span>
          <input {...register('hoTen')} className={`${s.input} ${errors.hoTen ? s.error : ''}`} placeholder="Nguyễn Thị B" />
        </Field>
        <Field label="Chức vụ" error={errors.chucVu?.message}>
          <span className={s.inputIcon}>💼</span>
          <input {...register('chucVu')} className={`${s.input} ${errors.chucVu ? s.error : ''}`} placeholder="HR Manager" />
        </Field>
      </div>

      <div className={s.formRow}>
        <Field label="Email" error={errors.email?.message}>
          <span className={s.inputIcon}>📧</span>
          <input {...register('email')} type="email" className={`${s.input} ${errors.email ? s.error : ''}`} placeholder="hr@company.com" />
        </Field>
        <Field label="Số điện thoại" error={errors.soDienThoai?.message}>
          <span className={s.inputIcon}>📱</span>
          <input {...register('soDienThoai')} className={`${s.input} ${errors.soDienThoai ? s.error : ''}`} placeholder="0901234567" />
        </Field>
      </div>

      <div className={s.formRow}>
        <Field label="Mật khẩu" error={errors.matKhau?.message}>
          <span className={s.inputIcon}>🔒</span>
          <input {...register('matKhau')} type={showPwd ? 'text' : 'password'} className={`${s.input} ${errors.matKhau ? s.error : ''}`} placeholder="Tối thiểu 6 ký tự" />
          <button type="button" className={s.eyeBtn} onClick={() => setShowPwd((v) => !v)}>{showPwd ? '🙈' : '👁️'}</button>
        </Field>
        <Field label="Xác nhận mật khẩu" error={errors.xacNhanMatKhau?.message}>
          <span className={s.inputIcon}>🔒</span>
          <input {...register('xacNhanMatKhau')} type="password" className={`${s.input} ${errors.xacNhanMatKhau ? s.error : ''}`} placeholder="Nhập lại mật khẩu" />
        </Field>
      </div>

      <p className={s.sectionLabel}>Thông tin công ty</p>
      <div className={s.formRow}>
        <Field label="Tên công ty" error={errors.tenCongTy?.message}>
          <span className={s.inputIcon}>🏢</span>
          <input {...register('tenCongTy')} className={`${s.input} ${errors.tenCongTy ? s.error : ''}`} placeholder="Công ty ABC" />
        </Field>
        <Field label="Mã số thuế" error={errors.maSoThue?.message}>
          <span className={s.inputIcon}>🔖</span>
          <input {...register('maSoThue')} className={`${s.input} ${errors.maSoThue ? s.error : ''}`} placeholder="0123456789" />
        </Field>
      </div>

      <div className={s.formRow}>
        <Field label="Ngành nghề" error={undefined}>
          <span className={s.inputIcon}>🏭</span>
          <input {...register('nganhNghe')} className={s.input} placeholder="Công nghệ thông tin" />
        </Field>
        <Field label="Website" error={undefined}>
          <span className={s.inputIcon}>🌐</span>
          <input {...register('website')} className={s.input} placeholder="https://company.com" />
        </Field>
      </div>

      <button type="submit" className={s.submitBtn} disabled={isSubmitting}>
        {isSubmitting ? <span className={s.spinner} /> : null}
        {isSubmitting ? 'Đang đăng ký…' : 'Đăng ký tài khoản nhà tuyển dụng'}
      </button>
    </form>
  );
}

// ── Shared field wrapper ─────────────────────────────────────────
function Field({ label, error, children }: { label: string; error?: string; children: React.ReactNode }) {
  return (
    <div className={s.formGroup}>
      <label className={s.label}>{label}</label>
      <div className={s.inputWrapper}>{children}</div>
      {error && <span className={s.errorMsg}>{error}</span>}
    </div>
  );
}
