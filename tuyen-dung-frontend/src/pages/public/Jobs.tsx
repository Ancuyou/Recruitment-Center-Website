import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { ROUTES } from '@/constants/routes';
import { useAuthStore } from '@/store/auth.store';
import { jobService } from '@/services/modules/job.module';
import { lookupService } from '@/services/modules/lookup.module';
import type { PageResponse } from '@/types/api.types';
import type { CapBacYeuCau, HinhThucLamViec, JobPosting, JobSearchParams } from '@/types/job.types';
import type { LookupItem } from '@/types/lookup.types';
import s from '@/assets/styles/job-public.module.css';

type JobsPageProps = {
  embedded?: boolean;
};

const CAP_BAC_OPTIONS: Array<{ value: CapBacYeuCau; label: string }> = [
  { value: 'FRESHER', label: 'Fresher' },
  { value: 'JUNIOR', label: 'Junior' },
  { value: 'SENIOR', label: 'Senior' },
  { value: 'LEAD', label: 'Lead' },
];

const HINH_THUC_OPTIONS: Array<{ value: HinhThucLamViec; label: string }> = [
  { value: 'ONLINE', label: 'Online' },
  { value: 'OFFICE', label: 'Office' },
  { value: 'HYBRID', label: 'Hybrid' },
];

type SearchFormState = {
  keyword: string;
  capBac: '' | CapBacYeuCau;
  hinhThuc: '' | HinhThucLamViec;
  mucLuongMin: string;
};

const DEFAULT_FILTERS: SearchFormState = {
  keyword: '',
  capBac: '',
  hinhThuc: '',
  mucLuongMin: '',
};

function hasSearchFilters(filters: SearchFormState): boolean {
  return Boolean(filters.keyword || filters.capBac || filters.hinhThuc || filters.mucLuongMin);
}

function formatSalary(min?: number, max?: number): string {
  if (min == null && max == null) return 'Thỏa thuận';
  if (min != null && max != null) {
    return `${Number(min).toLocaleString('vi-VN')} - ${Number(max).toLocaleString('vi-VN')} VND`;
  }
  if (min != null) return `Từ ${Number(min).toLocaleString('vi-VN')} VND`;
  return `Đến ${Number(max).toLocaleString('vi-VN')} VND`;
}

function toJobDetailPath(id: number, useCandidatePath: boolean): string {
  const pattern = useCandidatePath ? ROUTES.candidate.jobDetail : ROUTES.public.jobDetail;
  return pattern.replace(':id', String(id));
}

function mapApiError(error: unknown): string {
  return (
    (error as { response?: { data?: { message?: string } } })?.response?.data?.message ??
    'Không thể tải danh sách tin tuyển dụng.'
  );
}

export default function JobsPage({ embedded = false }: JobsPageProps) {
  const userRole = useAuthStore((state) => state.user?.vaiTro);
  const [filters, setFilters] = useState<SearchFormState>(DEFAULT_FILTERS);
  const [queryFilters, setQueryFilters] = useState<SearchFormState>(DEFAULT_FILTERS);
  const [pageData, setPageData] = useState<PageResponse<JobPosting>>({
    content: [],
    totalElements: 0,
    totalPages: 0,
    number: 0,
    size: 10,
  });
  const [industries, setIndustries] = useState<LookupItem[]>([]);
  const [locations, setLocations] = useState<LookupItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [catalogLoading, setCatalogLoading] = useState(false);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);

  const isSearchMode = useMemo(() => hasSearchFilters(queryFilters), [queryFilters]);
  const isCandidate = useMemo(() => userRole === 'UNG_VIEN', [userRole]);

  useEffect(() => {
    let mounted = true;

    const fetchCatalogs = async () => {
      setCatalogLoading(true);
      try {
        const [industryData, locationData] = await Promise.all([
          lookupService.getIndustries(),
          lookupService.getLocations(),
        ]);
        if (!mounted) return;
        setIndustries(industryData);
        setLocations(locationData);
      } catch {
        if (!mounted) return;
        setIndustries([]);
        setLocations([]);
      } finally {
        if (mounted) setCatalogLoading(false);
      }
    };

    void fetchCatalogs();
    return () => {
      mounted = false;
    };
  }, []);

  useEffect(() => {
    let mounted = true;

    const fetchJobs = async () => {
      setLoading(true);
      setError('');
      try {
        let data: PageResponse<JobPosting>;
        if (hasSearchFilters(queryFilters)) {
          const payload: JobSearchParams = {
            keyword: queryFilters.keyword,
            capBac: queryFilters.capBac || undefined,
            hinhThuc: queryFilters.hinhThuc || undefined,
            mucLuongMin: queryFilters.mucLuongMin ? Number(queryFilters.mucLuongMin) : undefined,
            page,
            size: 10,
          };
          data = await jobService.searchJobs(payload);
        } else {
          data = await jobService.getActiveJobs(page, 10);
        }

        if (!mounted) return;
        setPageData(data);
      } catch (err) {
        if (!mounted) return;
        setError(mapApiError(err));
      } finally {
        if (mounted) setLoading(false);
      }
    };

    void fetchJobs();
    return () => {
      mounted = false;
    };
  }, [page, queryFilters]);

  const handleSearch = () => {
    setPage(0);
    setQueryFilters(filters);
  };

  const handleReset = () => {
    setFilters(DEFAULT_FILTERS);
    setPage(0);
    setQueryFilters(DEFAULT_FILTERS);
  };

  const content = (
    <div className={s.container}>
        <section className={s.hero}>
          <h1>Tìm việc đúng kỹ năng</h1>
          <p>Danh sách tin đang mở được lấy trực tiếp từ backend hiện tại.</p>
        </section>

        <section className={s.filters}>
          <div className={s.filterGrid}>
            <input
              className={s.input}
              placeholder="Tìm theo tiêu đề hoặc mô tả..."
              value={filters.keyword}
              onChange={(e) => setFilters((prev) => ({ ...prev, keyword: e.target.value }))}
            />
            <select
              className={s.select}
              value={filters.capBac}
              onChange={(e) => setFilters((prev) => ({ ...prev, capBac: e.target.value as SearchFormState['capBac'] }))}
            >
              <option value="">Tất cả cấp bậc</option>
              {CAP_BAC_OPTIONS.map((item) => (
                <option key={item.value} value={item.value}>{item.label}</option>
              ))}
            </select>
            <select
              className={s.select}
              value={filters.hinhThuc}
              onChange={(e) => setFilters((prev) => ({ ...prev, hinhThuc: e.target.value as SearchFormState['hinhThuc'] }))}
            >
              <option value="">Tất cả hình thức</option>
              {HINH_THUC_OPTIONS.map((item) => (
                <option key={item.value} value={item.value}>{item.label}</option>
              ))}
            </select>
            <input
              className={s.input}
              type="number"
              min={0}
              placeholder="Lương tối thiểu"
              value={filters.mucLuongMin}
              onChange={(e) => setFilters((prev) => ({ ...prev, mucLuongMin: e.target.value }))}
            />
          </div>
          <div className={s.filterActions}>
            <button type="button" className={`${s.btn} ${s.btnGhost}`} onClick={handleReset}>Xóa lọc</button>
            <button type="button" className={`${s.btn} ${s.btnPrimary}`} onClick={handleSearch}>Tìm kiếm</button>
          </div>
        </section>

        <section className={s.metaRow}>
          <p>
            {loading ? 'Đang tải dữ liệu...' : `Tìm thấy ${pageData.totalElements} tin tuyển dụng`}
          </p>
          <div className={s.tagWrap}>
            {catalogLoading ? (
              <span className={s.tag}>Đang tải danh mục...</span>
            ) : (
              <>
                <span className={s.tag}>Ngành nghề: {industries.length}</span>
                <span className={s.tag}>Khu vực: {locations.length}</span>
                {isSearchMode ? <span className={s.tag}>Đang bật bộ lọc</span> : null}
              </>
            )}
          </div>
        </section>

        {error ? <div className={s.alert}>{error}</div> : null}

        <section className={s.jobList}>
          {pageData.content.map((job) => {
            const detailPath = toJobDetailPath(job.id, embedded && isCandidate);
            return (
              <article key={job.id} className={s.jobCard}>
                <div className={s.jobHead}>
                  <div>
                    <h3 className={s.jobTitle}>
                      <Link to={detailPath}>{job.tieuDe}</Link>
                    </h3>
                    <p className={s.company}>{job.tenCongTy}</p>
                  </div>
                  <div style={{ display: 'grid', gap: 8, justifyItems: 'end' }}>
                    <span className={s.badge}>{job.trangThaiLabel ?? 'Đang mở'}</span>
                    <Link className={`${s.linkBtn} ${s.linkSecondary}`} to={detailPath}>
                      Xem chi tiết
                    </Link>
                  </div>
                </div>
                <div className={s.jobMeta}>
                  <span>📍 {job.diaDiem || 'Nhiều khu vực'}</span>
                  <span>💼 {job.capBacYeuCau || 'Không yêu cầu'}</span>
                  <span>🧭 {job.hinhThucLamViec || 'Linh hoạt'}</span>
                  <span>💰 {formatSalary(job.mucLuongMin, job.mucLuongMax)}</span>
                  <span>🗂️ {job.soLuongDon} đơn</span>
                </div>
              </article>
            );
          })}
          {!loading && pageData.content.length === 0 ? (
            <div className={s.alert}>Không có tin nào phù hợp với điều kiện hiện tại.</div>
          ) : null}
        </section>

        <section className={s.pager}>
          <button type="button" disabled={page === 0 || loading} onClick={() => setPage((prev) => Math.max(prev - 1, 0))}>
            ← Trước
          </button>
          <span>Trang {pageData.number + 1} / {Math.max(pageData.totalPages, 1)}</span>
          <button
            type="button"
            disabled={loading || pageData.totalPages === 0 || page >= pageData.totalPages - 1}
            onClick={() => setPage((prev) => prev + 1)}
          >
            Sau →
          </button>
        </section>
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
