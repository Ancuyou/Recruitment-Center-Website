const SAVED_JOBS_KEY = 'candidate-saved-job-ids';

function normalizeIds(raw: unknown): number[] {
  if (!Array.isArray(raw)) return [];
  return raw
    .map((value) => Number(value))
    .filter((value) => Number.isInteger(value) && value > 0);
}

function readIds(): number[] {
  try {
    const raw = localStorage.getItem(SAVED_JOBS_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw) as unknown;
    return Array.from(new Set(normalizeIds(parsed)));
  } catch {
    return [];
  }
}

function writeIds(ids: number[]): void {
  localStorage.setItem(SAVED_JOBS_KEY, JSON.stringify(Array.from(new Set(ids))));
}

export const savedJobsLocal = {
  listIds(): number[] {
    return readIds();
  },

  isSaved(jobId: number): boolean {
    return readIds().includes(jobId);
  },

  toggle(jobId: number): number[] {
    const current = readIds();
    const exists = current.includes(jobId);
    const next = exists ? current.filter((id) => id !== jobId) : [jobId, ...current];
    writeIds(next);
    return next;
  },

  remove(jobId: number): number[] {
    const next = readIds().filter((id) => id !== jobId);
    writeIds(next);
    return next;
  },

  clear(): void {
    localStorage.removeItem(SAVED_JOBS_KEY);
  },
};
