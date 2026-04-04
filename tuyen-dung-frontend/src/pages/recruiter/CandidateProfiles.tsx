import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import MainLayout from '@/layouts/MainLayout';
import AppDataTable, { type AppDataColumn } from '@/components/common/AppDataTable';
import { ROUTES } from '@/constants/routes';
import { jobService } from '@/services/modules/job.module';
import { matchingService } from '@/services/modules/matching.module';
import type { JobPosting } from '@/types/job.types';
import type { CandidateSuggestion, MatchScoreResult } from '@/types/matching.types';
import s from '@/assets/styles/recruiter-workflow.module.css';

function mapError(error: unknown, fallback: string): string {
  return (
    (error as { response?: { data?: { message?: string } } })?.response?.data?.message ?? fallback
  );
}

export default function RecruiterCandidateProfilesPage() {
  const [jobs, setJobs] = useState<JobPosting[]>([]);
  const [selectedJobId, setSelectedJobId] = useState<number | null>(null);
  const [limit, setLimit] = useState(10);
  const [suggestions, setSuggestions] = useState<CandidateSuggestion[]>([]);
  const [selectedSuggestion, setSelectedSuggestion] = useState<CandidateSuggestion | null>(null);
  const [scoreResult, setScoreResult] = useState<MatchScoreResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [scoreLoading, setScoreLoading] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const normalizedLimit = useMemo(() => {
    if (!Number.isFinite(limit)) return 10;
    return Math.min(50, Math.max(1, Math.trunc(limit)));
  }, [limit]);

  const selectedJob = useMemo(
    () => jobs.find((job) => job.id === selectedJobId) ?? null,
    [jobs, selectedJobId]
  );

  const fetchJobs = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const myJobs = await jobService.getMyJobs();
      setJobs(myJobs);
      if (myJobs.length === 0) {
        setSelectedJobId(null);
      } else {
        setSelectedJobId((prev) => (prev && myJobs.some((item) => item.id === prev) ? prev : myJobs[0].id));
      }
    } catch (err) {
      setError(mapError(err, 'Không thể tải danh sách tin tuyển dụng.'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void fetchJobs();
  }, [fetchJobs]);

  const fetchSuggestions = useCallback(async () => {
    if (!selectedJobId) {
      setSuggestions([]);
      setSelectedSuggestion(null);
      setScoreResult(null);
      return;
    }

    setLoading(true);
    setError('');
    try {
      const data = await matchingService.suggestCandidatesForJob(selectedJobId, normalizedLimit);
      setSuggestions(data);
      setSelectedSuggestion((prev) => {
        if (!prev) return data[0] ?? null;
        return data.find((item) => item.cvId === prev.cvId && item.ungVienId === prev.ungVienId) ?? data[0] ?? null;
      });
      setScoreResult(null);
    } catch (err) {
      setError(mapError(err, 'Không thể lấy danh sách ứng viên gợi ý.'));
      setSuggestions([]);
      setSelectedSuggestion(null);
      setScoreResult(null);
    } finally {
      setLoading(false);
    }
  }, [selectedJobId, normalizedLimit]);

  useEffect(() => {
    void fetchSuggestions();
  }, [fetchSuggestions]);

  const handleRecalculate = async (strategy?: string) => {
    if (!selectedSuggestion || !selectedJobId) {
      setError('Hãy chọn ứng viên và tin tuyển dụng trước khi tính điểm.');
      return;
    }

    setScoreLoading(true);
    setError('');
    setMessage('');
    try {
      const payload = { cvId: selectedSuggestion.cvId, jobId: selectedJobId };
      const result = strategy
        ? await matchingService.calculateMatchScoreWithStrategy(payload, strategy)
        : await matchingService.calculateMatchScore(payload);

      setScoreResult(result);
      setMessage(
        strategy
          ? `Đã tính điểm theo strategy ${strategy}.`
          : 'Đã tính điểm theo strategy mặc định.'
      );
    } catch (err) {
      setError(mapError(err, 'Không thể tính lại điểm matching.'));
      setScoreResult(null);
    } finally {
      setScoreLoading(false);
    }
  };

  const columns: AppDataColumn<CandidateSuggestion>[] = [
    {
      key: 'hoTen',
      header: 'Ứng viên',
      render: (row) => (
        <div style={{ display: 'grid', gap: 4 }}>
          <strong>{row.hoTen}</strong>
          <span className={s.meta}>{row.email}</span>
        </div>
      ),
    },
    {
      key: 'matchPercentage',
      header: 'Điểm phù hợp',
      width: '150px',
      render: (row) => `${row.matchPercentage}% (${row.matchLevel})`,
    },
    {
      key: 'skillMatchCount',
      header: 'Kỹ năng khớp',
      width: '130px',
      render: (row) => `${row.skillMatchCount}/${row.totalSkillsRequired}`,
    },
    {
      key: 'actions',
      header: 'Tác vụ',
      width: '120px',
      align: 'center',
      render: (row) => (
        <button
          type="button"
          className={`${s.btn} ${s.btnGhost}`}
          onClick={() => setSelectedSuggestion(row)}
        >
          Chọn
        </button>
      ),
    },
  ];

  return (
    <MainLayout title="Hồ sơ ứng viên" breadcrumb="Trang chủ / Nhà tuyển dụng / Hồ sơ ứng viên">
      <div className={s.stack}>
        <div className={s.topBar}>
          <div className={s.tags}>
            <span className={s.tag}>Tin của bạn: {jobs.length}</span>
            <span className={s.tag}>Ứng viên gợi ý: {suggestions.length}</span>
          </div>
          <div className={s.actions}>
            <Link to={ROUTES.recruiter.applicants} className={s.inlineLink}>Xem ứng viên nộp thực tế</Link>
            <button type="button" className={`${s.btn} ${s.btnGhost}`} onClick={() => void fetchJobs()}>
              Làm mới
            </button>
          </div>
        </div>

        {loading ? <div className={s.alert}>Đang tải dữ liệu matching...</div> : null}
        {error ? <div className={`${s.alert} ${s.alertError}`}>{error}</div> : null}
        {message ? <div className={`${s.alert} ${s.alertSuccess}`}>{message}</div> : null}

        <section className={s.card}>
          <h3 className={s.cardTitle}>Bộ lọc matching</h3>
          <div className={s.grid2}>
            <div className={s.field}>
              <label className={s.label}>Tin tuyển dụng</label>
              <select
                className={s.select}
                value={selectedJobId ?? ''}
                onChange={(e) => setSelectedJobId(e.target.value ? Number(e.target.value) : null)}
              >
                <option value="">-- Chọn tin --</option>
                {jobs.map((job) => (
                  <option key={job.id} value={job.id}>{job.tieuDe}</option>
                ))}
              </select>
            </div>
            <div className={s.field}>
              <label className={s.label}>Số lượng gợi ý</label>
              <input
                className={s.input}
                type="number"
                min={1}
                max={50}
                value={normalizedLimit}
                onChange={(e) => {
                  const next = Number(e.target.value);
                  setLimit(Number.isFinite(next) ? next : 10);
                }}
              />
            </div>
          </div>
        </section>

        <section className={s.card}>
          <h3 className={s.cardTitle}>Danh sách hồ sơ gợi ý theo tin</h3>
          <AppDataTable
            columns={columns}
            data={suggestions}
            rowKey={(row) => `${row.ungVienId}-${row.cvId}`}
            emptyMessage="Chưa có hồ sơ gợi ý cho tin đã chọn."
          />
        </section>

        <div className={s.grid2}>
          <section className={s.card}>
            <h3 className={s.cardTitle}>Hồ sơ đang chọn</h3>
            {!selectedSuggestion ? (
              <div className={s.alert}>Chọn một ứng viên để xem chi tiết.</div>
            ) : (
              <div className={s.field}>
                <div className={s.tags}>
                  <span className={s.tag}>{selectedSuggestion.hoTen}</span>
                  <span className={s.tag}>{selectedSuggestion.email}</span>
                  <span className={s.tag}>CV: {selectedSuggestion.tieuDeCv}</span>
                  <span className={s.tag}>Điểm hiện tại: {selectedSuggestion.matchPercentage}%</span>
                </div>
                <div className={s.alert}>
                  <strong>Tin đang đối chiếu:</strong> {selectedJob?.tieuDe || 'Chưa chọn'}
                </div>
                <div className={s.actions}>
                  <button
                    type="button"
                    className={`${s.btn} ${s.btnPrimary}`}
                    disabled={scoreLoading}
                    onClick={() => void handleRecalculate()}
                  >
                    Tính lại điểm (default)
                  </button>
                  <button
                    type="button"
                    className={`${s.btn} ${s.btnGhost}`}
                    disabled={scoreLoading}
                    onClick={() => void handleRecalculate('keyword_matching')}
                  >
                    Tính theo keyword
                  </button>
                </div>
              </div>
            )}
          </section>

          <section className={s.card}>
            <h3 className={s.cardTitle}>Kết quả chấm điểm chi tiết</h3>
            {!scoreResult ? (
              <div className={s.alert}>Chưa có kết quả tính lại điểm.</div>
            ) : (
              <>
                <div className={s.tags}>
                  <span className={s.tag}>Match: {scoreResult.matchPercentage}%</span>
                  <span className={s.tag}>Level: {scoreResult.matchLevel}</span>
                </div>
                <div className={s.field}>
                  <label className={s.label}>Kỹ năng khớp</label>
                  <div className={s.alert}>
                    {scoreResult.matchedSkills.length > 0 ? scoreResult.matchedSkills.join(', ') : 'Không có'}
                  </div>
                </div>
                <div className={s.field}>
                  <label className={s.label}>Kỹ năng còn thiếu</label>
                  <div className={s.alert}>
                    {scoreResult.missingSkills.length > 0 ? scoreResult.missingSkills.join(', ') : 'Không có'}
                  </div>
                </div>
              </>
            )}
          </section>
        </div>
      </div>
    </MainLayout>
  );
}
