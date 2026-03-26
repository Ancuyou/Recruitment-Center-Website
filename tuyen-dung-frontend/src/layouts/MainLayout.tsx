import type { ReactNode } from 'react';
import Sidebar from '@/components/layout/Sidebar';
import Header from '@/components/layout/Header';
import s from './MainLayout.module.css';

interface Props {
  children: ReactNode;
  title: string;
  breadcrumb?: string;
}

/**
 * MainLayout — Single Responsibility: wraps page content with Sidebar + Header.
 * Pages only need to pass `title` and their content.
 */
export default function MainLayout({ children, title, breadcrumb }: Props) {
  return (
    <div className={s.shell}>
      <Sidebar />
      <div className={s.content}>
        <Header title={title} breadcrumb={breadcrumb} />
        <main className={s.main}>{children}</main>
      </div>
    </div>
  );
}
