# Kế hoạch Triển khai Tiếp theo

Sau khi đã hoàn thành Module Xác thực (Auth), dưới đây là lộ trình các công việc tiếp theo để hoàn thiện hệ thống CMS.

## Giai đoạn 1: Quản lý Người dùng (User Management)
*Mục tiêu: Cho phép người dùng xem/sửa thông tin cá nhân và Admin quản lý danh sách người dùng.*

1.  **API User Profile (Cho mọi User)**:
    *   `GET /api/users/me`: Xem thông tin bản thân.
    *   `PUT /api/users/me`: Cập nhật thông tin cá nhân (Avatar, Số điện thoại...).
    *   `PUT /api/users/change-password`: Đổi mật khẩu.
2.  **Admin User Management (Cho Admin)**:
    *   `GET /api/admin/users`: Danh sách users (Phân trang, Tìm kiếm).
    *   `POST /api/admin/users`: Tạo user mới (Admin, Teacher, Student).
    *   `PUT /api/admin/users/{id}`: Sửa thông tin user (Reset password, đổi Role).
    *   `DELETE /api/admin/users/{id}`: Vô hiệu hóa user (Soft delete).

## Giai đoạn 2: Module Học vụ (Academic Core)
*Mục tiêu: Xây dựng dữ liệu nền tảng cho việc đào tạo (Master Data).*

1.  **Quản lý Học kỳ (Semester)**:
    *   Tạo Entity `Semester`.
    *   API CRUD: Tạo, Sửa, Xóa (Soft), Mở/Đóng đăng ký.
    *   Logic: Validate ngày bắt đầu/kết thúc, chỉ 1 học kỳ được mở đăng ký.
2.  **Quản lý Môn học (Course)**:
    *   Tạo Entity `Course` (Mã môn, Tên môn, Số tín chỉ, Khoa).
    *   API CRUD.
3.  **Quản lý Lớp học phần (Course Section)**:
    *   Tạo Entity `CourseSection` (Liên kết Semester, Course, Teacher).
    *   API CRUD: Lên lịch học, Gán phòng, Giới hạn sĩ số.

## Giai đoạn 3: Module Đăng ký (Enrollment)
*Mục tiêu: Cho phép sinh viên đăng ký học theo tín chỉ.*

1.  **Quy trình Đăng ký**:
    *   Sinh viên xem danh sách lớp đang mở.
    *   API Đăng ký lớp (`POST /api/enrollments`).
    *   **Logic Validate phức tạp**:
        *   Kiểm tra trùng lịch học.
        *   Kiểm tra sĩ số lớp (Concurrency handling).
        *   Kiểm tra số tín chỉ tối đa/tối thiểu.
2.  **Quản lý Danh sách lớp**:
    *   Giảng viên/Admin xem danh sách sinh viên.
    *   Xuất danh sách điểm danh.

## Giai đoạn 4: Module Điểm số (Grading)
*Mục tiêu: Quản lý kết quả học tập và tính toán GPA.*

1.  **Nhập điểm (Teacher)**:
    *   Tạo Entity `Grade`.
    *   API Nhập điểm thành phần (Chuyên cần, Giữa kỳ, Cuối kỳ).
    *   Logic: Tính điểm tổng kết môn học.
2.  **Báo cáo (Student/Admin)**:
    *   Sinh viên xem bảng điểm cá nhân.
    *   Hệ thống tính GPA học kỳ và tích lũy.

## Đề xuất Thứ tự Thực hiện Ngay
Tôi đề xuất bắt đầu với **Giai đoạn 1 (User Management)** hoặc **Giai đoạn 2 (Academic Core)**.
*   Nếu bạn muốn hoàn thiện phần Quản trị hệ thống trước -> Chọn **Giai đoạn 1**.
*   Nếu bạn muốn đi thẳng vào nghiệp vụ chính của CMS -> Chọn **Giai đoạn 2**.
