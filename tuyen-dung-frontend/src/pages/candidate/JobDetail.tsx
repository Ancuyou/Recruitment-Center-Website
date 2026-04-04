import MainLayout from '@/layouts/MainLayout';
import JobDetailPage from '@/pages/public/JobDetail';

export default function CandidateJobDetailPage() {
  return (
    <MainLayout title="Chi tiết việc làm" breadcrumb="Trang chủ / Ứng viên / Tìm việc / Chi tiết">
      <JobDetailPage embedded />
    </MainLayout>
  );
}
