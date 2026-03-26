/* AuthLayout.tsx — Split screen: brand panel left, form right */
import type { ReactNode } from 'react';
import styles from './AuthLayout.module.css';

interface Props {
  children: ReactNode;
  title: string;
  subtitle?: string;
}

export default function AuthLayout({ children, title, subtitle }: Props) {
  return (
    <div className={styles.wrapper}>
      {/* ── Left brand panel ── */}
      <div className={styles.brandPanel}>
        <div className={styles.brandContent}>
          <div className={styles.logo}>
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <rect width="48" height="48" rx="14" fill="white" fillOpacity="0.15"/>
              <path d="M12 34L24 14L36 34H27L24 28L21 34H12Z" fill="white"/>
            </svg>
            <span>TuyenDung<span className={styles.logoAccent}>Pro</span></span>
          </div>
          <h1 className={styles.brandHeading}>Kết nối nhân tài,<br/>Kiến tạo tương lai</h1>
          <p className={styles.brandSub}>
            Nền tảng tuyển dụng thông minh — kết nối hàng nghìn ứng viên&nbsp;
            với các doanh nghiệp hàng đầu.
          </p>
          <ul className={styles.features}>
            {['Hàng nghìn việc làm mỗi ngày', 'Phù hợp theo năng lực thực tế', 'Bảo mật thông tin tuyệt đối'].map(f => (
              <li key={f}>
                <span className={styles.checkIcon}>✓</span>
                {f}
              </li>
            ))}
          </ul>
        </div>
        {/* Decorative blobs */}
        <div className={styles.blob1} />
        <div className={styles.blob2} />
      </div>

      {/* ── Right form panel ── */}
      <div className={styles.formPanel}>
        <div className={styles.formCard}>
          <div className={styles.formHeader}>
            <h2 className={styles.formTitle}>{title}</h2>
            {subtitle && <p className={styles.formSubtitle}>{subtitle}</p>}
          </div>
          {children}
        </div>
      </div>
    </div>
  );
}
