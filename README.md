# HỆ THỐNG TRUNG TÂM TUYỂN DỤNG (RECRUITMENT CENTER)
## Spring Boot REST API + ReactJS/TypeScript + MySQL

---

## 1. Giới thiệu đề tài
Đây là dự án xây dựng hệ thống nền tảng tuyển dụng trực tuyến (Web Application), giúp kết nối ứng viên tìm việc và nhà tuyển dụng. Hệ thống được phát triển theo mô hình Client-Server với Backend sử dụng Spring Boot cung cấp các RESTful API và Frontend sử dụng ReactJS.

Hệ thống hỗ trợ các chức năng chính phân theo 3 đối tượng người dùng:
* **Ứng viên**: Quản lý hồ sơ/CV, tìm kiếm việc làm, ứng tuyển, nhận thông báo và theo dõi trạng thái, xem lịch phỏng vấn.
* **Nhà tuyển dụng**: Quản lý thông tin công ty, đăng và quản lý tin tuyển dụng, xử lý đơn ứng tuyển, lên lịch phỏng vấn.
* **Quản trị viên (Admin)**: Quản lý người dùng, duyệt tin tuyển dụng, quản lý danh mục hệ thống (kỹ năng, khu vực, ngành nghề), thống kê dashboard.

---

## 2. Công nghệ sử dụng
**Backend:**
* Java 17+
* Spring Boot (Web, Data JPA, Security)
* JWT (JSON Web Token) cho Authentication & Authorization
* Maven
* MySQL 8+

**Frontend:**
* ReactJS
* TypeScript
* Vite

---

## 3. Kiến trúc hệ thống
Dự án được tổ chức theo kiến trúc Client-Server (RESTful API):
* **Frontend (Client)**: Giao diện web được xây dựng bằng React, gọi API để lấy và gửi dữ liệu.
* **Backend (Server)**: Được xây dựng theo kiến trúc đa tầng (Layered Architecture):
   * **Controller**: Tiếp nhận request từ Client (REST API endpoints).
   * **Service**: Xử lý logic nghiệp vụ, tích hợp các chiến lược (Strategy) như Cosine Similarity, Keyword Matching để tính điểm phù hợp (Match Score).
   * **Repository**: Thao tác với cơ sở dữ liệu qua Spring Data JPA.
   * **Entity / DTO**: Ánh xạ đối tượng CSDL và truyền tải dữ liệu.

**Luồng chính:**
`Client (React) -> Controller -> Service -> Repository -> Database`

---

## 4. Chức năng chính (Dựa trên Use Case System)

### 4.1. Chức năng chung
* Xem và tìm kiếm việc làm.
* Tra cứu danh mục hệ thống.
* Đăng ký, Đăng nhập (phân quyền vai trò bằng JWT).
* Khôi phục tài khoản (Quên mật khẩu).
* Riêng Admin chỉ có Đăng nhập, Xem và tìm kiếm việc làm và Tra cứu danh mục hệ thống (nếu Admin quên mật khẩu bắt buộc phải liên hệ SuperAdmin hoặc IT Support để reset).

### 4.2. Dành cho Ứng viên (Candidate)
* Quản lý hồ sơ cá nhân và quản lý nhiều CV.
* Ứng tuyển công việc và theo dõi lịch sử trạng thái đơn ứng tuyển.
* Xem lịch phỏng vấn.
* Xem Dashboard tổng quan ứng viên.
* Xem gợi ý việc làm dựa trên điểm phù hợp (Match Score).
* Nhận thông báo hệ thống.

### 4.3. Dành cho Nhà tuyển dụng (Recruiter)
* Quản lý thông tin công ty.
* Đăng và quản lý các tin tuyển dụng.
* Xử lý và thay đổi trạng thái đơn ứng tuyển của ứng viên.
* Sắp xếp và quản lý lịch phỏng vấn.
* Xem Dashboard thống kê của nhà tuyển dụng.
* Xem gợi ý ứng viên phù hợp với tin tuyển dụng (Match Score).

### 4.4. Dành cho Quản trị viên (Admin)
* Quản lý người dùng (Block/Unblock, thêm tài khoản quản trị).
* Kiểm duyệt và đóng các tin tuyển dụng vi phạm.
* Quản lý thông tin các công ty trên hệ thống.
* Quản lý danh mục kỹ năng hệ thống.
* Xem Dashboard quản trị tổng hợp.

---

## 5. Cấu trúc thư mục dự án
* `src/main/java`: Mã nguồn Backend Spring Boot.
* `src/main/resources`: Chứa file cấu hình `application.yaml`.
* `tuyen-dung-frontend`: Mã nguồn Frontend ReactJS.
* `database`: Các script khởi tạo và chèn dữ liệu mẫu MySQL.
* `uml`: Các sơ đồ thiết kế hệ thống (Use Case, Activity, Sequence, Class, Package Diagram).
* `uploads/cv-files`: Thư mục lưu trữ file CV upload.

---

## 6. Hướng dẫn tạo cơ sở dữ liệu

### Bước 1: Tạo database và bảng
Chạy lần lượt các file script trong thư mục `database`:
1. `01_create_database_tables.sql`
2. `02_insert_sample_data.sql` (Chứa dữ liệu mẫu để test)

### Bước 2: Cấu hình lại `application.yaml`
Mở file: `src/main/resources/application.yaml` và chỉnh lại thông số kết nối Database:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/he_thong_tuyen_dung?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh
    username: YOUR_MYSQL_USERNAME
    password: YOUR_MYSQL_PASSWORD
```
---

## 7. Hướng dẫn chạy project

**Chạy Backend (Spring Boot):**
1. Mở thư mục gốc bằng IntelliJ IDEA / Eclipse.
2. Chờ Maven tải các dependencies.
3. Chạy file `TuyendungApplication.java`.
4. Backend sẽ chạy tại `http://localhost:8080`.

**Chạy Frontend (React):**
1. Mở terminal tại thư mục `tuyen-dung-frontend`.
2. Chạy lệnh cài đặt thư viện: `npm install`
3. Chạy môi trường dev: `npm run dev`
4. Truy cập frontend thông qua link Vite cung cấp (thường là `http://localhost:3000`).

---

## 8. Tài khoản mẫu (Mật khẩu chung: `123`)

**ADMIN**
* Username: `admin@system.local`

**NHÀ TUYỂN DỤNG**
* Username 1: `hr1@fsoft.local`
* Username 2: `hr2@vng.local`

**ỨNG VIÊN**
* Username 1: `candidate1@mail.local`
* Username 2: `candidate2@mail.local`

---

## 9. Phân quyền

**ADMIN**
* Có quyền truy cập Admin Console, duyệt tin, quản lý user, dashboard hệ thống. Không thể ứng tuyển.

**NHÀ TUYỂN DỤNG**
* Được truy cập Recruiter Workflow, đăng tin, duyệt CV, lên lịch phỏng vấn.

**ỨNG VIÊN**
* Có quyền cập nhật CV cá nhân, tìm kiếm và nộp đơn ứng tuyển, theo dõi lịch phỏng vấn.

---

## 10. Sơ đồ thiết kế (UML)
Các sơ đồ UML được lưu trong thư mục `uml/`, gồm:
* Sơ đồ Use Case tổng quan và chi tiết cho từng tác nhân.
* Sơ đồ Hoạt động (Activity Diagram) cho 24 Use Case.
* Sơ đồ Tuần tự (Sequence Diagram) cho 24 Use Case.
* Sơ đồ Lớp (Class Diagram).
* Sơ đồ Gói (Package Diagram).

---

## 11. Phân công công việc (2 Thành viên)

**Sinh viên 1 (Backend & Database):**
* Phân tích thiết kế CSDL (MySQL) và vẽ sơ đồ ERD.
* Xây dựng hệ thống RESTful API bằng Spring Boot (Controller, Service, Repository, Entity).
* Triển khai bảo mật JWT (Authentication & Role-based Authorization).
* Cài đặt thuật toán xử lý Matching Score (Strategy Pattern).
* Xử lý luồng Upload và lưu trữ file (CV/Avatar).

**Sinh viên 2 (Frontend & System Analyst):**
* Xây dựng toàn bộ giao diện Web Application bằng ReactJS + TypeScript.
* Tích hợp API cho cả 3 phân hệ: Ứng viên, Nhà tuyển dụng, Quản trị viên.
* Xử lý State Management và kiểm soát Routing (Guards).
* Khảo sát, phân tích nghiệp vụ và vẽ toàn bộ các sơ đồ UML (Use Case, Activity, Sequence, Class).
* Viết tài liệu báo cáo đồ án 

---

## 12. Các điểm nổi bật của đồ án
* Áp dụng Design Pattern (Strategy Pattern cho thuật toán Matching).
* Có tính năng gợi ý việc làm/ứng viên dựa trên mức độ phù hợp (Match Score).
* Sử dụng JWT cho bảo mật và phân quyền linh hoạt.
* Hệ thống lưu vết lịch sử trạng thái đơn ứng tuyển chuyên nghiệp.
* Quản lý upload file linh hoạt (Lưu trữ Local).
* Sơ đồ UML cực kỳ chi tiết, bám sát nghiệp vụ thực tế.

---

## 13. Hướng phát triển thêm
* Tích hợp dịch vụ lưu trữ Cloud (AWS S3 hoặc Cloudinary) thay cho Local Storage.
* Bổ sung tính năng Real-time chat giữa Ứng viên và Nhà tuyển dụng (sử dụng WebSocket).
* Tích hợp AI để trích xuất thông tin tự động từ file PDF CV (OCR / NLP).
* Thông báo qua Email/SMS thực tế (tích hợp SendGrid/Twilio).

---

## 14. Kết luận
Dự án minh họa cách xây dựng một hệ thống Web toàn diện bằng Spring Boot và React, với kiến trúc RESTful rõ ràng, ứng dụng các nguyên lý thiết kế phần mềm, thuật toán phù hợp và rất thích hợp làm đồ án môn học Phân tích thiết kế hệ thống hoặc Đồ án tốt nghiệp.
