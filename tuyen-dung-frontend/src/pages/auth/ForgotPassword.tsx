import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import AuthLayout from '@/layouts/AuthLayout';
import { ROUTES } from '@/constants/routes';
import { authService } from '@/services/auth.service';
import s from '@/assets/styles/auth.module.css';

const schema = z.object({
  email: z.string().email('Email không hợp lệ'),
});

type FormData = z.infer<typeof schema>;

function mapError(error: unknown): string {
  return (
    (error as { response?: { data?: { message?: string } } })?.response?.data?.message ??
    'Không thể gửi yêu cầu quên mật khẩu.'
  );
}

export default function ForgotPassword() {
  const [success, setSuccess] = useState('');
  const [serverError, setServerError] = useState('');

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({ resolver: zodResolver(schema) });

  const onSubmit = async (data: FormData) => {
    setSuccess('');
    setServerError('');
    try {
      await authService.forgotPassword(data.email);
      setSuccess('Đã gửi hướng dẫn đặt lại mật khẩu. Vui lòng kiểm tra email.');
    } catch (error) {
      setServerError(mapError(error));
    }
  };

  return (
    <AuthLayout title="Quên mật khẩu" subtitle="Nhập email để nhận hướng dẫn đặt lại mật khẩu">
      <form onSubmit={handleSubmit(onSubmit)} className={s.form} noValidate>
        {serverError ? <div className={s.alertError}>⚠️ {serverError}</div> : null}
        {success ? (
          <div className={s.alertError} style={{ background: '#ecfdf5', borderColor: '#86efac', color: '#166534' }}>
            ✅ {success}
          </div>
        ) : null}

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
          {errors.email ? <span className={s.errorMsg}>{errors.email.message}</span> : null}
        </div>

        <button type="submit" className={s.submitBtn} disabled={isSubmitting}>
          {isSubmitting ? <span className={s.spinner} /> : null}
          {isSubmitting ? 'Đang gửi...' : 'Gửi yêu cầu'}
        </button>
      </form>

      <div className={s.footerLink} style={{ marginTop: 20 }}>
        <Link to={ROUTES.auth.login}>← Quay lại đăng nhập</Link>
      </div>
    </AuthLayout>
  );
}
