import MainLayout from '@/layouts/MainLayout';
import JobsPage from '@/pages/public/Jobs';

export default function CandidateJobsPage() {
  return (
    <MainLayout title="Tìm việc làm" breadcrumb="Trang chủ / Ứng viên / Tìm việc">
      <JobsPage embedded />
    </MainLayout>
  );
}