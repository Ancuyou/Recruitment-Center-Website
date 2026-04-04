export interface SkillItem {
  id: number;
  tenKyNang: string;
  ngayTao?: string;
}

export interface SkillUpsertRequest {
  tenKyNang: string;
}
