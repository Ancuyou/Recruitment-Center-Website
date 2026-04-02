# Bao cao phan ra Use Case he thong tuyen dung

## 1. Pham vi va nguyen tac

- Nguon doi soat chinh: controller API trong `src/main/java/com/example/tuyendung/controller`.
- Nguon quy uoc cach ve: muc Use Case trong file `ĐỀ MẪU DỰ ÁN.pdf` (phan Actors, Use cases bat buoc, include/extend goi y).
- Nguyen tac dat ten: su dung cum dong tu tieng Viet, uu tien muc nghiep vu.
- So do tong chi giu: Actor, boundary he thong, use case, mui ten lien ket, `<<include>>`, `<<extend>>`.

## 2. Actor he thong

- `Khach`
- `Ung vien`
- `Nha tuyen dung`
- `Quan tri vien`

## 3. Tong hop use case chinh

1. Xac thuc tai khoan
2. Quan ly ho so ca nhan
3. Quan ly cong ty
4. Quan ly tin tuyen dung
5. Tim kiem va xem tin tuyen dung
6. Quan ly ho so CV
7. Quan ly ky nang
8. Quan ly don ung tuyen
9. Quan ly phong van
10. Quan ly thong bao
11. Xem bang dieu khien
12. Goi y va danh gia do phu hop
13. Tra cuu danh muc
14. Quan ly tai khoan nguoi dung

## 4. Phan giai use case chinh -> use case con

### 4.1 Xac thuc tai khoan

- Dang ky ung vien
- Dang ky nha tuyen dung
- Dang nhap
- Dang xuat
- Lam moi token
- Quen mat khau
- Dat lai mat khau
- Xac thuc email

Controller/API:
- `AuthController` (`/api/auth/*`)

Quan he:
- `Xac thuc tai khoan <<include>>` tat ca use case con o tren.

### 4.2 Quan ly ho so ca nhan

- Xem thong tin ca nhan
- Cap nhat ho so ca nhan
- Doi mat khau
- Cap nhat anh dai dien

Controller/API:
- `UserController` (`/api/users/*`)
- `AuthController` (`/api/auth/me`)

Quan he:
- `Quan ly ho so ca nhan <<include>>` cac use case con.

### 4.3 Quan ly cong ty

- Tao cong ty
- Xem danh sach cong ty
- Xem chi tiet cong ty
- Cap nhat cong ty
- Xoa cong ty
- Xac minh ma so thue
- Quan ly nganh nghe cong ty

Controller/API:
- `CompanyController` (`/api/companies/*`)

Quan he:
- `Quan ly cong ty <<include>>` cac use case con.

### 4.4 Quan ly tin tuyen dung

- Dang tin tuyen dung
- Cap nhat tin tuyen dung
- Dong tin tuyen dung
- Xem tin da dang
- Xem thong ke tin
- Quan ly khu vuc tuyen
- Quan ly ky nang yeu cau

Controller/API:
- `TinTuyenDungController` (`/api/jobs/*`)
- `JobSkillController` (`/api/jobs/{jobId}/skills/*`)

Quan he:
- `Quan ly tin tuyen dung <<include>>` cac use case con.

### 4.5 Tim kiem va xem tin tuyen dung

- Xem danh sach tin
- Tim kiem tin
- Xem chi tiet tin

Controller/API:
- `TinTuyenDungController` (`/api/jobs`, `/api/jobs/search`, `/api/jobs/{id}`)

Quan he:
- `Tim kiem va xem tin tuyen dung <<include>>` 3 use case con.
- `Tim kiem tin <<extend>> Xem danh sach tin`.

### 4.6 Quan ly ho so CV

- Tao CV
- Cap nhat CV
- Xoa CV
- Chon CV mac dinh
- Tai tep CV
- Quan ly chi tiet CV (hoc van/kinh nghiem/chung chi)
- Quan ly ky nang CV

Controller/API:
- `HoSoCvController` (`/api/cvs/*`)
- `ChiTietCvController` (`/api/cvs/{cvId}/hoc-van-kn/*`)
- `CvSkillController` (`/api/cvs/{cvId}/skills/*`)

Quan he:
- `Quan ly ho so CV <<include>>` cac use case con.

### 4.7 Quan ly ky nang

- Xem danh sach ky nang
- Tim kiem ky nang
- Tao ky nang
- Cap nhat ky nang
- Xoa ky nang

Controller/API:
- `SkillsController` (`/api/skills/*`)

Quan he:
- `Quan ly ky nang <<include>>` cac use case con.
- `Tim kiem ky nang <<extend>> Xem danh sach ky nang`.

### 4.8 Quan ly don ung tuyen

- Nop don ung tuyen
- Xem don da nop
- Xem don theo tin
- Xem chi tiet don
- Cap nhat trang thai don
- Tu choi don
- Xem lich su trang thai
- Xem anh CV dinh kem

Controller/API:
- `DonUngTuyenController` (`/api/applications/*`)

Quan he:
- `Quan ly don ung tuyen <<include>>` cac use case con.
- `Tu choi don <<extend>> Cap nhat trang thai don`.

### 4.9 Quan ly phong van

- Tao lich phong van
- Xem lich phong van
- Cap nhat lich phong van
- Huy lich phong van
- Doi lich phong van

Controller/API:
- `LichPhongVanController` (`/api/interviews/*`)

Quan he:
- `Quan ly phong van <<include>>` cac use case con.
- `Doi lich phong van <<extend>> Cap nhat lich phong van`.

### 4.10 Quan ly thong bao

- Xem danh sach thong bao
- Danh dau da doc
- Danh dau tat ca da doc
- Dem thong bao chua doc

Controller/API:
- `ThongBaoController` (`/api/notifications/*`)

Quan he:
- `Quan ly thong bao <<include>>` cac use case con.

### 4.11 Xem bang dieu khien

- Xem dashboard ung vien
- Xem dashboard nha tuyen dung

Controller/API:
- `DashboardController` (`/api/dashboard/candidate`, `/api/dashboard/recruiter`)

Quan he:
- `Xem bang dieu khien <<include>>` cac use case con.

### 4.12 Goi y va danh gia do phu hop

- Goi y cong viec phu hop
- Goi y ung vien phu hop
- Tinh diem phu hop

Controller/API:
- `MatchingController` (`/api/matching/*`)

Quan he:
- `Goi y va danh gia do phu hop <<include>>` cac use case con.

### 4.13 Tra cuu danh muc

- Xem danh sach nganh nghe
- Xem danh sach khu vuc

Controller/API:
- `NganhNgheController` (`/api/industries`)
- `KhuVucController` (`/api/locations`)

Quan he:
- `Tra cuu danh muc <<include>>` cac use case con.

### 4.14 Quan ly tai khoan nguoi dung

- Xem thong tin tai khoan theo ID

Controller/API:
- `AuthController` (`GET /api/auth/thong-tin/{taiKhoanId}`)

Quan he:
- `Quan ly tai khoan nguoi dung <<include>> Xem thong tin tai khoan theo ID`.

## 5. File so do tong da cap nhat

- `uml/UseCase_v2/00_System_Overview.puml`

## 6. Huong tach tiep theo cho so do chi tiet

- Ve rieng theo nhom actor:
  - Ung vien: Auth, Ho so, CV, Tim kiem tin, Don ung tuyen, Phong van, Thong bao, Dashboard.
  - Nha tuyen dung: Auth, Ho so, Cong ty, Tin tuyen dung, Don ung tuyen, Phong van, Thong bao, Dashboard, Matching.
  - Quan tri vien: Cong ty, Tin tuyen dung, Ky nang, Quan ly tai khoan nguoi dung.
- Moi file chi tiet giu ID endpoint de trace 1-1 voi controller.

