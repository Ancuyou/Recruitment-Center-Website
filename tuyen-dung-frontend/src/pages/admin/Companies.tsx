import { useCallback, useEffect, useMemo, useState } from 'react';
import MainLayout from '@/layouts/MainLayout';
import AppDataTable, { type AppDataColumn } from '@/components/common/AppDataTable';
import AdminRoleNav from '@/components/admin/AdminRoleNav';
import { useDraftHistory } from '@/hooks/useDraftHistory';
import { companyService } from '@/services/modules/company.module';
import type { CompanyItem, CompanyRequest } from '@/types/company.types';
import a from '@/assets/styles/admin-console.module.css';

function mapError(error: unknown): string {
  return (
    (error as { response?: { data?: { message?: string } } })?.response?.data?.message ??
    'Không thể tải dữ liệu công ty.'
  );
}

function toRequestPayload(form: CompanyRequest): CompanyRequest {
  return {
    tenCongTy: form.tenCongTy.trim(),
    maSoThue: form.maSoThue?.trim() || undefined,
    logoUrl: form.logoUrl?.trim() || undefined,
    website: form.website?.trim() || undefined,
    moTa: form.moTa?.trim() || undefined,
  };
}

const EMPTY_FORM: CompanyRequest = {
  tenCongTy: '',
  maSoThue: '',
  logoUrl: '',
  website: '',
  moTa: '',
};

const TAX_CODE_REGEX = /^\d{10,14}$/;

export default function AdminCompaniesPage() {
  const [companies, setCompanies] = useState<CompanyItem[]>([]);
  const [keyword, setKeyword] = useState('');
  const [selectedCompanyId, setSelectedCompanyId] = useState<number | null>(null);
  const [companyIndustries, setCompanyIndustries] = useState<string[]>([]);
  const {
    value: form,
    setValue: setCompanyForm,
    replaceValue: replaceCompanyForm,
    undo: undoCompanyForm,
    redo: redoCompanyForm,
    clearDraft: clearCompanyFormDraft,
    canUndo: canUndoCompanyForm,
    canRedo: canRedoCompanyForm,
  } = useDraftHistory<CompanyRequest>({
    storageKey: `draft.admin.companies.${selectedCompanyId ?? 'none'}`,
    initialValue: EMPTY_FORM,
  });
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const selectedCompany = useMemo(
    () => companies.find((item) => item.id === selectedCompanyId) ?? null,
    [companies, selectedCompanyId]
  );

  const visibleCompanies = useMemo(() => {
    const normalized = keyword.trim().toLowerCase();
    if (!normalized) return companies;
    return companies.filter((company) =>
      [company.tenCongTy, company.maSoThue, company.website]
        .filter(Boolean)
        .join(' ')
        .toLowerCase()
        .includes(normalized)
    );
  }, [companies, keyword]);

  const fetchCompanies = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await companyService.getAllCompanies();
      setCompanies(data);
      if (!selectedCompanyId || !data.some((item) => item.id === selectedCompanyId)) {
        setSelectedCompanyId(data[0]?.id ?? null);
      }
    } catch (err) {
      setError(mapError(err));
    } finally {
      setLoading(false);
    }
  }, [selectedCompanyId]);

  useEffect(() => {
    void fetchCompanies();
  }, [fetchCompanies]);

  const loadCompanyBundle = useCallback(async (companyId: number) => {
    setLoading(true);
    setError('');
    try {
      const [company, industries] = await Promise.all([
        companyService.getCompanyById(companyId),
        companyService.getCompanyIndustries(companyId),
      ]);

      replaceCompanyForm({
        tenCongTy: company.tenCongTy || '',
        maSoThue: company.maSoThue || '',
        logoUrl: company.logoUrl || '',
        website: company.website || '',
        moTa: company.moTa || '',
      });
      setCompanyIndustries(industries);
    } catch (err) {
      setError(mapError(err));
      setCompanyIndustries([]);
      clearCompanyFormDraft(EMPTY_FORM);
    } finally {
      setLoading(false);
    }
  }, [clearCompanyFormDraft, replaceCompanyForm]);

  useEffect(() => {
    if (!selectedCompanyId) {
      clearCompanyFormDraft(EMPTY_FORM);
      setCompanyIndustries([]);
      return;
    }
    void loadCompanyBundle(selectedCompanyId);
  }, [selectedCompanyId, loadCompanyBundle, clearCompanyFormDraft]);

  const handlePickCompany = (company: CompanyItem) => {
    setSelectedCompanyId(company.id);
    setMessage('');
    setError('');
  };

  const handleUpdateCompany = async () => {
    if (!selectedCompanyId) {
      setError('Hãy chọn công ty cần cập nhật.');
      return;
    }
    if (!form.tenCongTy.trim()) {
      setError('Tên công ty không được để trống.');
      return;
    }

    const normalizedTaxCode = form.maSoThue?.trim() || '';
    if (normalizedTaxCode && !TAX_CODE_REGEX.test(normalizedTaxCode)) {
      setError('Mã số thuế phải gồm 10-14 chữ số.');
      return;
    }

    setSaving(true);
    setError('');
    setMessage('');
    try {
      await companyService.updateCompany(selectedCompanyId, {
        ...toRequestPayload(form),
        maSoThue: normalizedTaxCode || undefined,
      });
      setMessage('Cập nhật thông tin công ty thành công.');
      await fetchCompanies();
      await loadCompanyBundle(selectedCompanyId);
    } catch (err) {
      setError(mapError(err));
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteCompany = async (companyId: number) => {
    const confirmed = window.confirm('Bạn có chắc muốn xóa công ty này?');
    if (!confirmed) return;

    setSaving(true);
    setError('');
    setMessage('');
    try {
      await companyService.deleteCompany(companyId);
      setMessage('Xóa công ty thành công.');
      if (selectedCompanyId === companyId) {
        setSelectedCompanyId(null);
      }
      await fetchCompanies();
    } catch (err) {
      setError(mapError(err));
    } finally {
      setSaving(false);
    }
  };

  const columns: AppDataColumn<CompanyItem>[] = [
    {
      key: 'tenCongTy',
      header: 'Công ty',
      render: (row) => (
        <div style={{ display: 'grid', gap: 4 }}>
          <strong>{row.tenCongTy}</strong>
          <span className={a.muted}>MST: {row.maSoThue || 'Chưa cập nhật'}</span>
        </div>
      ),
    },
    {
      key: 'soTinDangMo',
      header: 'Tin đang mở',
      width: '120px',
      render: (row) => row.soTinDangMo,
    },
    {
      key: 'actions',
      header: 'Tác vụ',
      width: '220px',
      align: 'center',
      render: (row) => (
        <div className={a.toolbar} style={{ justifyContent: 'center' }}>
          <button type="button" className={`${a.btn} ${a.btnGhost}`} onClick={() => handlePickCompany(row)}>
            Chọn
          </button>
          <button
            type="button"
            className={`${a.btn} ${a.btnDanger}`}
            disabled={saving}
            onClick={() => void handleDeleteCompany(row.id)}
          >
            Xóa
          </button>
        </div>
      ),
    },
  ];

  return (
    <MainLayout title="Công ty" breadcrumb="Trang chủ / Admin / Công ty">
      <div className={a.page}>
        <AdminRoleNav />

        <section className={a.surface}>
          <div className={a.sectionHead}>
            <h3 className={a.sectionTitle}>Danh bạ công ty</h3>
            <div className={a.chips}>
              <span className={a.chip}>Tổng công ty: {companies.length}</span>
              {selectedCompany ? <span className={a.chip}>Đang chọn: {selectedCompany.tenCongTy}</span> : null}
            </div>
          </div>
          <div className={a.toolbar}>
            <input
              className={a.input}
              style={{ maxWidth: 320 }}
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder="Lọc nhanh theo tên công ty / MST / website"
            />
            <button type="button" className={`${a.btn} ${a.btnGhost}`} onClick={() => void fetchCompanies()}>
              Làm mới dữ liệu
            </button>
          </div>
        </section>

        {loading ? <div className={a.notice}>Đang tải dữ liệu công ty...</div> : null}
        {error ? <div className={`${a.notice} ${a.noticeError}`}>{error}</div> : null}
        {message ? <div className={`${a.notice} ${a.noticeSuccess}`}>{message}</div> : null}

        <section className={a.surface}>
          <div className={a.sectionHead}>
            <h3 className={a.sectionTitle}>Danh sách công ty</h3>
            {keyword.trim() ? <span className={a.chip}>Kết quả lọc: {visibleCompanies.length}</span> : null}
          </div>
          <AppDataTable
            columns={columns}
            data={visibleCompanies}
            rowKey={(row) => String(row.id)}
            emptyMessage="Không có dữ liệu công ty."
          />
        </section>

        <div className={a.grid2}>
          <section className={a.surface}>
            <h3 className={a.sectionTitle}>Chi tiết và cập nhật công ty</h3>
            {!selectedCompanyId ? (
              <div className={a.notice}>Chọn một công ty để cập nhật.</div>
            ) : (
              <>
                <div className={a.stack}>
                  <input
                    className={a.input}
                    value={form.tenCongTy || ''}
                    onChange={(e) =>
                      setCompanyForm((prev) => ({ ...prev, tenCongTy: e.target.value }))
                    }
                    placeholder="Tên công ty"
                  />
                  <input
                    className={a.input}
                    value={form.maSoThue || ''}
                    onChange={(e) =>
                      setCompanyForm((prev) => ({ ...prev, maSoThue: e.target.value }))
                    }
                    placeholder="Mã số thuế"
                  />
                  <input
                    className={a.input}
                    value={form.website || ''}
                    onChange={(e) =>
                      setCompanyForm((prev) => ({ ...prev, website: e.target.value }))
                    }
                    placeholder="Website"
                  />
                  <input
                    className={a.input}
                    value={form.logoUrl || ''}
                    onChange={(e) =>
                      setCompanyForm((prev) => ({ ...prev, logoUrl: e.target.value }))
                    }
                    placeholder="Logo URL"
                  />
                  <textarea
                    className={a.textarea}
                    value={form.moTa || ''}
                    onChange={(e) =>
                      setCompanyForm((prev) => ({ ...prev, moTa: e.target.value }))
                    }
                    placeholder="Mô tả công ty"
                  />
                </div>
                <div className={a.toolbar}>
                  <button
                    type="button"
                    className={`${a.btn} ${a.btnGhost}`}
                    disabled={saving || !canUndoCompanyForm}
                    onClick={undoCompanyForm}
                  >
                    Undo
                  </button>
                  <button
                    type="button"
                    className={`${a.btn} ${a.btnGhost}`}
                    disabled={saving || !canRedoCompanyForm}
                    onClick={redoCompanyForm}
                  >
                    Redo
                  </button>
                  <button
                    type="button"
                    className={`${a.btn} ${a.btnPrimary}`}
                    disabled={saving}
                    onClick={() => void handleUpdateCompany()}
                  >
                    Lưu cập nhật
                  </button>
                </div>
              </>
            )}
          </section>

          <section className={a.surface}>
            <h3 className={a.sectionTitle}>Ngành nghề của công ty</h3>
            {!selectedCompanyId ? (
              <div className={a.notice}>Chọn công ty để xem ngành nghề.</div>
            ) : companyIndustries.length === 0 ? (
              <div className={a.notice}>Công ty chưa có ngành nghề.</div>
            ) : (
              <div className={a.chips}>
                {companyIndustries.map((industry) => (
                  <span key={industry} className={a.chip}>{industry}</span>
                ))}
              </div>
            )}
          </section>
        </div>
      </div>
    </MainLayout>
  );
}
