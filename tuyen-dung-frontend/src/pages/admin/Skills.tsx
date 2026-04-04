import { useCallback, useEffect, useState } from 'react';
import MainLayout from '@/layouts/MainLayout';
import AppDataTable, { type AppDataColumn } from '@/components/common/AppDataTable';
import AdminRoleNav from '@/components/admin/AdminRoleNav';
import { useDraftHistory } from '@/hooks/useDraftHistory';
import { skillService } from '@/services/modules/skill.module';
import type { SkillItem } from '@/types/skill.types';
import a from '@/assets/styles/admin-console.module.css';

function mapError(error: unknown): string {
  return (
    (error as { response?: { data?: { message?: string } } })?.response?.data?.message ??
    'Không thể xử lý dữ liệu kỹ năng.'
  );
}

export default function AdminSkillsPage() {
  const [skills, setSkills] = useState<SkillItem[]>([]);
  const [searchKeyword, setSearchKeyword] = useState('');
  const skillNameDraft = useDraftHistory<string>({
    storageKey: 'draft.admin.skills.name',
    initialValue: '',
  });
  const nameInput = skillNameDraft.value;
  const [editingId, setEditingId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const fetchSkills = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await skillService.getAllSkills();
      setSkills(data);
    } catch (err) {
      setError(mapError(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void fetchSkills();
  }, [fetchSkills]);

  const handleSearch = async () => {
    const keyword = searchKeyword.trim();
    if (!keyword) {
      await fetchSkills();
      return;
    }

    setLoading(true);
    setError('');
    try {
      const data = await skillService.searchSkills(keyword);
      setSkills(data);
    } catch (err) {
      setError(mapError(err));
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    const tenKyNang = nameInput.trim();
    if (!tenKyNang) {
      setError('Tên kỹ năng không được để trống.');
      return;
    }

    setSaving(true);
    setError('');
    setMessage('');
    try {
      if (editingId == null) {
        await skillService.createSkill({ tenKyNang });
        setMessage('Tạo kỹ năng thành công.');
      } else {
        await skillService.updateSkill(editingId, { tenKyNang });
        setMessage('Cập nhật kỹ năng thành công.');
      }
      setEditingId(null);
      skillNameDraft.clearDraft('');
      await fetchSkills();
    } catch (err) {
      setError(mapError(err));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: number) => {
    const confirmed = window.confirm('Bạn có chắc muốn xóa kỹ năng này?');
    if (!confirmed) return;

    setSaving(true);
    setError('');
    setMessage('');
    try {
      await skillService.deleteSkill(id);
      setMessage('Xóa kỹ năng thành công.');
      if (editingId === id) {
        setEditingId(null);
        skillNameDraft.clearDraft('');
      }
      await fetchSkills();
    } catch (err) {
      setError(mapError(err));
    } finally {
      setSaving(false);
    }
  };

  const handlePickSkill = async (id: number) => {
    setSaving(true);
    setError('');
    setMessage('');
    try {
      const detail = await skillService.getSkillById(id);
      setEditingId(detail.id);
      skillNameDraft.replaceValue(detail.tenKyNang);
    } catch (err) {
      setError(mapError(err));
    } finally {
      setSaving(false);
    }
  };

  const columns: AppDataColumn<SkillItem>[] = [
    {
      key: 'tenKyNang',
      header: 'Tên kỹ năng',
      render: (row) => (
        <div style={{ display: 'grid', gap: 4 }}>
          <strong>{row.tenKyNang}</strong>
          <span className={a.muted}>ID: {row.id}</span>
        </div>
      ),
    },
    {
      key: 'actions',
      header: 'Tác vụ',
      width: '220px',
      align: 'center',
      render: (row) => (
        <div className={a.toolbar} style={{ justifyContent: 'center' }}>
          <button
            type="button"
            className={`${a.btn} ${a.btnGhost}`}
            disabled={saving}
            onClick={() => void handlePickSkill(row.id)}
          >
            Sửa
          </button>
          <button
            type="button"
            className={`${a.btn} ${a.btnDanger}`}
            disabled={saving}
            onClick={() => void handleDelete(row.id)}
          >
            Xóa
          </button>
        </div>
      ),
    },
  ];

  return (
    <MainLayout title="Kỹ năng" breadcrumb="Trang chủ / Admin / Kỹ năng">
      <div className={a.page}>
        <AdminRoleNav />

        <section className={a.surface}>
          <div className={a.sectionHead}>
            <h3 className={a.sectionTitle}>Kho kỹ năng hệ thống</h3>
            <div className={a.chips}>
              <span className={a.chip}>Tổng kỹ năng: {skills.length}</span>
              {editingId != null ? <span className={a.chip}>Đang sửa ID: {editingId}</span> : null}
            </div>
          </div>
          <div className={a.toolbar}>
            <input
              className={a.input}
              style={{ maxWidth: 340 }}
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              placeholder="Nhập từ khóa kỹ năng"
            />
            <button type="button" className={`${a.btn} ${a.btnGhost}`} onClick={() => void handleSearch()}>
              Tìm
            </button>
            <button
              type="button"
              className={`${a.btn} ${a.btnGhost}`}
              onClick={() => {
                setSearchKeyword('');
                void fetchSkills();
              }}
            >
              Làm mới
            </button>
          </div>
        </section>

        <div className={a.grid2}>
          <section className={a.surface}>
            <h3 className={a.sectionTitle}>Bộ chỉnh sửa kỹ năng</h3>
            <p className={a.sectionHint}>Dùng cùng một form cho tạo mới và cập nhật.</p>
            <input
              className={a.input}
              value={nameInput}
              onChange={(e) => skillNameDraft.setValue(e.target.value)}
              placeholder="Ví dụ: React, Spring Boot, SQL"
            />
            <div className={a.toolbar}>
              <button
                type="button"
                className={`${a.btn} ${a.btnGhost}`}
                disabled={saving || !skillNameDraft.canUndo}
                onClick={skillNameDraft.undo}
              >
                Undo
              </button>
              <button
                type="button"
                className={`${a.btn} ${a.btnGhost}`}
                disabled={saving || !skillNameDraft.canRedo}
                onClick={skillNameDraft.redo}
              >
                Redo
              </button>
              <button
                type="button"
                className={`${a.btn} ${a.btnGhost}`}
                onClick={() => {
                  setEditingId(null);
                  skillNameDraft.clearDraft('');
                  setError('');
                  setMessage('');
                }}
              >
                Reset form
              </button>
              <button
                type="button"
                className={`${a.btn} ${a.btnPrimary}`}
                disabled={saving}
                onClick={() => void handleSave()}
              >
                {editingId == null ? 'Tạo kỹ năng' : 'Lưu cập nhật'}
              </button>
            </div>
          </section>

          <section className={a.surface}>
            <h3 className={a.sectionTitle}>Danh sách kỹ năng</h3>
            <AppDataTable
              columns={columns}
              data={skills}
              rowKey={(row) => String(row.id)}
              emptyMessage="Không có kỹ năng nào."
            />
          </section>
        </div>

        {loading ? <div className={a.notice}>Đang tải dữ liệu kỹ năng...</div> : null}
        {error ? <div className={`${a.notice} ${a.noticeError}`}>{error}</div> : null}
        {message ? <div className={`${a.notice} ${a.noticeSuccess}`}>{message}</div> : null}

        <section className={a.surface}>
          <h3 className={a.sectionTitle}>Lưu ý vận hành</h3>
          <div className={a.list}>
            <div className={a.listItem}>
              <div className={a.listTitle}>Tạo kỹ năng trước khi gán cho job/CV</div>
              <div className={a.muted}>Kỹ năng hệ thống là nguồn dùng chung cho module recruiter và candidate.</div>
            </div>
            <div className={a.listItem}>
              <div className={a.listTitle}>Xóa kỹ năng cần thận trọng</div>
              <div className={a.muted}>Nếu kỹ năng đã được dùng trong job hoặc CV, backend có thể từ chối xóa theo ràng buộc dữ liệu.</div>
            </div>
          </div>
        </section>
      </div>
    </MainLayout>
  );
}
