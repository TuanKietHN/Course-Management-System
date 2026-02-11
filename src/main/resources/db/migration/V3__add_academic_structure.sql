-- =====================================================
-- Migration V3: Academic Structure Enhancement
-- =====================================================
-- Mục đích: Thêm cấu trúc tổ chức học thuật và cải tiến bảng courses
-- =====================================================

-- =====================================================
-- 1. DEPARTMENTS TABLE
-- =====================================================
-- Lưu thông tin Khoa/Phòng ban
-- Thiết kế: Hỗ trợ hierarchical structure (department có thể có parent)
CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,       -- Mã khoa: VD: IT, BUS, ENG
    name VARCHAR(255) NOT NULL,             -- Tên: Khoa Công nghệ Thông tin
    description TEXT,
    parent_id BIGINT,                       -- NULL = khoa gốc, NOT NULL = bộ phận con
    head_teacher_id BIGINT,                 -- Trưởng khoa (sẽ link sau khi có teachers table)
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (parent_id) REFERENCES departments(id) ON DELETE SET NULL
);

-- Index
CREATE INDEX idx_departments_code ON departments(code);
CREATE INDEX idx_departments_active ON departments(active);
CREATE INDEX idx_departments_parent_id ON departments(parent_id);

-- =====================================================
-- 2. TEACHERS TABLE
-- =====================================================
-- Lưu thông tin chi tiết giảng viên
-- Thiết kế: Tách riêng khỏi users để linh hoạt (1 iam có thể có profile teacher riêng)
CREATE TABLE teachers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,         -- Link đến users table
    employee_code VARCHAR(50) NOT NULL UNIQUE, -- Mã nhân viên: GV001
    department_id BIGINT,                   -- Thuộc khoa nào
    specialization VARCHAR(255),            -- Chuyên môn: Backend Development, Data Science
    title VARCHAR(100),                     -- Học hàm: TS, PGS, GS
    bio TEXT,                               -- Tiểu sử giảng viên
    office_location VARCHAR(255),           -- Phòng làm việc: A101
    office_hours VARCHAR(255),              -- Giờ làm việc: Mon-Fri 9-11AM
    phone VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
);

-- Index
CREATE INDEX idx_teachers_user_id ON teachers(user_id);
CREATE INDEX idx_teachers_employee_code ON teachers(employee_code);
CREATE INDEX idx_teachers_department_id ON teachers(department_id);
CREATE INDEX idx_teachers_active ON teachers(active);

-- =====================================================
-- 3. UPDATE DEPARTMENTS - Add FK to teachers
-- =====================================================
-- Bây giờ teachers table đã có, thêm foreign key cho head_teacher_id
ALTER TABLE departments
    ADD CONSTRAINT fk_departments_head_teacher
    FOREIGN KEY (head_teacher_id) REFERENCES teachers(id) ON DELETE SET NULL;

CREATE INDEX idx_departments_head_teacher_id ON departments(head_teacher_id);

-- =====================================================
-- 4. ALTER COURSES TABLE - Enhancement
-- =====================================================
-- Thêm các trường cần thiết cho hệ thống E-learning hiện đại

-- Thêm description (mô tả chi tiết khóa học)
ALTER TABLE courses ADD COLUMN description TEXT;

-- Thêm thumbnail (ảnh đại diện khóa học)
ALTER TABLE courses ADD COLUMN thumbnail_url VARCHAR(500);

-- Thêm status (trạng thái: draft, published, archived)
ALTER TABLE courses ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'draft';

-- Thêm credits (số tín chỉ)
ALTER TABLE courses ADD COLUMN credits INTEGER;

-- Thêm department_id (khóa học thuộc khoa nào)
ALTER TABLE courses ADD COLUMN department_id BIGINT;

-- Thêm duration_weeks (thời lượng khóa học - tuần)
ALTER TABLE courses ADD COLUMN duration_weeks INTEGER;

-- Thêm level (trình độ: beginner, intermediate, advanced)
ALTER TABLE courses ADD COLUMN level VARCHAR(20);

-- Thêm language (ngôn ngữ giảng dạy)
ALTER TABLE courses ADD COLUMN language VARCHAR(10) DEFAULT 'vi';

-- Thêm enrollment_start_date, enrollment_end_date
ALTER TABLE courses ADD COLUMN enrollment_start_date DATE;
ALTER TABLE courses ADD COLUMN enrollment_end_date DATE;

-- Thêm foreign key constraints
ALTER TABLE courses
    ADD CONSTRAINT fk_courses_department
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL;

-- Thêm constraint cho status (chỉ cho phép các giá trị hợp lệ)
ALTER TABLE courses
    ADD CONSTRAINT chk_courses_status
    CHECK (status IN ('draft', 'published', 'archived'));

-- Thêm constraint cho level
ALTER TABLE courses
    ADD CONSTRAINT chk_courses_level
    CHECK (level IS NULL OR level IN ('beginner', 'intermediate', 'advanced'));

-- Index mới
CREATE INDEX idx_courses_department_id ON courses(department_id);
CREATE INDEX idx_courses_status ON courses(status);
CREATE INDEX idx_courses_level ON courses(level);
CREATE INDEX idx_courses_enrollment_dates ON courses(enrollment_start_date, enrollment_end_date);

-- =====================================================
-- 5. ALTER SUBJECTS TABLE - Enhancement
-- =====================================================
-- Cải tiến subjects để phù hợp với cấu trúc academic

-- Thêm department_id (môn học thuộc khoa)
ALTER TABLE subjects ADD COLUMN department_id BIGINT;

-- Thêm prerequisite_subjects (môn học tiên quyết - lưu dạng array ID)
ALTER TABLE subjects ADD COLUMN prerequisite_subject_ids BIGINT[];

-- Thêm credits (tín chỉ môn học)
ALTER TABLE subjects ADD COLUMN credits INTEGER NOT NULL DEFAULT 3;

-- Thêm theory_hours, practice_hours
ALTER TABLE subjects ADD COLUMN theory_hours INTEGER DEFAULT 0;
ALTER TABLE subjects ADD COLUMN practice_hours INTEGER DEFAULT 0;

-- Thêm foreign key
ALTER TABLE subjects
    ADD CONSTRAINT fk_subjects_department
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL;

-- Index
CREATE INDEX idx_subjects_department_id ON subjects(department_id);

-- =====================================================
-- 6. SEED DATA - Sample Departments
-- =====================================================
INSERT INTO departments (code, name, description, active) VALUES
    ('IT', 'Khoa Công nghệ Thông tin', 'Khoa chuyên đào tạo về lập trình, mạng máy tính, AI', TRUE),
    ('BUS', 'Khoa Quản trị Kinh doanh', 'Khoa chuyên đào tạo về quản trị, marketing, tài chính', TRUE),
    ('ENG', 'Khoa Ngoại ngữ', 'Khoa chuyên đào tạo tiếng Anh, tiếng Nhật, tiếng Trung', TRUE);

-- =====================================================
-- 7. MIGRATE EXISTING DATA
-- =====================================================
-- Update courses: set default department (IT) cho các course hiện có
UPDATE courses
SET department_id = (SELECT id FROM departments WHERE code = 'IT' LIMIT 1)
WHERE department_id IS NULL;

-- Update subjects: set default department
UPDATE subjects
SET department_id = (SELECT id FROM departments WHERE code = 'IT' LIMIT 1)
WHERE department_id IS NULL;

-- =====================================================
-- 8. CREATE VIEW - Course với Teacher Info
-- =====================================================
-- View để dễ dàng query course kèm thông tin teacher
CREATE OR REPLACE VIEW v_courses_with_teacher AS
SELECT
    c.id AS course_id,
    c.code AS course_code,
    c.name AS course_name,
    c.description,
    c.status,
    c.credits,
    c.level,
    c.semester_id,
    c.subject_id,
    c.current_students,
    c.max_students,
    s.name AS subject_name,
    sem.name AS semester_name,
    t.id AS teacher_id,
    t.employee_code,
    u.username AS teacher_username,
    u.email AS teacher_email,
    d.code AS department_code,
    d.name AS department_name
FROM courses c
LEFT JOIN subjects s ON c.subject_id = s.id
LEFT JOIN semesters sem ON c.semester_id = sem.id
LEFT JOIN teachers t ON c.teacher_id = t.id
LEFT JOIN users u ON t.user_id = u.id
LEFT JOIN departments d ON c.department_id = d.id;

-- =====================================================
-- GIẢI THÍCH DESIGN DECISIONS:
-- =====================================================
-- 1. DEPARTMENTS TABLE:
--    - Hỗ trợ hierarchical: có thể có "Khoa IT" và "Bộ môn AI" (child của IT)
--    - head_teacher_id: Quản lý trưởng khoa
--    - Dễ mở rộng: thêm fields như phone, address sau
--
-- 2. TEACHERS TABLE:
--    - Tách riêng khỏi users: 1 iam có thể có nhiều role, teacher là 1 profile
--    - employee_code: UNIQUE, dùng cho quản lý nhân sự
--    - specialization: Giúp match teacher với course phù hợp
--    - office_hours: Students biết khi nào có thể gặp teacher
--
-- 3. COURSES ENHANCEMENT:
--    - status (draft/published/archived): Workflow management
--      + draft: Teacher đang soạn
--      + published: Học sinh có thể enroll
--      + archived: Kết thúc, chỉ xem
--    - thumbnail_url: UI/UX tốt hơn
--    - enrollment dates: Kiểm soát thời gian đăng ký
--    - level: Phân loại khóa học (beginner/intermediate/advanced)
--
-- 4. SUBJECTS ENHANCEMENT:
--    - prerequisite_subject_ids: Array of IDs (Postgres support)
--      + VD: Môn "Data Structures" cần học trước "Algorithms"
--      + Query: WHERE 123 = ANY(prerequisite_subject_ids)
--    - theory_hours + practice_hours: Tính toán workload
--
-- 5. VIEW v_courses_with_teacher:
--    - Tránh viết JOIN nhiều lần trong Service layer
--    - Performance: Postgres optimize view tốt
--    - Dễ maintain: Sửa view thay vì sửa nhiều query
-- =====================================================
