import { useEffect, useMemo, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import AuthLayout from '@/layouts/AuthLayout';
import { ROUTES } from '@/constants/routes';
import { authService } from '@/services/auth.service';
import s from '@/assets/styles/auth.module.css';

function mapError(error: unknown): string {
  return (
    (error as { response?: { data?: { message?: string } } })?.response?.data?.message ??
    'Không thể xác thực email. Token có thể đã hết hạn.'
  );
}

export default function VerifyEmail() {
  const [params] = useSearchParams();
  const token = useMemo(() => params.get('token') ?? '', [params]);
  const [loading, setLoading] = useState(true);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    let mounted = true;

    const verify = async () => {
      if (!token) {
        setError('Thiếu token xác thực trong URL.');
        setLoading(false);
        return;
      }

      try {
        await authService.verifyEmail(token);
        if (!mounted) return;
        setSuccess('Xác thực email thành công. Bạn có thể đăng nhập.');
      } catch (err) {
        if (!mounted) return;
        setError(mapError(err));
      } finally {
        if (mounted) setLoading(false);
      }
    };

    void verify();
    return () => {
      mounted = false;
    };
  }, [token]);

  return (
    <AuthLayout title="Xác thực email" subtitle="Đang kiểm tra token xác thực tài khoản">
      <div className={s.form}>
        {loading ? (
          <div className={s.alertError} style={{ background: '#eff6ff', borderColor: '#bfdbfe', color: '#1d4ed8' }}>
            ⏳ Đang xác thực email...
          </div>
        ) : null}

        {!loading && success ? (
          <div className={s.alertError} style={{ background: '#ecfdf5', borderColor: '#86efac', color: '#166534' }}>
            ✅ {success}
          </div>
        ) : null}

        {!loading && error ? <div className={s.alertError}>⚠️ {error}</div> : null}

        <div className={s.footerLink} style={{ marginTop: 8 }}>
          <Link to={ROUTES.auth.login}>Đi tới đăng nhập</Link>
        </div>
      </div>
    </AuthLayout>
  );
}
