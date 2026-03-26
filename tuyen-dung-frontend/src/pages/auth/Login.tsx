import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import AuthLayout from '@/layouts/AuthLayout';
import { authService } from '@/services/auth.service';
import { useAuthStore } from '@/store/auth.store';
import s from '@/assets/styles/auth.module.css';

const schema = z.object({
  email: z.string().email('Email không hợp lệ'),
  matKhau: z.string().min(1, 'Vui lòng nhập mật khẩu'),
});

type FormData = z.infer<typeof schema>;

export default function Login() {
  const navigate = useNavigate();
  const setAuth = useAuthStore((state) => state.setAuth);
  const [showPwd, setShowPwd] = useState(false);
  const [serverError, setServerError] = useState('');

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({ resolver: zodResolver(schema) });

  const onSubmit = async (data: FormData) => {
    setServerError('');
    try {
      const res = await authService.dangNhap(data);
      setAuth(res);
      // Redirect based on role
      if (res.vaiTro === 'UNG_VIEN') navigate('/ung-vien/dashboard');
      else if (res.vaiTro === 'NHA_TUYEN_DUNG') navigate('/nha-tuyen-dung/dashboard');
      else navigate('/');
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })
          ?.response?.data?.message ?? 'Email hoặc mật khẩu không đúng';
      setServerError(msg);
    }
  };

  return (
    <AuthLayout title="Chào mừng trở lại 👋" subtitle="Đăng nhập để tiếp tục hành trình của bạn">
      <form onSubmit={handleSubmit(onSubmit)} className={s.form} noValidate>
        {serverError && (
          <div className={s.alertError}>
            <span>⚠️</span> {serverError}
          </div>
        )}

        {/* Email */}
        <div className={s.formGroup}>
          <label className={s.label}>Email</label>
          <div className={s.inputWrapper}>
            <span className={s.inputIcon}>📧</span>
            <input
              {...register('email')}
              type="email"
              className={`${s.input} ${errors.email ? s.error : ''}`}
              placeholder="email@example.com"
              autoComplete="email"
            />
          </div>
          {errors.email && <span className={s.errorMsg}>{errors.email.message}</span>}
        </div>

        {/* Password */}
        <div className={s.formGroup}>
          <label className={s.label}>Mật khẩu</label>
          <div className={s.inputWrapper}>
            <span className={s.inputIcon}>🔒</span>
            <input
              {...register('matKhau')}
              type={showPwd ? 'text' : 'password'}
              className={`${s.input} ${errors.matKhau ? s.error : ''}`}
              placeholder="Nhập mật khẩu"
              autoComplete="current-password"
            />
            <button type="button" className={s.eyeBtn} onClick={() => setShowPwd((v) => !v)}>
              {showPwd ? '🙈' : '👁️'}
            </button>
          </div>
          {errors.matKhau && <span className={s.errorMsg}>{errors.matKhau.message}</span>}
        </div>

        {/* Submit */}
        <button type="submit" className={s.submitBtn} disabled={isSubmitting}>
          {isSubmitting ? <span className={s.spinner} /> : null}
          {isSubmitting ? 'Đang đăng nhập…' : 'Đăng nhập'}
        </button>
      </form>

      <div className={s.footerLink} style={{ marginTop: 20 }}>
        Chưa có tài khoản?{' '}
        <Link to="/dang-ky">Đăng ký ngay</Link>
      </div>
    </AuthLayout>
  );
}
