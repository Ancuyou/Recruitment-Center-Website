import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ROUTES } from '@/constants/routes';
import { savedJobsLocal } from '@/services/local/saved-jobs.local';
import { applicationService } from '@/services/modules/application.module';
import { cvService } from '@/services/modules/cv.module';
import { jobService } from '@/services/modules/job.module';
import { useAuthStore } from '@/store/auth.store';
import type { CvItem } from '@/types/cv.types';
import type { JobPosting, JobSkill, KhuVuc } from '@/types/job.types';
import s from '@/assets/styles/job-public.module.css';

type JobDetailPageProps = {
  embedded?: boolean;
};

function formatSalary(min?: number, max?: number): string {
  if (min == null && max == null) return 'Thỏa thuận';
  if (min != null && max != null) {
    return `${Number(min).toLocaleString('vi-VN')} - ${Number(max).toLocaleString('vi-VN')} VND`;
  }
  if (min != null) return `Từ ${Number(min).toLocaleString('vi-VN')} VND`;
  return `Đến ${Number(max).toLocaleString('vi-VN')} VND`;
}

function mapKhuVucLabel(value: KhuVuc): string {
  return value.replace(/_/g, ' ');
}

function mapError(error: unknown): string {
  return (
    (error as { response?: { data?: { message?: string } } })?.response?.data?.message ??
    'Không thể tải chi tiết tin tuyển dụng.'
  );
}

export default function JobDetailPage({ embedded = false }: JobDetailPageProps) {
  const { id } = useParams();
  const authUser = useAuthStore((state) => state.user);
  const [job, setJob] = useState<JobPosting | null>(null);
  const [skills, setSkills] = useState<JobSkill[]>([]);
  const [locations, setLocations] = useState<KhuVuc[]>([]);
  const [candidateCvs, setCandidateCvs] = useState<CvItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [cvLoading, setCvLoading] = useState(false);
  const [saved, setSaved] = useState(false);
  const [selectedCvId, setSelectedCvId] = useState<number | null>(null);
  const [thuNgo, setThuNgo] = useState('');
  const [submitLoading, setSubmitLoading] = useState(false);
  const [submitSuccess, setSubmitSuccess] = useState('');
  const [submitError, setSubmitError] = useState('');
  const [error, setError] = useState('');

  const jobId = useMemo(() => Number(id), [id]);
  const isCandidate = useMemo(() => authUser?.vaiTro === 'UNG_VIEN', [authUser?.vaiTro]);
  const backToJobsPath = useMemo(() => {
    if (embedded && isCandidate) return ROUTES.candidate.jobs;
    return ROUTES.public.jobs;
  }, [embedded, isCandidate]);

  useEffect(() => {
    let mounted = true;

    const fetchDetail = async () => {
      if (!Number.isFinite(jobId)) {
        setError('ID tin tuyển dụng không hợp lệ.');
        setLoading(false);
        return;
      }

      setLoading(true);
      setError('');
      try {
        const [jobData, locationData, skillData] = await Promise.all([
          jobService.getJobById(jobId),
          jobService.getJobLocations(jobId),
          jobService.getJobSkills(jobId),
        ]);
        if (!mounted) return;
        setJob(jobData);
        setLocations(locationData);
        setSkills(skillData);
      } catch (err) {
        if (!mounted) return;
        setError(mapError(err));
      } finally {
        if (mounted) setLoading(false);
      }
    };

    void fetchDetail();
    return () => {
      mounted = false;
    };
  }, [jobId]);

  useEffect(() => {
    if (!isCandidate || !Number.isFinite(jobId)) {
      setSaved(false);
      return;
    }
    setSaved(savedJobsLocal.isSaved(jobId));
  }, [isCandidate, jobId]);

  useEffect(() => {
    let mounted = true;

    const fetchCvs = async () => {
      if (!isCandidate) {
        setCandidateCvs([]);
        setSelectedCvId(null);
        return;
      }

      setCvLoading(true);
      try {
        const cvs = await cvService.getMyCvs();
        if (!mounted) return;

        setCandidateCvs(cvs);
        const defaultCvId = cvs.find((cv) => cv.laCvChinh)?.id ?? cvs[0]?.id ?? null;
        setSelectedCvId(defaultCvId);
      } catch (err) {
        if (!mounted) return;
        setSubmitError(
          (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
          'Không thể tải danh sách CV để ứng tuyển.'
        );
      } finally {
        if (mounted) setCvLoading(false);
      }
    };

    void fetchCvs();
    return () => {
      mounted = false;
    };
  }, [isCandidate]);

  const handleToggleSaved = () => {
    if (!Number.isFinite(jobId)) return;
    const ids = savedJobsLocal.toggle(jobId);
    setSaved(ids.includes(jobId));
  };

  const handleSubmitApplication = async () => {
    if (!job || !selectedCvId) {
      setSubmitError('Vui lòng chọn CV trước khi nộp đơn.');
      return;
    }

    setSubmitLoading(true);
    setSubmitError('');
    setSubmitSuccess('');

    try {
      await applicationService.submitApplication({
        tinTuyenDungId: job.id,
        hoSoCvId: selectedCvId,
        thuNgo: thuNgo.trim() || undefined,
      });

      setSubmitSuccess('Nộp đơn thành công. Bạn có thể theo dõi ở trang Đã ứng tuyển.');
      setThuNgo('');
    } catch (err) {
      setSubmitError(
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        'Không thể nộp đơn ứng tuyển.'
      );
    } finally {
      setSubmitLoading(false);
    }
  };

  const content = (
    <div className={s.container}>
        <section className={s.hero}>
          <h1>Chi tiết tin tuyển dụng</h1>
          <p>Thông tin đang được đồng bộ trực tiếp với backend.</p>
        </section>

        {loading ? <div className={s.alert}>Đang tải dữ liệu...</div> : null}
        {error ? <div className={s.alert}>{error}</div> : null}

        {!loading && !error && job ? (
          <section className={s.detailCard}>
            <h2 className={s.detailTitle}>{job.tieuDe}</h2>
            <p className={s.company}>{job.tenCongTy} · {job.tenNhaTuyenDung}</p>

            <div className={s.jobMeta}>
              <span>📍 {job.diaDiem || 'Nhiều khu vực'}</span>
              <span>💼 {job.capBacYeuCau || 'Không yêu cầu'}</span>
              <span>🧭 {job.hinhThucLamViec || 'Linh hoạt'}</span>
              <span>💰 {formatSalary(job.mucLuongMin, job.mucLuongMax)}</span>
              <span>📅 Hạn nộp: {job.hanNop || 'Chưa cập nhật'}</span>
            </div>

            <div>
              <h3 className={s.sectionTitle}>Mô tả công việc</h3>
              <p className={s.paragraph}>{job.moTaCongViec}</p>
            </div>

            <div>
              <h3 className={s.sectionTitle}>Yêu cầu ứng viên</h3>
              <p className={s.paragraph}>{job.yeuCauUngVien}</p>
            </div>

            <div>
              <h3 className={s.sectionTitle}>Khu vực áp dụng</h3>
              <div className={s.tagWrap}>
                {(locations.length ? locations : job.khuVucs ?? []).map((loc) => (
                  <span key={loc} className={s.tag}>{mapKhuVucLabel(loc)}</span>
                ))}
              </div>
            </div>

            <div>
              <h3 className={s.sectionTitle}>Kỹ năng yêu cầu</h3>
              <div className={s.tagWrap}>
                {skills.length === 0 ? <span className={s.tag}>Chưa có dữ liệu kỹ năng</span> : null}
                {skills.map((skill) => (
                  <span key={skill.id} className={s.tag}>{skill.tenKyNang} · Mức {skill.yeucau}</span>
                ))}
              </div>
            </div>

            {isCandidate ? (
              <div className={s.applyBox}>
                <h3 className={s.sectionTitle}>Nộp đơn ứng tuyển</h3>

                {cvLoading ? <div className={s.alert}>Đang tải danh sách CV...</div> : null}
                {submitError ? <div className={s.alert}>{submitError}</div> : null}
                {submitSuccess ? <div className={s.successAlert}>{submitSuccess}</div> : null}

                {!cvLoading && candidateCvs.length === 0 ? (
                  <div className={s.alert}>
                    Bạn chưa có CV để ứng tuyển. Hãy tạo CV trước tại{' '}
                    <Link to={ROUTES.candidate.cv}>Quản lý CV</Link>.
                  </div>
                ) : null}

                {!cvLoading && candidateCvs.length > 0 ? (
                  <div className={s.applyGrid}>
                    <div>
                      <label className={s.label}>Chọn CV</label>
                      <select
                        className={s.select}
                        value={selectedCvId ?? ''}
                        onChange={(e) => setSelectedCvId(Number(e.target.value))}
                      >
                        {candidateCvs.map((cv) => (
                          <option key={cv.id} value={cv.id}>
                            {cv.tieuDeCv} {cv.laCvChinh ? '(CV chính)' : ''}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div>
                      <label className={s.label}>Thư ngỏ (tùy chọn)</label>
                      <textarea
                        className={s.textarea}
                        value={thuNgo}
                        onChange={(e) => setThuNgo(e.target.value)}
                        placeholder="Giới thiệu ngắn gọn về lý do ứng tuyển"
                      />
                    </div>

                    <div className={s.applyActions}>
                      <button
                        type="button"
                        className={`${s.btn} ${s.btnPrimary}`}
                        disabled={submitLoading}
                        onClick={() => void handleSubmitApplication()}
                      >
                        {submitLoading ? 'Đang nộp đơn...' : 'Nộp đơn ngay'}
                      </button>
                      <Link to={ROUTES.candidate.applications} className={`${s.linkBtn} ${s.linkSecondary}`}>
                        Xem đơn đã nộp
                      </Link>
                    </div>
                  </div>
                ) : null}
              </div>
            ) : null}

            <div className={s.cta}>
              {authUser && isCandidate ? (
                <Link to={ROUTES.candidate.applications} className={`${s.linkBtn} ${s.linkPrimary}`}>
                  Theo dõi đơn ứng tuyển
                </Link>
              ) : (
                <Link to={ROUTES.auth.login} className={`${s.linkBtn} ${s.linkPrimary}`}>
                  Đăng nhập để ứng tuyển
                </Link>
              )}
              {isCandidate ? (
                <button
                  type="button"
                  className={`${s.btn} ${saved ? s.btnGhost : s.btnPrimary}`}
                  onClick={handleToggleSaved}
                >
                  {saved ? 'Bỏ lưu tin này' : 'Lưu tin này'}
                </button>
              ) : null}
              {authUser && !isCandidate ? (
                <span className={s.tag}>Vai trò hiện tại không thể nộp đơn trực tiếp.</span>
              ) : null}
              <Link to={backToJobsPath} className={`${s.linkBtn} ${s.linkSecondary}`}>
                Quay lại danh sách tin
              </Link>
            </div>
          </section>
        ) : null}
    </div>
  );

  if (embedded) {
    return content;
  }

  return (
    <div className={s.page}>
      {content}
    </div>
  );
}
