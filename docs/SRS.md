# Đặc tả Yêu cầu Phần mềm (SRS)
## Dự án: Hệ thống Quản lý Khóa học (CMS)

## 1. Giới thiệu
### 1.1 Mục đích
Tài liệu này nhằm xác định các yêu cầu phần mềm cho Hệ thống Quản lý Khóa học (CMS). Hệ thống được thiết kế để quản lý các khóa học đào tạo nội bộ, đăng ký của sinh viên, điểm số và báo cáo học vụ.

### 1.2 Phạm vi
CMS sẽ là một ứng dụng web cho phép:
- **Quản trị viên (Admin)**: Quản lý người dùng, khóa học, học kỳ và cấu hình hệ thống.
- **Giảng viên (Teacher)**: Quản lý lớp học phần, chấm điểm sinh viên và xem lịch giảng dạy.
- **Sinh viên (Student)**: Đăng ký khóa học, xem điểm và theo dõi tiến độ học tập.

### 1.3 Định nghĩa, Từ viết tắt
- **CMS**: Course Management System (Hệ thống quản lý khóa học).
- **JWT**: JSON Web Token.
- **Redis**: Remote Dictionary Server (Dùng cho Caching & Token Store).
- **GPA**: Grade Point Average (Điểm trung bình tích lũy).
- **RBAC**: Role-Based Access Control (Kiểm soát truy cập dựa trên vai trò).

## 2. Mô tả Tổng quan
### 2.1 Đặc điểm Người dùng
- **Admin**: Nhân viên kỹ thuật hoặc giáo vụ chịu trách nhiệm cấu hình hệ thống và dữ liệu gốc.
- **Teacher**: Giảng viên giảng dạy các khóa học.
- **Student**: Người học đăng ký tại cơ sở đào tạo.

### 2.2 Tính năng Sản phẩm
1.  **Xác thực & Phân quyền**: Đăng nhập tùy chỉnh sử dụng JWT (Access + Refresh Token) và Redis.
2.  **Quản lý Khóa học**: Tạo, cập nhật, xóa khóa học và môn học.
3.  **Kế hoạch Học tập**: Quản lý học kỳ và mở lớp học phần.
4.  **Đăng ký học**: Xử lý việc đăng ký khóa học của sinh viên.
5.  **Chấm điểm**: Nhập điểm và tính toán điểm số.
6.  **Báo cáo**: Xuất bảng điểm và cảnh báo học vụ.

### 2.3 Môi trường Hoạt động
- **Server**: Linux/Windows Server có hỗ trợ Docker.
- **Database**: PostgreSQL 14+.
- **Cache**: Redis 6+.
- **Client**: Trình duyệt web hiện đại (Chrome, Firefox, Edge).

## 3. Yêu cầu Chức năng

### 3.1 Xác thực (Module: Auth)
- **FR-01**: Người dùng đăng nhập bằng Username/Password.
- **FR-02**: Hệ thống trả về Access Token (ngắn hạn) và Refresh Token (dài hạn).
- **FR-03**: Hỗ trợ cơ chế "Refresh Token" để cấp lại Access Token mới mà không cần đăng nhập lại.
- **FR-04**: Hỗ trợ Đăng xuất (Thu hồi Refresh Token trong Redis).

### 3.2 Quản lý Học vụ (Module: Academic)
- **FR-05**: Admin có thể quản lý Học kỳ (Tạo, Mở, Đóng).
- **FR-06**: Admin có thể quản lý Danh mục Khóa học (Thông tin môn học, tín chỉ).
- **FR-07**: Admin/Giảng viên có thể quản lý Lớp học phần cho một học kỳ cụ thể.

### 3.3 Đăng ký học (Module: Enrollment)
- **FR-08**: Sinh viên có thể xem danh sách lớp học phần đang mở trong học kỳ hiện tại.
- **FR-09**: Sinh viên có thể đăng ký khóa học trong giới hạn tín chỉ cho phép.
- **FR-10**: Hệ thống phải kiểm tra điều kiện tiên quyết trước khi cho phép đăng ký.

### 3.4 Chấm điểm (Module: Grading)
- **FR-11**: Giảng viên có thể nhập điểm cho sinh viên trong lớp của họ.
- **FR-12**: Hệ thống tự động tính GPA dựa trên công thức đã cấu hình.
- **FR-13**: Sinh viên có thể xem điểm và GPA của chính mình.

## 4. Yêu cầu Phi chức năng
### 4.1 Bảo mật
- Tất cả API phải được bảo vệ sử dụng JWT (Bearer Token).
- Mật khẩu phải được mã hóa (BCrypt) trước khi lưu vào Database.
- Refresh Token phải được lưu trữ an toàn và có thời hạn (TTL) trong Redis.

### 4.2 Hiệu năng
- Thời gian phản hồi API phải dưới 500ms cho 95% các yêu cầu.
- Sử dụng Redis để cache các dữ liệu thường xuyên truy xuất (như thông tin User, Cấu hình).

### 4.3 Độ tin cậy
- Mục tiêu thời gian hoạt động (Uptime): 99.9%.
- Chiến lược sao lưu dữ liệu: Sao lưu gia tăng hàng ngày.
