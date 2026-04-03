export type CapBacYeuCau = 'FRESHER' | 'JUNIOR' | 'SENIOR' | 'LEAD';

export type HinhThucLamViec = 'ONLINE' | 'OFFICE' | 'HYBRID';

export type KhuVuc =
  | 'HA_NOI'
  | 'HO_CHI_MINH'
  | 'DA_NANG'
  | 'CAN_THO'
  | 'HAI_PHONG'
  | 'BINH_DUONG'
  | 'DONG_NAI'
  | 'HUNG_YEN'
  | 'BAC_NINH'
  | 'QUANG_NINH'
  | 'NGHE_AN'
  | 'THUA_THIEN_HUE'
  | 'KHANH_HOA'
  | 'LAM_DONG'
  | 'AN_GIANG'
  | 'REMOTE';

export interface JobPosting {
  id: number;
  nhaTuyenDungId: number;
  tenNhaTuyenDung: string;
  congTyId: number;
  tenCongTy: string;
  logoUrl?: string;
  tieuDe: string;
  moTaCongViec: string;
  yeuCauUngVien: string;
  mucLuongMin?: number;
  mucLuongMax?: number;
  diaDiem?: string;
  capBacYeuCau?: CapBacYeuCau;
  hinhThucLamViec?: HinhThucLamViec;
  hanNop?: string;
  trangThaiLabel?: string;
  trangThai: number;
  ngayTao?: string;
  ngayCapNhat?: string;
  khuVucs?: KhuVuc[];
  soLuongDon: number;
}

export interface JobSkill {
  id: number;
  jobId: number;
  kyNangId: number;
  tenKyNang: string;
  yeucau: number;
  moTa?: string;
  ngayTao?: string;
}

export interface JobSearchParams {
  keyword?: string;
  capBac?: CapBacYeuCau;
  hinhThuc?: HinhThucLamViec;
  mucLuongMin?: number;
  page?: number;
  size?: number;
}

export interface JobUpsertRequest {
  tieuDe: string;
  moTaCongViec: string;
  yeuCauUngVien: string;
  mucLuongMin?: number;
  mucLuongMax?: number;
  diaDiem?: string;
  capBacYeuCau: CapBacYeuCau;
  hinhThucLamViec: HinhThucLamViec;
  hanNop: string;
}

export interface JobStatistics {
  tinId: number;
  tieuDe: string;
  tongSoDon: number;
  soMoi: number;
  soReview: number;
  soPhongVan: number;
  soOffer: number;
  soTuChoi: number;
}
