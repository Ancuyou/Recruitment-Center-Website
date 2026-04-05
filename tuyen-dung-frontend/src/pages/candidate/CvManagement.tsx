import { useCallback, useEffect, useMemo, useState } from 'react';
import MainLayout from '@/layouts/MainLayout';
import AppDataTable, { type AppDataColumn } from '@/components/common/AppDataTable';
import { useDraftHistory } from '@/hooks/useDraftHistory';
import { cvService } from '@/services/modules/cv.module';
import { lookupService } from '@/services/modules/lookup.module';
import type {
  CvDetailRequest,
  CvDetailItem,
  CvItem,
  CvRequest,
  CvSkillRequest,
  CvSkillItem,
  LoaiBanGhiCv,
} from '@/types/cv.types';
import type { LookupItem } from '@/types/lookup.types';
import s from '@/assets/styles/candidate-workflow.module.css';
import t from '@/assets/styles/tabs.module.css';
import m from '@/assets/styles/modal.module.css';

type CvTab = 'overview' | 'profile' | 'skills' | 'details';

const EMPTY_CV_FORM: CvRequest = {
  tieuDeCv: '',
  mucTieuNgheNghiep: '',
  fileCvUrl: '',
};

type SkillFormState = {
  kyNangId: string;
  mucThanhThao: string;
  moTa: string;
};

const EMPTY_SKILL_FORM: SkillFormState = {
  kyNangId: '',
  mucThanhThao: '3',
  moTa: '',
};

type DetailFormState = {
  loaiBanGhi: LoaiBanGhiCv;
  tenToChuc: string;
  chuyenNganhHoacViTri: string;
  ngayBatDau: string;
  ngayKetThuc: string;
  moTaChiTiet: string;
};

const EMPTY_DETAIL_FORM: DetailFormState = {
  loaiBanGhi: 1,
  tenToChuc: '',
  chuyenNganhHoacViTri: '',
  ngayBatDau: '',
  ngayKetThuc: '',
  moTaChiTiet: '',
};

type DetailTypeFilter = 'ALL' | LoaiBanGhiCv;

const CV_FILE_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ??
  (typeof window !== 'undefined' ? window.location.origin : '');

function mapError(error: unknown, fallback: string): string {
  return (
    (error as { response?: { data?: { message?: string } } })?.response?.data?.message ?? fallback
  );
}

function formatDate(value?: string): string {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString('vi-VN');
}

function loaiBanGhiLabel(value: LoaiBanGhiCv): string {
  if (value === 1) return 'Học vấn';
  if (value === 2) return 'Kinh nghiệm';
  return 'Chứng chỉ';
}

function normalizeCvPayload(form: CvRequest): CvRequest {
  return {
    tieuDeCv: form.tieuDeCv.trim(),
    mucTieuNgheNghiep: form.mucTieuNgheNghiep?.trim() || undefined,
    fileCvUrl: form.fileCvUrl?.trim() || undefined,
  };
}

function resolveCvFileUrl(fileUrl?: string): string {
  const raw = fileUrl?.trim();
  if (!raw) return '';
  if (/^https?:\/\//i.test(raw)) return raw;

  try {
    const base = CV_FILE_BASE_URL?.trim();
    if (!base) return raw;
    const normalizedBase = base.endsWith('/') ? base : `${base}/`;
    return new URL(raw, normalizedBase).toString();
  } catch {
    return raw;
  }
}

function toSameOriginPreviewUrl(fileUrl: string): string {
  if (typeof window === 'undefined') return fileUrl;

  try {
    const parsed = new URL(fileUrl, window.location.origin);
    if (parsed.pathname.startsWith('/uploads/')) {
      return `${window.location.origin}${parsed.pathname}${parsed.search}${parsed.hash}`;
    }
  } catch {
    return fileUrl;
  }

  return fileUrl;
}

export default function CandidateCvManagementPage() {
  const [cvs, setCvs] = useState<CvItem[]>([]);
  const [selectedCvId, setSelectedCvId] = useState<number | null>(null);
  const [selectedCv, setSelectedCv] = useState<CvItem | null>(null);
  const [skills, setSkills] = useState<CvSkillItem[]>([]);
  const [details, setDetails] = useState<CvDetailItem[]>([]);
  const [skillOptions, setSkillOptions] = useState<LookupItem[]>([]);
  const createCvDraft = useDraftHistory<CvRequest>({
    storageKey: 'draft.candidate.cv.create',
    initialValue: EMPTY_CV_FORM,
  });
  const createForm = createCvDraft.value;
  const [editForm, setEditForm] = useState<CvRequest>(EMPTY_CV_FORM);
  const [skillForm, setSkillForm] = useState<SkillFormState>(EMPTY_SKILL_FORM);
  const [detailForm, setDetailForm] = useState<DetailFormState>(EMPTY_DETAIL_FORM);
  const [selectedSkillKey, setSelectedSkillKey] = useState<number | null>(null);
  const [selectedDetailId, setSelectedDetailId] = useState<number | null>(null);
  const [detailTypeFilter, setDetailTypeFilter] = useState<DetailTypeFilter>('ALL');
  const [uploadFile, setUploadFile] = useState<File | null>(null);
  const [activeTab, setActiveTab] = useState<CvTab>('overview');
  const [isPreviewModalOpen, setIsPreviewModalOpen] = useState(false);
  const [previewBlobUrl, setPreviewBlobUrl] = useState('');
  const [previewLoading, setPreviewLoading] = useState(false);
  const [previewError, setPreviewError] = useState('');
  const [loading, setLoading] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [catalogLoading, setCatalogLoading] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const currentCvFileUrl = useMemo(() => {
    const selectedFileUrl = resolveCvFileUrl(selectedCv?.fileCvUrl);
    if (selectedFileUrl) return selectedFileUrl;
    return resolveCvFileUrl(editForm.fileCvUrl);
  }, [selectedCv?.fileCvUrl, editForm.fileCvUrl]);

  useEffect(() => {
    return () => {
      if (previewBlobUrl) {
        URL.revokeObjectURL(previewBlobUrl);
      }
    };
  }, [previewBlobUrl]);

  const closePreviewModal = useCallback(() => {
    setIsPreviewModalOpen(false);
    setPreviewLoading(false);
    setPreviewError('');
    if (previewBlobUrl) {
      URL.revokeObjectURL(previewBlobUrl);
      setPreviewBlobUrl('');
    }
  }, [previewBlobUrl]);

  const fetchSkillCatalog = useCallback(async () => {
    setCatalogLoading(true);
    try {
      const data = await lookupService.getSkills();
      setSkillOptions(data);
    } catch {
      setSkillOptions([]);
    } finally {
      setCatalogLoading(false);
    }
  }, []);

  useEffect(() => {
    void fetchSkillCatalog();
  }, [fetchSkillCatalog]);

  const fetchCvList = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await cvService.getMyCvs();
      setCvs(data);

      if (data.length === 0) {
        setSelectedCvId(null);
      } else if (!selectedCvId || !data.some((cv) => cv.id === selectedCvId)) {
        setSelectedCvId(data[0].id);
      }
    } catch (err) {
      setError(mapError(err, 'Không thể tải danh sách CV.'));
    } finally {
      setLoading(false);
    }
  }, [selectedCvId]);

  useEffect(() => {
    void fetchCvList();
  }, [fetchCvList]);

  const fetchCvBundle = useCallback(async (cvId: number) => {
    setDetailLoading(true);
    try {
      const cvDetailsPromise =
        detailTypeFilter === 'ALL'
          ? cvService.getCvDetails(cvId)
          : cvService.getCvDetailsByType(cvId, detailTypeFilter);

      const [cvDetail, cvSkills, cvDetails] = await Promise.all([
        cvService.getCvById(cvId),
        cvService.getCvSkills(cvId),
        cvDetailsPromise,
      ]);
      setSelectedCv(cvDetail);
      setSkills(cvSkills);
      setDetails(cvDetails);
      setEditForm({
        tieuDeCv: cvDetail.tieuDeCv,
        mucTieuNgheNghiep: cvDetail.mucTieuNgheNghiep || '',
        fileCvUrl: cvDetail.fileCvUrl || '',
      });
      setSelectedSkillKey(null);
      setSelectedDetailId(null);
      setSkillForm(EMPTY_SKILL_FORM);
      setDetailForm(EMPTY_DETAIL_FORM);
    } catch (err) {
      setError(mapError(err, 'Không thể tải dữ liệu chi tiết CV.'));
    } finally {
      setDetailLoading(false);
    }
  }, [detailTypeFilter]);

  useEffect(() => {
    if (!selectedCvId) {
      setSelectedCv(null);
      setSkills([]);
      setDetails([]);
      return;
    }
    void fetchCvBundle(selectedCvId);
  }, [selectedCvId, fetchCvBundle]);

  const handleCreateCv = async () => {
    if (!createForm.tieuDeCv.trim()) {
      setError('Tiêu đề CV không được để trống.');
      return;
    }

    setSaving(true);
    setMessage('');
    setError('');
    try {
      const created = await cvService.createCv(normalizeCvPayload({ ...createForm, fileCvUrl: '' }));
      createCvDraft.clearDraft(EMPTY_CV_FORM);
      setMessage('Tạo CV thành công.');
      await fetchCvList();
      setSelectedCvId(created.id);
      setActiveTab('profile');
    } catch (err) {
      setError(mapError(err, 'Không thể tạo CV mới.'));
    } finally {
      setSaving(false);
    }
  };

  const handlePickSkill = (row: CvSkillItem) => {
    setSelectedSkillKey(row.kyNangId);
    setSkillForm({
      kyNangId: String(row.kyNangId),
      mucThanhThao: String(row.mucThanhThao),
      moTa: row.moTa || '',
    });
  };

  const handleResetSkillForm = () => {
    setSelectedSkillKey(null);
    setSkillForm(EMPTY_SKILL_FORM);
  };

  const buildSkillPayload = (): CvSkillRequest | null => {
    const kyNangId = Number(skillForm.kyNangId);
    const mucThanhThao = Number(skillForm.mucThanhThao);

    if (!Number.isInteger(kyNangId) || kyNangId <= 0) {
      setError('Vui lòng chọn kỹ năng hợp lệ.');
      return null;
    }
    if (!Number.isInteger(mucThanhThao) || mucThanhThao < 1 || mucThanhThao > 5) {
      setError('Mức thành thạo phải từ 1 đến 5.');
      return null;
    }

    return {
      kyNangId,
      mucThanhThao,
      moTa: skillForm.moTa.trim() || undefined,
    };
  };

  const handleAddSkill = async () => {
    if (!selectedCvId) return;
    const payload = buildSkillPayload();
    if (!payload) return;

    setSaving(true);
    setMessage('');
    setError('');
    try {
      await cvService.addCvSkill(selectedCvId, payload);
      setMessage('Thêm kỹ năng vào CV thành công.');
      handleResetSkillForm();
      await fetchCvBundle(selectedCvId);
    } catch (err) {
      setError(mapError(err, 'Không thể thêm kỹ năng cho CV.'));
    } finally {
      setSaving(false);
    }
  };

  const handleUpdateSkill = async () => {
    if (!selectedCvId || !selectedSkillKey) {
      setError('Hãy chọn một kỹ năng để cập nhật.');
      return;
    }

    const payload = buildSkillPayload();
    if (!payload) return;

    setSaving(true);
    setMessage('');
    setError('');
    try {
      await cvService.updateCvSkill(selectedCvId, selectedSkillKey, {
        ...payload,
        kyNangId: selectedSkillKey,
      });
      setMessage('Cập nhật kỹ năng thành công.');
      await fetchCvBundle(selectedCvId);
    } catch (err) {
      setError(mapError(err, 'Không thể cập nhật kỹ năng trong CV.'));
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteSkill = async () => {
    if (!selectedCvId || !selectedSkillKey) {
      setError('Hãy chọn một kỹ năng để xóa.');
      return;
    }

    const confirmed = window.confirm('Bạn có chắc muốn xóa kỹ năng khỏi CV này?');
    if (!confirmed) return;

    setSaving(true);
    setMessage('');
    setError('');
    try {
      await cvService.deleteCvSkill(selectedCvId, selectedSkillKey);
      setMessage('Xóa kỹ năng thành công.');
      handleResetSkillForm();
      await fetchCvBundle(selectedCvId);
    } catch (err) {
      setError(mapError(err, 'Không thể xóa kỹ năng khỏi CV.'));
    } finally {
      setSaving(false);
    }
  };

  const handlePickDetail = (row: CvDetailItem) => {
    setSelectedDetailId(row.id);
    setDetailForm({
      loaiBanGhi: row.loaiBanGhi,
      tenToChuc: row.tenToChuc,
      chuyenNganhHoacViTri: row.chuyenNganhHoacViTri || '',
      ngayBatDau: row.ngayBatDau || '',
      ngayKetThuc: row.ngayKetThuc || '',
      moTaChiTiet: row.moTaChiTiet || '',
    });
  };

  const handleResetDetailForm = () => {
    setSelectedDetailId(null);
    setDetailForm(EMPTY_DETAIL_FORM);
  };

  const buildDetailPayload = (): CvDetailRequest | null => {
    if (!detailForm.tenToChuc.trim()) {
      setError('Tên tổ chức không được để trống.');
      return null;
    }
    if (!detailForm.ngayBatDau) {
      setError('Ngày bắt đầu không được để trống.');
      return null;
    }

    return {
      loaiBanGhi: detailForm.loaiBanGhi,
      tenToChuc: detailForm.tenToChuc.trim(),
      chuyenNganhHoacViTri: detailForm.chuyenNganhHoacViTri.trim() || undefined,
      ngayBatDau: detailForm.ngayBatDau,
      ngayKetThuc: detailForm.ngayKetThuc || undefined,
      moTaChiTiet: detailForm.moTaChiTiet.trim() || undefined,
    };
  };

  const handleAddDetail = async () => {
    if (!selectedCvId) return;
    const payload = buildDetailPayload();
    if (!payload) return;

    setSaving(true);
    setMessage('');
    setError('');
    try {
      await cvService.addCvDetail(selectedCvId, payload);
      setMessage('Thêm chi tiết CV thành công.');
      handleResetDetailForm();
      await fetchCvBundle(selectedCvId);
    } catch (err) {
      setError(mapError(err, 'Không thể thêm chi tiết CV.'));
    } finally {
      setSaving(false);
    }
  };

  const handleUpdateDetail = async () => {
    if (!selectedCvId || !selectedDetailId) {
      setError('Hãy chọn một bản ghi để cập nhật.');
      return;
    }

    const payload = buildDetailPayload();
    if (!payload) return;

    setSaving(true);
    setMessage('');
    setError('');
    try {
      await cvService.updateCvDetail(selectedCvId, selectedDetailId, payload);
      setMessage('Cập nhật chi tiết CV thành công.');
      await fetchCvBundle(selectedCvId);
    } catch (err) {
      setError(mapError(err, 'Không thể cập nhật chi tiết CV.'));
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteDetail = async () => {
    if (!selectedCvId || !selectedDetailId) {
      setError('Hãy chọn một bản ghi để xóa.');
      return;
    }

    const confirmed = window.confirm('Bạn có chắc muốn xóa bản ghi chi tiết này?');
    if (!confirmed) return;

    setSaving(true);
    setMessage('');
    setError('');
    try {
      await cvService.deleteCvDetail(selectedCvId, selectedDetailId);
      setMessage('Xóa chi tiết CV thành công.');
      handleResetDetailForm();
      await fetchCvBundle(selectedCvId);
    } catch (err) {
      setError(mapError(err, 'Không thể xóa chi tiết CV.'));
    } finally {
      setSaving(false);
    }
  };

  const handleUpdateCv = async () => {
    if (!selectedCvId) return;
    if (!editForm.tieuDeCv.trim()) {
      setError('Tiêu đề CV không được để trống.');
      return;
    }

    setSaving(true);
    setMessage('');
    setError('');
    try {
      await cvService.updateCv(selectedCvId, normalizeCvPayload(editForm));
      setMessage('Cập nhật CV thành công.');
      await fetchCvList();
      await fetchCvBundle(selectedCvId);
    } catch (err) {
      setError(mapError(err, 'Không thể cập nhật CV.'));
    } finally {
      setSaving(false);
    }
  };

  const handleSetDefaultCv = async () => {
    if (!selectedCvId) return;

    setSaving(true);
    setMessage('');
    setError('');
    try {
      await cvService.setDefaultCv(selectedCvId);
      setMessage('Đặt CV chính thành công.');
      await fetchCvList();
      await fetchCvBundle(selectedCvId);
    } catch (err) {
      setError(mapError(err, 'Không thể đặt CV chính.'));
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteCv = async () => {
    if (!selectedCvId) return;

    const confirmed = window.confirm('Bạn có chắc muốn xóa CV này?');
    if (!confirmed) return;

    setSaving(true);
    setMessage('');
    setError('');
    try {
      await cvService.deleteCv(selectedCvId);
      setMessage('Xóa CV thành công.');
      await fetchCvList();
    } catch (err) {
      setError(mapError(err, 'Không thể xóa CV.'));
    } finally {
      setSaving(false);
    }
  };

  const handleUploadFile = async () => {
    if (!selectedCvId || !uploadFile) {
      setError('Vui lòng chọn file PDF trước khi upload.');
      return;
    }

    setSaving(true);
    setMessage('');
    setError('');
    try {
      await cvService.uploadCvFile(selectedCvId, uploadFile);
      setUploadFile(null);
      setMessage('Upload file CV thành công.');
      await fetchCvBundle(selectedCvId);
      await fetchCvList();
    } catch (err) {
      setError(mapError(err, 'Không thể upload file CV.'));
    } finally {
      setSaving(false);
    }
  };

  const handleOpenCvPreview = async () => {
    if (!currentCvFileUrl) {
      setError('CV này chưa có file upload để xem review.');
      return;
    }

    const previewUrl = toSameOriginPreviewUrl(currentCvFileUrl);

    if (previewBlobUrl) {
      URL.revokeObjectURL(previewBlobUrl);
      setPreviewBlobUrl('');
    }

    setIsPreviewModalOpen(true);
    setPreviewLoading(true);
    setPreviewError('');

    try {
      const response = await fetch(previewUrl);
      if (!response.ok) {
        throw new Error(`Preview request failed with status ${response.status}`);
      }

      const pdfBlob = await response.blob();
      const blobUrl = URL.createObjectURL(pdfBlob);
      setPreviewBlobUrl(blobUrl);
    } catch {
      setPreviewError('Không thể preview trực tiếp trong modal. Vui lòng bấm mở file ở tab mới.');
    } finally {
      setPreviewLoading(false);
    }
  };

  const cvColumns: AppDataColumn<CvItem>[] = useMemo(
    () => [
      {
        key: 'tieuDeCv',
        header: 'CV',
        render: (row) => (
          <div style={{ display: 'grid', gap: 4 }}>
            <strong>{row.tieuDeCv}</strong>
            <span className={s.meta}>Cập nhật: {formatDate(row.ngayCapNhat)}</span>
          </div>
        ),
      },
      {
        key: 'laCvChinh',
        header: 'Mặc định',
        width: '120px',
        render: (row) => (row.laCvChinh ? 'CV chính' : 'Phụ'),
      },
      {
        key: 'actions',
        header: 'Tác vụ',
        width: '140px',
        align: 'center',
        render: (row) => (
          <button
            type="button"
            className={`${s.btn} ${s.btnGhost}`}
            onClick={() => {
              setSelectedCvId(row.id);
              setActiveTab('profile');
            }}
          >
            Chọn
          </button>
        ),
      },
    ],
    []
  );

  const tabItems: Array<{ key: CvTab; label: string; hint: string }> = [
    { key: 'overview', label: 'Tổng quan', hint: 'Quản lý danh sách CV và tạo CV mới' },
    { key: 'profile', label: 'Thông tin CV', hint: 'Chỉnh tiêu đề, mục tiêu, upload file và CV chính' },
    { key: 'skills', label: 'Kỹ năng', hint: 'Quản lý kỹ năng với mức thành thạo trực quan' },
    { key: 'details', label: 'Học vấn/Kinh nghiệm', hint: 'Nhóm timeline theo từng loại bản ghi' },
  ];

  const activeStepIndex = tabItems.findIndex((tab) => tab.key === activeTab);

  const groupedDetails = useMemo(() => {
    const groups: Record<LoaiBanGhiCv, CvDetailItem[]> = {
      1: [],
      2: [],
      3: [],
    };

    details.forEach((item) => {
      groups[item.loaiBanGhi].push(item);
    });

    ([1, 2, 3] as LoaiBanGhiCv[]).forEach((key) => {
      groups[key].sort((a, b) => new Date(b.ngayBatDau).getTime() - new Date(a.ngayBatDau).getTime());
    });

    return groups;
  }, [details]);

  const skillAverage = useMemo(() => {
    if (skills.length === 0) return 0;
    const total = skills.reduce((sum, item) => sum + item.mucThanhThao, 0);
    return Math.round((total / skills.length) * 10) / 10;
  }, [skills]);

  return (
    <MainLayout title="Quản lý CV" breadcrumb="Trang chủ / Ứng viên / Quản lý CV">
      <div className={s.stack}>
        <div className={s.topBar}>
          <div className={s.tags}>
            <span className={s.tag}>Tổng CV: {cvs.length}</span>
            {selectedCv?.laCvChinh ? <span className={s.tag}>CV đang chọn là CV chính</span> : null}
          </div>
          <button type="button" className={`${s.btn} ${s.btnGhost}`} onClick={() => void fetchCvList()}>
            Làm mới
          </button>
        </div>

        {loading ? <div className={s.alert}>Đang tải danh sách CV...</div> : null}
        {message ? <div className={`${s.alert} ${s.alertSuccess}`}>{message}</div> : null}
        {error ? <div className={`${s.alert} ${s.alertError}`}>{error}</div> : null}

        <div className={t.tabsWrap}>
          <div className={t.tabs}>
            {tabItems.map((tab) => (
              <button
                key={tab.key}
                type="button"
                className={`${t.tabBtn} ${activeTab === tab.key ? t.tabBtnActive : ''}`}
                onClick={() => setActiveTab(tab.key)}
              >
                {tab.label}
              </button>
            ))}
          </div>
          <p className={t.tabHint}>{tabItems.find((tab) => tab.key === activeTab)?.hint}</p>

          <div className={t.stepper}>
            {tabItems.map((tab, index) => (
              <article key={tab.key} className={`${t.step} ${index <= activeStepIndex ? t.stepActive : ''}`}>
                <span className={t.stepIndex}>{index + 1}</span>
                <p className={t.stepTitle}>{tab.label}</p>
                <p className={t.stepSub}>{tab.hint}</p>
              </article>
            ))}
          </div>
        </div>

        {activeTab === 'overview' ? (
          <div className={s.grid2}>
            <section className={s.card}>
              <h3 className={s.cardTitle}>Danh sách CV của bạn</h3>
              <AppDataTable
                columns={cvColumns}
                data={cvs}
                rowKey={(row) => String(row.id)}
                emptyMessage="Bạn chưa có CV nào."
              />
            </section>

            <section className={s.card}>
              <h3 className={s.cardTitle}>Tạo CV mới</h3>
              <div className={s.field}>
                <label className={s.label}>Tiêu đề CV</label>
                <input
                  className={s.input}
                  value={createForm.tieuDeCv}
                  onChange={(e) =>
                    createCvDraft.setValue((prev) => ({ ...prev, tieuDeCv: e.target.value }))
                  }
                  placeholder="Ví dụ: CV Frontend React"
                />
              </div>
              <div className={s.field}>
                <label className={s.label}>Mục tiêu nghề nghiệp</label>
                <textarea
                  className={s.textarea}
                  value={createForm.mucTieuNgheNghiep}
                  onChange={(e) =>
                    createCvDraft.setValue((prev) => ({ ...prev, mucTieuNgheNghiep: e.target.value }))
                  }
                  placeholder="Mô tả ngắn mục tiêu nghề nghiệp"
                />
              </div>
              <div className={s.alert}>File CV sẽ được upload ở tab Thông tin CV sau khi tạo CV.</div>
              <div className={s.actions}>
                <button
                  type="button"
                  className={`${s.btn} ${s.btnGhost}`}
                  disabled={saving || !createCvDraft.canUndo}
                  onClick={createCvDraft.undo}
                >
                  Undo
                </button>
                <button
                  type="button"
                  className={`${s.btn} ${s.btnGhost}`}
                  disabled={saving || !createCvDraft.canRedo}
                  onClick={createCvDraft.redo}
                >
                  Redo
                </button>
                <button
                  type="button"
                  className={`${s.btn} ${s.btnGhost}`}
                  disabled={saving}
                  onClick={() => createCvDraft.clearDraft(EMPTY_CV_FORM)}
                >
                  Xóa nháp
                </button>
                <button
                  type="button"
                  className={`${s.btn} ${s.btnPrimary}`}
                  disabled={saving}
                  onClick={() => void handleCreateCv()}
                >
                  Tạo CV
                </button>
              </div>
            </section>
          </div>
        ) : null}

        {activeTab === 'profile' ? (
          <section className={s.card}>
            <h3 className={s.cardTitle}>Chỉnh sửa CV đang chọn</h3>
            {!selectedCvId ? <div className={s.alert}>Hãy tạo hoặc chọn một CV để chỉnh sửa.</div> : null}
            {detailLoading ? <div className={s.alert}>Đang tải chi tiết CV...</div> : null}

            {selectedCvId && !detailLoading ? (
              <>
                <div className={s.field}>
                  <label className={s.label}>Tiêu đề CV</label>
                  <input
                    className={s.input}
                    value={editForm.tieuDeCv}
                    onChange={(e) => setEditForm((prev) => ({ ...prev, tieuDeCv: e.target.value }))}
                  />
                </div>

                <div className={s.field}>
                  <label className={s.label}>Mục tiêu nghề nghiệp</label>
                  <textarea
                    className={s.textarea}
                    value={editForm.mucTieuNgheNghiep}
                    onChange={(e) => setEditForm((prev) => ({ ...prev, mucTieuNgheNghiep: e.target.value }))}
                  />
                </div>

                <div className={s.actions}>
                  <button
                    type="button"
                    className={`${s.btn} ${s.btnGhost}`}
                    disabled={saving}
                    onClick={() => void handleSetDefaultCv()}
                  >
                    Đặt làm CV chính
                  </button>
                  <button
                    type="button"
                    className={`${s.btn} ${s.btnDanger}`}
                    disabled={saving}
                    onClick={() => void handleDeleteCv()}
                  >
                    Xóa CV
                  </button>
                  <button
                    type="button"
                    className={`${s.btn} ${s.btnPrimary}`}
                    disabled={saving}
                    onClick={() => void handleUpdateCv()}
                  >
                    Lưu thay đổi
                  </button>
                </div>

                <div className={s.field}>
                  <label className={s.label}>Upload file PDF CV</label>
                  <input
                    className={s.input}
                    type="file"
                    accept="application/pdf"
                    onChange={(e) => setUploadFile(e.target.files?.[0] ?? null)}
                  />
                  <div className={s.actions}>
                    <button
                      type="button"
                      className={`${s.btn} ${s.btnGhost}`}
                      disabled={saving || !uploadFile}
                      onClick={() => void handleUploadFile()}
                    >
                      Upload file
                    </button>
                    <button
                      type="button"
                      className={`${s.btn} ${s.btnPrimary}`}
                      disabled={!currentCvFileUrl}
                      onClick={() => void handleOpenCvPreview()}
                    >
                      Xem review file CV
                    </button>
                    {currentCvFileUrl ? (
                      <a className={s.inlineLink} href={currentCvFileUrl} target="_blank" rel="noreferrer">
                        Mở tab mới
                      </a>
                    ) : (
                      <span className={s.meta}>Chưa có file CV đã upload.</span>
                    )}
                  </div>
                </div>
              </>
            ) : null}
          </section>
        ) : null}

        {activeTab === 'skills' ? (
          <section className={s.card}>
            <h3 className={s.cardTitle}>Kỹ năng trong CV</h3>
            {!selectedCvId ? <div className={s.alert}>Hãy chọn một CV ở tab Tổng quan trước.</div> : null}
            {selectedCvId && detailLoading ? <div className={s.alert}>Đang tải dữ liệu kỹ năng...</div> : null}

            {selectedCvId && !detailLoading ? (
              <>
                <div className={s.tags}>
                  <span className={s.tag}>Tổng kỹ năng: {skills.length}</span>
                  <span className={s.tag}>Mức trung bình: {skillAverage}/5</span>
                </div>

                <AppDataTable
                  columns={[
                    {
                      key: 'tenKyNang',
                      header: 'Kỹ năng',
                      render: (row: CvSkillItem) => (
                        <div className={s.skillCell}>
                          <strong>{row.tenKyNang}</strong>
                          <span className={s.meta}>Mức thành thạo: {row.mucThanhThao}/5</span>
                          <div className={s.skillMeterTrack}>
                            <span className={s.skillMeterFill} style={{ width: `${row.mucThanhThao * 20}%` }} />
                          </div>
                        </div>
                      ),
                    },
                    {
                      key: 'moTa',
                      header: 'Mô tả',
                      render: (row: CvSkillItem) => row.moTa || '-',
                    },
                    {
                      key: 'actions',
                      header: 'Tác vụ',
                      width: '120px',
                      align: 'center',
                      render: (row: CvSkillItem) => (
                        <button
                          type="button"
                          className={`${s.btn} ${s.btnGhost}`}
                          onClick={() => handlePickSkill(row)}
                        >
                          Sửa
                        </button>
                      ),
                    },
                  ]}
                  data={skills}
                  rowKey={(row) => `${row.cvId}-${row.kyNangId}`}
                  emptyMessage="CV này chưa có kỹ năng nào."
                />

                <div className={s.grid2}>
                  <div className={s.field}>
                    <label className={s.label}>Kỹ năng</label>
                    <select
                      className={s.select}
                      disabled={selectedSkillKey !== null}
                      value={skillForm.kyNangId}
                      onChange={(e) => setSkillForm((prev) => ({ ...prev, kyNangId: e.target.value }))}
                    >
                      <option value="">Chọn kỹ năng</option>
                      {skillOptions.map((skill) => (
                        <option key={skill.value} value={skill.value}>{skill.label}</option>
                      ))}
                    </select>
                  </div>
                  <div className={s.field}>
                    <label className={s.label}>Mức thành thạo</label>
                    <select
                      className={s.select}
                      value={skillForm.mucThanhThao}
                      onChange={(e) => setSkillForm((prev) => ({ ...prev, mucThanhThao: e.target.value }))}
                    >
                      <option value="1">1 - Sơ cấp</option>
                      <option value="2">2 - Cơ bản</option>
                      <option value="3">3 - Trung bình</option>
                      <option value="4">4 - Nâng cao</option>
                      <option value="5">5 - Chuyên gia</option>
                    </select>
                  </div>
                </div>

                <div className={s.field}>
                  <label className={s.label}>Mô tả kỹ năng</label>
                  <textarea
                    className={s.textarea}
                    value={skillForm.moTa}
                    onChange={(e) => setSkillForm((prev) => ({ ...prev, moTa: e.target.value }))}
                    placeholder="Mô tả ngắn cách bạn sử dụng kỹ năng này"
                  />
                </div>

                <div className={s.actions}>
                  <button
                    type="button"
                    className={`${s.btn} ${s.btnGhost}`}
                    disabled={saving}
                    onClick={handleResetSkillForm}
                  >
                    Làm mới form
                  </button>
                  <button
                    type="button"
                    className={`${s.btn} ${s.btnPrimary}`}
                    disabled={saving || catalogLoading}
                    onClick={() => void handleAddSkill()}
                  >
                    Thêm kỹ năng
                  </button>
                  <button
                    type="button"
                    className={`${s.btn} ${s.btnGhost}`}
                    disabled={saving || selectedSkillKey === null}
                    onClick={() => void handleUpdateSkill()}
                  >
                    Cập nhật kỹ năng
                  </button>
                  <button
                    type="button"
                    className={`${s.btn} ${s.btnDanger}`}
                    disabled={saving || selectedSkillKey === null}
                    onClick={() => void handleDeleteSkill()}
                  >
                    Xóa kỹ năng
                  </button>
                </div>
              </>
            ) : null}
          </section>
        ) : null}

        {activeTab === 'details' ? (
          <div className={s.stack}>
            <section className={s.card}>
              <h3 className={s.cardTitle}>Nhóm học vấn/kinh nghiệm/chứng chỉ</h3>
              {!selectedCvId ? <div className={s.alert}>Hãy chọn một CV ở tab Tổng quan trước.</div> : null}
              {selectedCvId && detailLoading ? <div className={s.alert}>Đang tải timeline chi tiết...</div> : null}

              {selectedCvId && !detailLoading ? (
                <div className={s.groupGrid}>
                  {([1, 2, 3] as LoaiBanGhiCv[]).map((type) => (
                    <article key={type} className={s.groupCard}>
                      <div className={s.topBar}>
                        <strong>{loaiBanGhiLabel(type)}</strong>
                        <span className={s.tag}>{groupedDetails[type].length}</span>
                      </div>

                      {groupedDetails[type].length === 0 ? (
                        <span className={s.meta}>Chưa có bản ghi.</span>
                      ) : (
                        <div className={s.timelineList}>
                          {groupedDetails[type].slice(0, 3).map((item) => (
                            <div key={item.id} className={s.timelineItem}>
                              <strong>{item.tenToChuc}</strong>
                              <span className={s.meta}>{item.chuyenNganhHoacViTri || '-'}</span>
                              <span className={s.timelineTime}>{item.ngayBatDau} - {item.ngayKetThuc || 'Hiện tại'}</span>
                            </div>
                          ))}
                        </div>
                      )}
                    </article>
                  ))}
                </div>
              ) : null}
            </section>

            <section className={s.card}>
              <h3 className={s.cardTitle}>Bản ghi chi tiết</h3>
              <div className={s.topBar}>
                <div className={s.tags}>
                  <span className={s.tag}>Bản ghi: {details.length}</span>
                </div>
                <div className={s.field} style={{ minWidth: 220 }}>
                  <label className={s.label}>Lọc theo loại</label>
                  <select
                    className={s.select}
                    value={String(detailTypeFilter)}
                    onChange={(e) => {
                      const value = e.target.value;
                      setDetailTypeFilter(value === 'ALL' ? 'ALL' : (Number(value) as LoaiBanGhiCv));
                      setSelectedDetailId(null);
                    }}
                  >
                    <option value="ALL">Tất cả</option>
                    <option value="1">Học vấn</option>
                    <option value="2">Kinh nghiệm</option>
                    <option value="3">Chứng chỉ</option>
                  </select>
                </div>
              </div>

              <AppDataTable
                columns={[
                  {
                    key: 'loaiBanGhi',
                    header: 'Loại',
                    width: '130px',
                    render: (row: CvDetailItem) => loaiBanGhiLabel(row.loaiBanGhi),
                  },
                  {
                    key: 'tenToChuc',
                    header: 'Tổ chức',
                    render: (row: CvDetailItem) => (
                      <div style={{ display: 'grid', gap: 4 }}>
                        <strong>{row.tenToChuc}</strong>
                        <span className={s.meta}>{row.chuyenNganhHoacViTri || '-'}</span>
                      </div>
                    ),
                  },
                  {
                    key: 'ngayBatDau',
                    header: 'Thời gian',
                    width: '170px',
                    render: (row: CvDetailItem) => `${row.ngayBatDau} - ${row.ngayKetThuc || 'Hiện tại'}`,
                  },
                  {
                    key: 'actions',
                    header: 'Tác vụ',
                    width: '120px',
                    align: 'center',
                    render: (row: CvDetailItem) => (
                      <button
                        type="button"
                        className={`${s.btn} ${s.btnGhost}`}
                        onClick={() => handlePickDetail(row)}
                      >
                        Sửa
                      </button>
                    ),
                  },
                ]}
                data={details}
                rowKey={(row) => String(row.id)}
                emptyMessage="CV này chưa có chi tiết học vấn/kinh nghiệm."
              />

              <div className={s.grid2}>
                <div className={s.field}>
                  <label className={s.label}>Loại bản ghi</label>
                  <select
                    className={s.select}
                    value={String(detailForm.loaiBanGhi)}
                    onChange={(e) => setDetailForm((prev) => ({
                      ...prev,
                      loaiBanGhi: Number(e.target.value) as LoaiBanGhiCv,
                    }))}
                  >
                    <option value="1">Học vấn</option>
                    <option value="2">Kinh nghiệm</option>
                    <option value="3">Chứng chỉ</option>
                  </select>
                </div>
                <div className={s.field}>
                  <label className={s.label}>Tên tổ chức</label>
                  <input
                    className={s.input}
                    value={detailForm.tenToChuc}
                    onChange={(e) => setDetailForm((prev) => ({ ...prev, tenToChuc: e.target.value }))}
                    placeholder="Ví dụ: Đại học Bách Khoa, ABC Corp"
                  />
                </div>
              </div>

              <div className={s.grid2}>
                <div className={s.field}>
                  <label className={s.label}>Chuyên ngành/Vị trí</label>
                  <input
                    className={s.input}
                    value={detailForm.chuyenNganhHoacViTri}
                    onChange={(e) => setDetailForm((prev) => ({ ...prev, chuyenNganhHoacViTri: e.target.value }))}
                    placeholder="Ví dụ: Frontend Developer"
                  />
                </div>
                <div className={s.field}>
                  <label className={s.label}>Ngày bắt đầu</label>
                  <input
                    className={s.input}
                    type="date"
                    value={detailForm.ngayBatDau}
                    onChange={(e) => setDetailForm((prev) => ({ ...prev, ngayBatDau: e.target.value }))}
                  />
                </div>
              </div>

              <div className={s.grid2}>
                <div className={s.field}>
                  <label className={s.label}>Ngày kết thúc</label>
                  <input
                    className={s.input}
                    type="date"
                    value={detailForm.ngayKetThuc}
                    onChange={(e) => setDetailForm((prev) => ({ ...prev, ngayKetThuc: e.target.value }))}
                  />
                </div>
                <div className={s.field}>
                  <label className={s.label}>Mô tả chi tiết</label>
                  <textarea
                    className={s.textarea}
                    value={detailForm.moTaChiTiet}
                    onChange={(e) => setDetailForm((prev) => ({ ...prev, moTaChiTiet: e.target.value }))}
                  />
                </div>
              </div>

              <div className={s.actions}>
                <button
                  type="button"
                  className={`${s.btn} ${s.btnGhost}`}
                  disabled={saving}
                  onClick={handleResetDetailForm}
                >
                  Làm mới form
                </button>
                <button
                  type="button"
                  className={`${s.btn} ${s.btnPrimary}`}
                  disabled={saving}
                  onClick={() => void handleAddDetail()}
                >
                  Thêm bản ghi
                </button>
                <button
                  type="button"
                  className={`${s.btn} ${s.btnGhost}`}
                  disabled={saving || selectedDetailId === null}
                  onClick={() => void handleUpdateDetail()}
                >
                  Cập nhật bản ghi
                </button>
                <button
                  type="button"
                  className={`${s.btn} ${s.btnDanger}`}
                  disabled={saving || selectedDetailId === null}
                  onClick={() => void handleDeleteDetail()}
                >
                  Xóa bản ghi
                </button>
              </div>
            </section>
          </div>
        ) : null}

        {isPreviewModalOpen && currentCvFileUrl ? (
          <div className={m.overlay} role="dialog" aria-modal="true" onClick={closePreviewModal}>
            <div className={m.modal} onClick={(event) => event.stopPropagation()}>
              <div className={m.modalHead}>
                <h4 className={m.modalTitle}>Review file CV đã upload</h4>
                <button type="button" className={m.closeBtn} onClick={closePreviewModal}>
                  Đóng
                </button>
              </div>

              {previewLoading ? <div className={s.alert}>Đang tải nội dung file để preview...</div> : null}
              {previewError ? <div className={`${s.alert} ${s.alertError}`}>{previewError}</div> : null}
              {!previewLoading && previewBlobUrl ? (
                <iframe
                  title="CV preview"
                  src={previewBlobUrl}
                  style={{ width: '100%', height: '70vh', border: '1px solid #dbe4f2', borderRadius: 12 }}
                />
              ) : null}

              <div className={s.actions}>
                <a className={s.inlineLink} href={currentCvFileUrl} target="_blank" rel="noreferrer">
                  Nếu không hiển thị, bấm mở file ở tab mới
                </a>
              </div>
            </div>
          </div>
        ) : null}
      </div>
    </MainLayout>
  );
}
