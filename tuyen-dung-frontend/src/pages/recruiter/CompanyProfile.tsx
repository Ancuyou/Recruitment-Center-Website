import { useCallback, useEffect, useMemo, useState } from 'react';
import MainLayout from '@/layouts/MainLayout';
import { useAuthStore } from '@/store/auth.store';
import { companyService } from '@/services/modules/company.module';
import { jobService } from '@/services/modules/job.module';
import { lookupService } from '@/services/modules/lookup.module';
import type { CompanyRequest } from '@/types/company.types';
import type { LookupItem } from '@/types/lookup.types';
import s from '@/assets/styles/recruiter-workflow.module.css';

const EMPTY_FORM: CompanyRequest = {
  tenCongTy: '',
  maSoThue: '',
  logoUrl: '',
  website: '',
  moTa: '',
};

const TAX_CODE_REGEX = /^\d{10,14}$/;

function mapError(error: unknown, fallback: string): string {
  return (
    (error as { response?: { data?: { message?: string } } })?.response?.data?.message ?? fallback
  );
}

export default function RecruiterCompanyProfilePage() {
  const user = useAuthStore((state) => state.user);

  const [companyId, setCompanyId] = useState<number | null>(null);
  const [industryOptions, setIndustryOptions] = useState<LookupItem[]>([]);
  const [selectedIndustries, setSelectedIndustries] = useState<string[]>([]);
  const [form, setForm] = useState<CompanyRequest>(EMPTY_FORM);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [taxChecking, setTaxChecking] = useState(false);
  const [taxInput, setTaxInput] = useState('');
  const [taxResult, setTaxResult] = useState('');
  const [manualCompanyId, setManualCompanyId] = useState('');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const resolveCompanyId = useCallback(async (): Promise<number | null> => {
    const myJobs = await jobService.getMyJobs();
    if (myJobs.length > 0) return myJobs[0].congTyId;

    if (!user?.tenCongTy?.trim()) return null;

    const companies = await companyService.getAllCompanies();
    const normalizedName = user.tenCongTy.trim().toLowerCase();
    const matched = companies.find((company) => company.tenCongTy?.trim().toLowerCase() === normalizedName);
    return matched?.id ?? null;
  }, [user?.tenCongTy]);

  const loadCompanyData = useCallback(async (resolvedCompanyId: number | null) => {
    if (!resolvedCompanyId) {
      setCompanyId(null);
      setForm({ ...EMPTY_FORM, tenCongTy: user?.tenCongTy || '' });
      setSelectedIndustries([]);
      return;
    }

    const [company, industries] = await Promise.all([
      companyService.getCompanyById(resolvedCompanyId),
      companyService.getCompanyIndustries(resolvedCompanyId),
    ]);

    setCompanyId(company.id);
    setForm({
      tenCongTy: company.tenCongTy || '',
      maSoThue: company.maSoThue || '',
      logoUrl: company.logoUrl || '',
      website: company.website || '',
      moTa: company.moTa || '',
    });
    setTaxInput(company.maSoThue || '');
    setSelectedIndustries(industries || []);
  }, [user?.tenCongTy]);

  const fetchData = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [industries, resolvedCompanyId] = await Promise.all([
        lookupService.getIndustries(),
        resolveCompanyId(),
      ]);

      setIndustryOptions(industries);
      await loadCompanyData(resolvedCompanyId);

      if (!resolvedCompanyId) {
        setError('Không xác định được công ty liên kết từ backend. Bạn có thể nhập Company ID để tải thủ công.');
      }
    } catch (err) {
      setError(mapError(err, 'Không thể tải dữ liệu công ty.'));
    } finally {
      setLoading(false);
    }
  }, [loadCompanyData, resolveCompanyId]);

  useEffect(() => {
    void fetchData();
  }, [fetchData]);

  const handleToggleIndustry = (value: string) => {
    setSelectedIndustries((prev) =>
      prev.includes(value) ? prev.filter((item) => item !== value) : [...prev, value]
    );
  };

  const handleSave = async () => {
    if (!companyId) {
      setError('Chưa có Company ID hợp lệ để cập nhật. Hãy tải công ty thủ công hoặc đảm bảo recruiter có dữ liệu backend đầy đủ.');
      return;
    }
    if (!form.tenCongTy?.trim()) {
      setError('Tên công ty không được để trống.');
      return;
    }

    const normalizedTaxCode = form.maSoThue?.trim() || '';
    if (normalizedTaxCode && !TAX_CODE_REGEX.test(normalizedTaxCode)) {
      setError('Mã số thuế phải gồm 10-14 chữ số.');
      return;
    }

    setSaving(true);
    setMessage('');
    setError('');

    try {
      await companyService.updateCompany(companyId, {
        tenCongTy: form.tenCongTy.trim(),
        maSoThue: normalizedTaxCode || undefined,
        logoUrl: form.logoUrl?.trim() || undefined,
        website: form.website?.trim() || undefined,
        moTa: form.moTa?.trim() || undefined,
      });
      await companyService.updateCompanyIndustries(companyId, selectedIndustries);
      setMessage('Cập nhật thông tin công ty thành công.');
      await fetchData();
    } catch (err) {
      setError(mapError(err, 'Không thể cập nhật thông tin công ty.'));
    } finally {
      setSaving(false);
    }
  };

  const handleCreateCompany = async () => {
    if (!form.tenCongTy?.trim()) {
      setError('Tên công ty không được để trống.');
      return;
    }

    const normalizedTaxCode = form.maSoThue?.trim() || '';
    if (normalizedTaxCode && !TAX_CODE_REGEX.test(normalizedTaxCode)) {
      setError('Mã số thuế phải gồm 10-14 chữ số.');
      return;
    }

    setSaving(true);
    setMessage('');
    setError('');

    try {
      const created = await companyService.createCompany({
        tenCongTy: form.tenCongTy.trim(),
        maSoThue: normalizedTaxCode || undefined,
        logoUrl: form.logoUrl?.trim() || undefined,
        website: form.website?.trim() || undefined,
        moTa: form.moTa?.trim() || undefined,
      });

      if (selectedIndustries.length > 0) {
        await companyService.updateCompanyIndustries(created.id, selectedIndustries);
      }

      setCompanyId(created.id);
      setMessage('Tạo công ty thành công.');
      await fetchData();
    } catch (err) {
      setError(mapError(err, 'Không thể tạo công ty mới.'));
    } finally {
      setSaving(false);
    }
  };

  const handleVerifyTax = async () => {
    const tax = taxInput.trim();
    if (!tax) {
      setTaxResult('Vui lòng nhập mã số thuế để kiểm tra.');
      return;
    }

    setTaxChecking(true);
    setTaxResult('');
    try {
      const exists = await companyService.verifyTaxCode(tax);
      setTaxResult(exists ? 'Mã số thuế đã tồn tại trong hệ thống.' : 'Mã số thuế chưa tồn tại.');
    } catch (err) {
      setTaxResult(mapError(err, 'Không thể kiểm tra mã số thuế.'));
    } finally {
      setTaxChecking(false);
    }
  };

  const handleLoadManualCompany = async () => {
    const id = Number(manualCompanyId);
    if (!Number.isInteger(id) || id <= 0) {
      setError('Company ID không hợp lệ.');
      return;
    }

    setLoading(true);
    setError('');
    setMessage('');
    try {
      await loadCompanyData(id);
      setMessage('Đã tải công ty theo Company ID.');
    } catch (err) {
      setError(mapError(err, 'Không thể tải công ty theo Company ID.'));
    } finally {
      setLoading(false);
    }
  };

  const companyTag = useMemo(() => {
    if (!companyId) return 'Chưa xác định Company ID';
    return `Company ID: ${companyId}`;
  }, [companyId]);

  return (
    <MainLayout title="Thông tin công ty" breadcrumb="Trang chủ / Nhà tuyển dụng / Công ty">
      <div className={s.stack}>
        <div className={s.topBar}>
          <div className={s.tags}>
            <span className={s.tag}>{companyTag}</span>
            <span className={s.tag}>Tài khoản: {user?.tenCongTy || 'N/A'}</span>
          </div>
          <div className={s.actions}>
            <button type="button" className={`${s.btn} ${s.btnGhost}`} onClick={() => void fetchData()}>
              Làm mới
            </button>
          </div>
        </div>

        {loading ? <div className={s.alert}>Đang tải dữ liệu công ty...</div> : null}
        {error ? <div className={`${s.alert} ${s.alertError}`}>{error}</div> : null}
        {message ? <div className={`${s.alert} ${s.alertSuccess}`}>{message}</div> : null}

        <section className={s.card}>
          <h3 className={s.cardTitle}>Nạp công ty theo ID (khi không auto-resolve được)</h3>
          <div className={s.actions}>
            <input
              className={s.input}
              style={{ maxWidth: 220 }}
              value={manualCompanyId}
              onChange={(e) => setManualCompanyId(e.target.value)}
              placeholder="Nhập Company ID"
            />
            <button type="button" className={`${s.btn} ${s.btnGhost}`} onClick={() => void handleLoadManualCompany()}>
              Tải công ty
            </button>
          </div>
        </section>

        <div className={s.grid2}>
          <section className={s.card}>
            <h3 className={s.cardTitle}>Thông tin cơ bản</h3>
            <div className={s.field}>
              <label className={s.label}>Tên công ty</label>
              <input
                className={s.input}
                value={form.tenCongTy || ''}
                onChange={(e) => setForm((prev) => ({ ...prev, tenCongTy: e.target.value }))}
              />
            </div>
            <div className={s.field}>
              <label className={s.label}>Mã số thuế</label>
              <input
                className={s.input}
                value={form.maSoThue || ''}
                onChange={(e) => setForm((prev) => ({ ...prev, maSoThue: e.target.value }))}
              />
            </div>
            <div className={s.field}>
              <label className={s.label}>Website</label>
              <input
                className={s.input}
                value={form.website || ''}
                onChange={(e) => setForm((prev) => ({ ...prev, website: e.target.value }))}
              />
            </div>
            <div className={s.field}>
              <label className={s.label}>Logo URL</label>
              <input
                className={s.input}
                value={form.logoUrl || ''}
                onChange={(e) => setForm((prev) => ({ ...prev, logoUrl: e.target.value }))}
              />
            </div>
            <div className={s.field}>
              <label className={s.label}>Mô tả</label>
              <textarea
                className={s.textarea}
                value={form.moTa || ''}
                onChange={(e) => setForm((prev) => ({ ...prev, moTa: e.target.value }))}
              />
            </div>
            <div className={s.actions}>
              {!companyId ? (
                <button
                  type="button"
                  className={`${s.btn} ${s.btnPrimary}`}
                  disabled={saving}
                  onClick={() => void handleCreateCompany()}
                >
                  Tạo công ty mới
                </button>
              ) : null}
              <button
                type="button"
                className={`${s.btn} ${s.btnPrimary}`}
                disabled={saving || !companyId}
                onClick={() => void handleSave()}
              >
                Lưu công ty
              </button>
            </div>
          </section>

          <section className={s.card}>
            <h3 className={s.cardTitle}>Ngành nghề và kiểm tra MST</h3>
            <div className={s.field}>
              <label className={s.label}>Chọn ngành nghề</label>
              <div className={s.checkGrid}>
                {industryOptions.map((item) => (
                  <label key={item.value} className={s.checkItem}>
                    <input
                      type="checkbox"
                      checked={selectedIndustries.includes(item.value)}
                      onChange={() => handleToggleIndustry(item.value)}
                    />
                    <span>{item.label}</span>
                  </label>
                ))}
              </div>
            </div>

            <div className={s.field}>
              <label className={s.label}>Kiểm tra mã số thuế (B6)</label>
              <div className={s.actions}>
                <input
                  className={s.input}
                  value={taxInput}
                  onChange={(e) => setTaxInput(e.target.value)}
                  placeholder="Nhập mã số thuế"
                />
                <button
                  type="button"
                  className={`${s.btn} ${s.btnGhost}`}
                  disabled={taxChecking}
                  onClick={() => void handleVerifyTax()}
                >
                  {taxChecking ? 'Đang kiểm tra...' : 'Kiểm tra'}
                </button>
              </div>
              {taxResult ? <div className={s.alert}>{taxResult}</div> : null}
            </div>
          </section>
        </div>
      </div>
    </MainLayout>
  );
}
