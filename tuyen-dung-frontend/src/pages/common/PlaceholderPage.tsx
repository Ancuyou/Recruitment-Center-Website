import MainLayout from '@/layouts/MainLayout';

type Props = {
  title: string;
  breadcrumb: string;
  description?: string;
};

export default function PlaceholderPage({
  title,
  breadcrumb,
  description = 'Tính năng đang được triển khai để bám sát backend hiện tại.',
}: Props) {
  return (
    <MainLayout title={title} breadcrumb={breadcrumb}>
      <div
        style={{
          background: '#fff',
          border: '1px solid #e2e8f0',
          borderRadius: 12,
          padding: 24,
          color: '#334155',
          display: 'grid',
          gap: 8,
        }}
      >
        <h3 style={{ margin: 0, fontSize: 20, fontWeight: 700 }}>Đang chuẩn hóa màn hình</h3>
        <p style={{ margin: 0, color: '#64748b' }}>{description}</p>
      </div>
    </MainLayout>
  );
}
