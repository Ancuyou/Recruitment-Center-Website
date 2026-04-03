export type TrangThaiDon = 1 | 2 | 3 | 4 | 5;

export interface SubmitApplicationRequest {
  tinTuyenDungId: number;
  hoSoCvId: number;
  thuNgo?: string;
}

export interface ApplicationItem {
  id: number;
  tinTuyenDungId: number;
  tieuDeTin: string;
  tenCongTy: string;
  logoCongTy?: string;
  ungVienId: number;
  tenUngVien: string;
  emailUngVien: string;
  hoSoCvId: number;
  cvUrl?: string;
  thuNgo?: string;
  trangThai: TrangThaiDon;
  trangThaiLabel: string;
  ngayNop: string;
}

export interface ApplicationStatusHistoryItem {
  id: number;
  donUngTuyenId: number;
  nguoiThucHien: string;
  vaiTro: string;
  trangThaiCu?: TrangThaiDon;
  trangThaiCuLabel?: string;
  trangThaiMoi: TrangThaiDon;
  trangThaiMoiLabel: string;
  ghiChu?: string;
  thoiGianChuyen: string;
}

export type HinhThucPhongVan = 'ONLINE' | 'OFFLINE';

export type TrangThaiPhongVan = 'CHO_PHONG_VAN' | 'HOAN_THANH' | 'HUY';

export interface InterviewItem {
  id: number;
  donUngTuyenId: number;
  tieuDeTin: string;
  tenUngVien: string;
  nguoiPhongVanId: number;
  tenNguoiPhongVan: string;
  tieuDeVong: string;
  thoiGianBatDau: string;
  thoiGianKetThuc: string;
  hinhThuc: HinhThucPhongVan;
  diaDiemHoacLink?: string;
  trangThai: TrangThaiPhongVan;
  ngayTao: string;
}

export interface UpdateApplicationStatusRequest {
  trangThaiMoi: TrangThaiDon;
  ghiChu?: string;
}

export interface InterviewUpsertRequest {
  donUngTuyenId: number;
  tieuDeVong: string;
  thoiGianBatDau: string;
  thoiGianKetThuc: string;
  hinhThuc: HinhThucPhongVan;
  diaDiemHoacLink?: string;
}

export interface InterviewRescheduleRequest {
  thoiGianBatDau: string;
  thoiGianKetThuc: string;
  diaDiemHoacLink?: string;
}
