import { useMemo, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import AuthLayout from '@/layouts/AuthLayout';
import { ROUTES } from '@/constants/routes';
import { authService } from '@/services/auth.service';
import s from '@/assets/styles/auth.module.css';

const schema = z.object({
  token: z.string().min(1, 'Token không được để trống'),
  newPassword: z.string().min(6, 'Mật khẩu mới tối thiểu 6 ký tự'),
  confirmPassword: z.string().min(6, 'Vui lòng xác nhận mật khẩu'),
}).refine((data) => data.newPassword === data.confirmPassword, {
  path: ['confirmPassword'],
  message: 'Mật khẩu xác nhận không khớp',
});

type FormData = z.infer<typeof schema>;

function mapError(error: unknown): string {
  return (
    (error as { response?: { data?: { message?: string } } })?.response?.data?.message ??
    'Không thể đặt lại mật khẩu.'
  );
}

export default function ResetPassword() {
  const [params] = useSearchParams();
  const tokenFromQuery = useMemo(() => params.get('token') ?? '', [params]);
  const [success, setSuccess] = useState('');
  const [serverError, setServerError] = useState('');

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      token: tokenFromQuery,
      newPassword: '',
      confirmPassword: '',
    },
  });

  const onSubmit = async (data: FormData) => {
    setSuccess('');
    setServerError('');
    try {
      await authService.resetPassword(data.token, data.newPassword);
      setSuccess('Đặt lại mật khẩu thành công. Bạn có thể đăng nhập lại.');
    } catch (error) {
      setServerError(mapError(error));
    }
  };

  return (
    <AuthLayout title="Đặt lại mật khẩu" subtitle="Nhập token và mật khẩu mới">
      <form onSubmit={handleSubmit(onSubmit)} className={s.form} noValidate>
        {serverError ? <div className={s.alertError}>⚠️ {serverError}</div> : null}
        {success ? (
          <div className={s.alertError} style={{ background: '#ecfdf5', borderColor: '#86efac', color: '#166534' }}>
            ✅ {success}
          </div>
        ) : null}

        <div className={s.formGroup}>
          <label className={s.label}>Token</label>
          <div className={s.inputWrapper}>
            <span className={s.inputIcon}>🪪</span>
            <input
              {...register('token')}
              className={`${s.input} ${errors.token ? s.error : ''}`}
              placeholder="Token xác thực"
            />
          </div>
          {errors.token ? <span className={s.errorMsg}>{errors.token.message}</span> : null}
        </div>

        <div className={s.formGroup}>
          <label className={s.label}>Mật khẩu mới</label>
          <div className={s.inputWrapper}>
            <span className={s.inputIcon}>🔒</span>
            <input
              {...register('newPassword')}
              type="password"
              className={`${s.input} ${errors.newPassword ? s.error : ''}`}
              placeholder="Mật khẩu mới"
            />
          </div>
          {errors.newPassword ? <span className={s.errorMsg}>{errors.newPassword.message}</span> : null}
        </div>

        <div className={s.formGroup}>
          <label className={s.label}>Xác nhận mật khẩu</label>
          <div className={s.inputWrapper}>
            <span className={s.inputIcon}>🔐</span>
            <input
              {...register('confirmPassword')}
              type="password"
              className={`${s.input} ${errors.confirmPassword ? s.error : ''}`}
              placeholder="Nhập lại mật khẩu mới"
            />
          </div>
          {errors.confirmPassword ? <span className={s.errorMsg}>{errors.confirmPassword.message}</span> : null}
        </div>

        <button type="submit" className={s.submitBtn} disabled={isSubmitting}>
          {isSubmitting ? <span className={s.spinner} /> : null}
          {isSubmitting ? 'Đang cập nhật...' : 'Đặt lại mật khẩu'}
        </button>
      </form>

      <div className={s.footerLink} style={{ marginTop: 20 }}>
        <Link to={ROUTES.auth.login}>← Quay lại đăng nhập</Link>
      </div>
    </AuthLayout>
  );
}
