export type LoaiBanGhiCv = 1 | 2 | 3;

export interface CvRequest {
  tieuDeCv: string;
  mucTieuNgheNghiep?: string;
  fileCvUrl?: string;
}

export interface CvSkillRequest {
  kyNangId: number;
  mucThanhThao: number;
  moTa?: string;
}

export interface CvSkillItem {
  id: number;
  cvId: number;
  kyNangId: number;
  tenKyNang: string;
  mucThanhThao: number;
  moTa?: string;
  ngayTao?: number;
}

export interface CvDetailRequest {
  loaiBanGhi: LoaiBanGhiCv;
  tenToChuc: string;
  chuyenNganhHoacViTri?: string;
  ngayBatDau: string;
  ngayKetThuc?: string;
  moTaChiTiet?: string;
}

export interface CvDetailItem {
  id: number;
  loaiBanGhi: LoaiBanGhiCv;
  tenToChuc: string;
  chuyenNganhHoacViTri?: string;
  ngayBatDau: string;
  ngayKetThuc?: string;
  moTaChiTiet?: string;
  ngayTao?: string;
}

export interface CvItem {
  id: number;
  tieuDeCv: string;
  mucTieuNgheNghiep?: string;
  fileCvUrl?: string;
  laCvChinh: boolean;
  ngayTao?: string;
  ngayCapNhat?: string;
  chiTietCvs?: CvDetailItem[];
  kyNangs?: CvSkillItem[];
}
