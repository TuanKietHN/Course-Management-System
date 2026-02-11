# 01. Database review (migrations ↔ entity/domain)

## 1) Thiết lập hiện tại

### Backend đang chạy theo hướng nào?
- `spring.flyway.enabled=false` (tắt Flyway auto-migrate)
- `spring.jpa.hibernate.ddl-auto=update` (Hibernate tự “update schema” theo entity)

Tổ hợp này có rủi ro drift schema: migrations mô tả một schema “chuẩn”, nhưng runtime lại có thể tạo/đổi schema theo entity. Khi hai nguồn sự thật không đồng bộ, sẽ khó đảm bảo production có schema giống môi trường dev.

Nguồn:
- `application.properties`: `spring.flyway.enabled=false`, `spring.jpa.hibernate.ddl-auto=update`
- Migrations: `src/main/resources/db/migration/V1..V8`

## 2) Nhóm bảng theo bounded context

### 2.1. Auth / IAM

Schema theo migrations:
- `users` (V1, V7 drop `role`, V8 add `avatar`)
- `roles`, `permissions`, `role_permissions`, `user_roles` (V2)
- `refresh_tokens`, `password_reset_tokens` (V2)

Entity hiện có:
- `UserEntity` → `users`
- `RoleEntity`, `UserRoleEntity` → `roles`, `user_roles`

Nhận xét:
- Dữ liệu “permission” đã có trong DB (migrations) nhưng hiện chưa thấy entity/service/controller nào dùng `permissions`, `role_permissions` để authorize theo permission. Code hiện thiên về `@PreAuthorize(hasRole(...))`.

### 2.2. Academic (quản lý học vụ cơ bản)

Schema theo migrations (đang được API sử dụng):
- `semesters` (V1)
- `subjects` (V1, V3 mở rộng)
- `courses` (V1, V3 mở rộng)
- `enrollments` (V1)

Entity hiện có:
- `SemesterEntity` → `semesters`
- `SubjectEntity` → `subjects`
- `CourseEntity` → `courses`
- `EnrollmentEntity` → `enrollments`
- `DepartmentEntity` → `departments` (V3)
- `TeacherEntity` → `teachers` (V3)

Nhận xét nổi bật:
- API/Service hiện gọi `Course` là “lớp học phần”, tức một course gắn với `semester_id` và `subject_id` (mô hình V1).
- Migrations V4–V6 lại coi `courses` là “khóa học e-learning” để gắn lesson/quiz/attendance.

### 2.3. Lesson / Assessment / Attendance (đã có trong DB, chưa có module Java)

Schema theo migrations:
- V4: `lessons`, `lesson_contents`, `lesson_attachments`, `lesson_sections`, `student_lesson_progress`
- V5: `assignments`, `assignment_submissions`, `assignment_submission_files`, `quizzes`, `quiz_questions`, `quiz_answers`, `quiz_attempts`, `quiz_attempt_answers`
- V6: `class_sessions`, `attendance`, `attendance_qr_codes`

Hiện trạng code:
- Chưa có package/module Java tương ứng dưới `vn.com.nws.cms.modules.*` cho lesson/assessment/attendance, nên các bảng này đang “chưa được dùng” ở layer ứng dụng.

## 3) Các điểm lệch schema ↔ entity/domain cần chú ý

### 3.1. `courses.teacher_id`: Users hay Teachers?

V1:
- `courses.teacher_id` FK → `users(id)`

V3:
- Tạo bảng `teachers(user_id, ...)`
- View `v_courses_with_teacher` JOIN `courses.teacher_id = teachers.id` (ngụ ý `teacher_id` là teachers.id)
- Nhưng migration không ALTER FK của `courses.teacher_id`, nên về mặt schema gốc vẫn theo V1

Trong code:
- `CourseEntity.teacher` đang map sang `UserEntity` (users)

Hệ quả:
- View `v_courses_with_teacher` có nguy cơ sai (JOIN nhầm cột) nếu `teacher_id` thực tế là user_id.

Khuyến nghị chuẩn hoá (chọn 1 trong 2 hướng):
1. Hướng A (đơn giản): giữ `courses.teacher_id` → `users(id)` và sửa view để JOIN `teachers.user_id = courses.teacher_id`.
2. Hướng B (giàu profile): migrate `courses.teacher_id` → `teachers(id)` + sửa entity/domain/service theo `Teacher`.

### 3.2. `subjects.credit` vs `subjects.credits`

V1 tạo cột `credit`.
V3 thêm cột `credits` (mặc định 3).

Trong code:
- `SubjectEntity`/domain sử dụng `credits`.

Hệ quả:
- Schema có khả năng tồn tại đồng thời 2 cột cùng ý nghĩa; dữ liệu seed ở V1 đi vào `credit` sẽ không tự “điền” sang `credits` nếu không có migration chuyển đổi.

Khuyến nghị:
- Viết migration hợp nhất (copy dữ liệu `credit` → `credits` nếu `credits` null, sau đó drop `credit`) hoặc đổi entity dùng `credit` cho đến khi schema được làm sạch.

### 3.3. Nullability/constraints giữa migration và entity

Ví dụ:
- V3 thêm `courses.credits INTEGER` không NOT NULL, nhưng `CourseEntity.credits` lại đánh dấu `nullable=false`.

Hệ quả:
- Nếu schema thực sự theo migrations (không NOT NULL), insert/update qua JPA có thể tạo dữ liệu null mà code không dự tính; hoặc nếu Hibernate đã “update schema” thành NOT NULL thì dữ liệu migration cũ có thể gây lỗi.

Khuyến nghị:
- Quy về 1 nguồn schema (ưu tiên Flyway), tắt `ddl-auto=update`, và đảm bảo entity phản ánh đúng constraints trong migrations.

## 4) Kết luận nhanh về DB readiness cho “quản lý khóa học nội bộ”

Phần đã “đủ dùng” (có API + entity + domain):
- Auth cơ bản + JWT
- Quản lý người dùng (admin) + avatar (MinIO)
- Môn học (Subject), Học kỳ (Semester), Lớp học phần (Course), Đăng ký (Enrollment)

Phần đã “có schema” nhưng chưa có code để thành tính năng:
- Lesson/content/progress (V4)
- Assignment/Quiz/submission (V5)
- Attendance/session/QR (V6)

Điểm cần xử lý trước khi mở rộng:
- Chuẩn hoá khái niệm Course (khóa học) vs Course offering (lớp học phần)
- Chuẩn hoá quan hệ Teacher (users vs teachers)
- Chọn 1 cơ chế quản trị schema (Flyway hoặc Hibernate), tránh chạy song song.

