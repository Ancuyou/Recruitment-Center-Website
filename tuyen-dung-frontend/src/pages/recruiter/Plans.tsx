import MainLayout from '@/layouts/MainLayout';
import s from '@/assets/styles/recruiter-workflow.module.css';

export default function RecruiterPlansPage() {
  return (
    <MainLayout title="Gói dịch vụ" breadcrumb="Trang chủ / Nhà tuyển dụng / Gói dịch vụ">
      <div className={s.stack}>
        <section className={s.card}>
          <h3 className={s.cardTitle}>Trạng thái tích hợp backend</h3>
          <div className={s.alert}>
            Backend hiện tại chưa có API quản lý gói dịch vụ riêng cho nhà tuyển dụng.
            Màn hình này được giữ để hệ thống frontend đồng bộ route, và sẽ nối API ngay khi backend bổ sung endpoint.
          </div>
        </section>
      </div>
    </MainLayout>
  );
}
