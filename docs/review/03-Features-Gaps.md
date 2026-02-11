# 03. Chức năng hiện có và đề xuất mở rộng

## 1) Chức năng hiện có (theo code)

### 1.1. Auth (JWT)
API dưới `/api/v1/auth`:
- Đăng nhập (access + refresh token)
- Đăng ký tài khoản
- Làm mới token (refresh)
- Đăng xuất (revoke refresh token)
- Quên mật khẩu / đặt lại mật khẩu

Async:
- Có RabbitMQ producer/consumer phục vụ email (reset password, v.v.)

### 1.2. User (Admin)
API dưới `/api/v1/users` (bị chặn `hasRole('ADMIN')` ở controller):
- Danh sách user (search + filter role)
- Chi tiết user
- Tạo user
- Cập nhật user
- Xoá user
- Upload avatar (MinIO)

### 1.3. Academic (Học vụ cơ bản)
API:
- `/api/v1/subjects`: CRUD + list/filter
- `/api/v1/semesters`: CRUD + list/filter + lấy học kỳ active
- `/api/v1/courses`: CRUD + list/filter (controller mô tả là “lớp học phần”)
- `/api/v1/enrollments`:
  - Sinh viên đăng ký học phần
  - Teacher/Admin cập nhật trạng thái/điểm
  - Admin huỷ đăng ký
  - Danh sách enrollment theo student / theo course

Frontend:
- Chỉ có màn login và home (rất tối giản), chưa thấy UI quản trị học vụ.

## 2) Các khoảng trống (so với “quản lý khóa học nội bộ”)

### 2.1. Học vụ/đào tạo
- Lịch học, phòng học, ca học (section schedule)
- Trùng lịch khi đăng ký, quota/waitlist, điều kiện tiên quyết
- Chia điểm thành phần (gradebook), export bảng điểm
- Điểm danh theo buổi (attendance) và báo cáo chuyên cần
- Quy trình mở lớp: duyệt mở lớp, publish/unpublish, đóng đăng ký theo thời gian

### 2.2. Tổ chức nhân sự học thuật
- Quản lý khoa/phòng ban (departments): CRUD + phân cấp
- Quản lý hồ sơ giảng viên (teachers): employee_code, khoa, học hàm, bio…
- Gán giảng viên cho lớp / thay giảng / đồng giảng (co-teaching)

### 2.3. E-learning (nội dung và tiến độ)
Trong DB đã có schema (V4–V6) nhưng code chưa dùng:
- Lessons + lesson contents/attachments/sections
- Progress theo enrollment
- Assignment/quiz/submission
- Class sessions + attendance QR

### 2.4. Quản trị hệ thống
- Permission-based authorization (DB có permissions nhưng code chưa áp dụng)
- Audit log nghiệp vụ (ai đổi điểm/ai hủy enrollment/ai publish course)
- Import/export (CSV/Excel) user/subject/course/enrollment
- Thống kê & dashboard (theo khoa/giảng viên/học kỳ)

## 3) Đề xuất tính năng bổ sung (ưu tiên theo giai đoạn)

### Giai đoạn 1 (tăng độ “đủ dùng” cho nội bộ)
- Chuẩn hoá mô hình “Course vs Lớp học phần” và “Teacher identity”
- CRUD departments/teachers, gán teacher cho lớp
- Enrollment rules: giới hạn sĩ số + chặn đăng ký khi quá hạn/đóng đăng ký
- Lịch học tối thiểu: lưu ngày/giờ/phòng cho lớp học phần (1..n buổi)
- Báo cáo: danh sách lớp, danh sách SV theo lớp, export CSV

### Giai đoạn 2 (đào tạo có đánh giá/điểm danh)
- Attendance theo buổi + QR check-in (tận dụng schema V6)
- Gradebook: điểm thành phần + tổng kết + lịch sử chỉnh sửa điểm
- Thông báo: email/app notification khi đổi lịch/điểm/đóng đăng ký

### Giai đoạn 3 (e-learning đầy đủ)
- Lesson/content management + progress tracking (V4)
- Assignment/quiz/submission + auto-grade cơ bản (V5)
- Video pipeline (xem 04-Video-Extension.md)

## 4) Các rủi ro kỹ thuật cần xử lý sớm

- Flyway đang tắt nhưng migrations khá lớn; nếu muốn mở rộng schema có kiểm soát, nên chuyển về “Flyway là nguồn sự thật” và tắt `ddl-auto=update`.
- Một số docs trong repo đang lệch so với schema/code hiện tại; cần đồng bộ để giảm chi phí onboarding/bảo trì.

