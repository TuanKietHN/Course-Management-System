# Tài liệu Đặc tả Chức năng
## Hệ thống Quản lý Khóa học (CMS)

## 1. Module Xác thực (Authentication)
Module này xử lý việc đăng nhập, cấp token và quản lý phiên làm việc sử dụng JWT và Redis.

### 1.1 Luồng Đăng nhập (Login)
1.  **Người dùng** gửi `username` và `password` đến API `/api/auth/login`.
2.  **Hệ thống** xác thực thông tin từ Database (so khớp hash password).
3.  Nếu đúng:
    *   Tạo **Access Token** (JWT): Chứa thông tin user (id, username, roles). Thời hạn: 15-30 phút.
    *   Tạo **Refresh Token** (UUID/Random String): Thời hạn: 7-30 ngày.
    *   Lưu Refresh Token vào **Redis** với key `rt:{username}` hoặc `rt:{userId}`.
4.  Trả về cả 2 token cho Client.

### 1.2 Luồng Làm mới Token (Refresh Token)
1.  **Client** phát hiện Access Token hết hạn (hoặc sắp hết hạn).
2.  Gửi request đến `/api/auth/refresh` kèm `refreshToken`.
3.  **Hệ thống** kiểm tra `refreshToken` có tồn tại trong **Redis** không.
4.  Nếu tồn tại và khớp:
    *   Tạo cặp Access Token & Refresh Token mới.
    *   Cập nhật lại Redis (thu hồi token cũ, lưu token mới).
    *   Trả về token mới.
5.  Nếu không tồn tại/không khớp: Trả về lỗi 401 (Yêu cầu đăng nhập lại).

### 1.3 Luồng Đăng xuất (Logout)
1.  **Client** gọi API `/api/auth/logout`.
2.  **Hệ thống** xóa Refresh Token tương ứng khỏi **Redis**.
3.  (Tùy chọn) Đưa Access Token hiện tại vào **Blacklist** trên Redis với TTL bằng thời gian còn lại của token.

## 2. Module Học vụ (Cốt lõi)
*(Giữ nguyên logic nghiệp vụ)*

### 2.1 Quản lý Học kỳ
*   **Tác nhân**: Admin.
*   **Dữ liệu**: Tên, Ngày bắt đầu, Ngày kết thúc, Trạng thái.
*   **Quy tắc**: Chỉ một học kỳ có thể ở trạng thái "Mở đăng ký".

### 2.2 Quản lý Khóa học
*   **Tác nhân**: Admin.
*   **Dữ liệu**: Mã môn, Tên môn, Số tín chỉ.

### 2.3 Quản lý Lớp học phần
*   **Tác nhân**: Admin.
*   **Dữ liệu**: Môn học, Học kỳ, Giảng viên, Lịch học, Sĩ số.

## 3. Module Đăng ký học
*(Giữ nguyên logic nghiệp vụ)*

### 3.1 Đăng ký Khóa học
*   **Tác nhân**: Sinh viên.
*   **Quy trình**: Chọn lớp -> Kiểm tra điều kiện (Thời gian, Sĩ số, Trùng lịch, Tiên quyết) -> Ghi nhận.

## 4. Module Chấm điểm
*(Giữ nguyên logic nghiệp vụ)*

### 4.1 Nhập điểm
*   **Tác nhân**: Giảng viên.
*   **Quy tắc**: Điểm 0-10, theo trọng số.

### 4.2 Tính GPA
*   **Logic**: Thang 10 -> Thang 4 -> Tính trung bình.

## 5. Module Báo cáo
*(Giữ nguyên logic nghiệp vụ)*
