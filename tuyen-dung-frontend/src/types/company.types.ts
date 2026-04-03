export interface CompanyRequest {
  tenCongTy: string;
  maSoThue?: string;
  logoUrl?: string;
  website?: string;
  moTa?: string;
}

export interface CompanyItem {
  id: number;
  tenCongTy: string;
  maSoThue?: string;
  logoUrl?: string;
  website?: string;
  moTa?: string;
  ngayTao?: string;
  ngayCapNhat?: string;
  nganhNghes?: string[];
  soTinDangMo: number;
}
