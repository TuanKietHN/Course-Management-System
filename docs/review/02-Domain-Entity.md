# 02. Domain/entity review cho hệ thống quản lý khóa học nội bộ

## 1) Mục tiêu nghiệp vụ “quản lý khóa học nội bộ” (tham chiếu)

Một hệ thống nội bộ tối thiểu thường có:
- Danh mục môn/khóa học (catalog)
- Kỳ học (semester/term)
- Lớp học phần/đợt mở lớp (offering/section) kèm lịch học, phòng, giảng viên
- Đăng ký học phần (enrollment) + kiểm tra điều kiện (tiên quyết, trùng lịch, quota)
- Điểm số/đánh giá (gradebook)
- Điểm danh (attendance) (nếu có học trực tiếp/online theo buổi)
- Học liệu/nội dung (lesson/content) (nếu có e-learning)

## 2) Hiện trạng domain model vs entity

### 2.1. Academic core đang có

Trong code hiện tại:
- `Subject` đại diện “môn học” (catalog)
- `Semester` đại diện “học kỳ”
- `Course` đang được gọi và expose như “lớp học phần”
  - Có `semester`, `subject`, `teacher`, quota
- `Enrollment` là đăng ký học phần (student ↔ course)

Đây là một “skeleton” phù hợp để bắt đầu quản lý học vụ cơ bản.

### 2.2. Vấn đề lớn: khái niệm `Course`

Trong API:
- Controller ghi rõ `Course` = “lớp học phần”.

Trong DB migrations V4–V6:
- `lessons.course_id`, `assignments.course_id`, `class_sessions.course_id` đều gắn vào `courses`
- Nghĩa là `courses` được dùng như “khóa học e-learning” (một khóa học có lesson/quiz/session)

=> Hai cách hiểu `Course` đang bị trộn.

Đề xuất chuẩn hoá mô hình (khuyến nghị):
- Tách 2 aggregate:
  1. `Course` (khóa học/catalog) – tên, mô tả, thumbnail, level, language, credits…
  2. `CourseOffering`/`CourseSection` (lớp học phần) – course_id, semester_id, teacher_id, quota, lịch học…
- Khi đó:
  - `lessons/assignments/quizzes` gắn với `Course` (khóa học) hoặc gắn với offering tuỳ nghiệp vụ (nếu nội dung thay đổi theo lớp)
  - `enrollments` gắn với offering (vì sinh viên đăng ký theo lớp mở trong kỳ)

Nếu muốn giữ schema hiện tại:
- Có thể rename khái niệm ở code cho đúng: `CourseEntity` → `CourseSectionEntity`
- Và tạo thêm bảng `courses_catalog` (hoặc dùng `subjects` làm catalog và `courses` là offering)
  - Tuy nhiên migrations V4–V6 sẽ cần chỉnh lại FK nếu tiếp tục dùng `subjects` làm “khóa học e-learning”.

## 3) Teacher/Department modeling

Hiện có cả:
- `users` (tài khoản đăng nhập)
- `teachers` (profile giảng viên) liên kết 1-1 với `users`
- `departments` có `head_teacher_id` FK → `teachers(id)`

Nhưng:
- `CourseEntity.teacher` hiện map tới `UserEntity`
- Không có module/API quản lý teachers/departments nên các bảng V3 chưa “sống” trong hệ thống

Đề xuất:
- Nếu hệ thống nội bộ cần quản lý giảng viên theo “nhân sự” (employee_code, khoa, học hàm…): dùng `Teacher` làm aggregate, và để `CourseOffering.teacher_id` tham chiếu `teachers`.
- Nếu chỉ cần “tài khoản có role TEACHER”: bỏ teachers table hoặc chỉ dùng như profile mở rộng nhưng phải thống nhất FK/join.

## 4) Domain ↔ entity mismatch đang gây rủi ro khi mở rộng

### 4.1. Course domain thiếu các field đã có trong entity/migration

`CourseEntity` có thêm nhiều trường (description/status/credits/thumbnail/department/level/language/enrollment_dates…),
nhưng `Course` (domain) và DTO/service hiện chưa expose hoặc set các trường này.

Rủi ro:
- Dữ liệu có thể không được ghi/đọc đúng (tuỳ MapStruct mapping và cách save)
- Khó triển khai workflow “draft/published/archived” hoặc enrollment window

Khuyến nghị:
- Nếu quyết định `courses` = “khóa học e-learning”: mở rộng domain/DTO/service cho các field trên.
- Nếu quyết định `courses` = “lớp học phần”: di chuyển các field “e-learning/course marketing” sang bảng/aggregate “Course”.

### 4.2. Array columns (prerequisite IDs) chưa được map

Migrations dùng `BIGINT[]` cho `subjects.prerequisite_subject_ids`, `lessons.prerequisite_lesson_ids`, `quiz_questions.correct_answer_ids`.

Hiện code chưa map kiểu array này trong JPA.

Khuyến nghị:
- Dùng table quan hệ chuẩn hoá (many-to-many) thay vì array nếu muốn query/constraint chặt chẽ
- Hoặc dùng custom Hibernate type cho Postgres array nếu chấp nhận trade-off

## 5) Kết luận: đã phù hợp chưa?

### Đủ cho “quản lý học vụ cơ bản”
- CRUD Subject/Semester/Course(lớp học phần)
- Enrollment + nhập điểm tổng (grade) theo enrollment
- Quản lý người dùng + phân quyền role-based

### Chưa đủ cho “quản lý khóa học nội bộ” theo nghĩa đầy đủ
Thiếu hoặc chưa kết nối:
- Teacher/Department module (API + nghiệp vụ)
- Lịch học/phòng học/buổi học (class sessions) gắn với offering
- Attendance/gradebook chi tiết
- Quản lý nội dung học liệu (lessons/contents), progress theo enrollment

Để mở rộng e-learning/video ổn định:
- Cần chuẩn hoá “Course vs Offering” và “Teacher identity” trước, vì các migrations V4–V6 phụ thuộc trực tiếp vào các khái niệm này.

