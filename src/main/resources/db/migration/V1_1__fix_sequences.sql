-- =====================================================
-- V1_PATCH: Sequence Reset & Compatibility Fixes
-- =====================================================
-- Mục đích: Fix sequence sau khi V1 insert với explicit IDs
--           Ensure compatibility với các migrations tiếp theo
-- =====================================================
-- CHÚ Ý: File này PHẢI chạy SAU V1 và TRƯỚC V2
--        Rename thành: V1_1__fix_sequences.sql
-- =====================================================

-- =====================================================
-- 1. RESET ALL SEQUENCES
-- =====================================================
-- PostgreSQL sequences không tự động update khi insert với explicit ID
-- Phải manually reset về giá trị MAX hiện tại

-- Reset semesters sequence
SELECT setval('semesters_id_seq', (SELECT COALESCE(MAX(id), 1) FROM semesters), true);

-- Reset subjects sequence
SELECT setval('subjects_id_seq', (SELECT COALESCE(MAX(id), 1) FROM subjects), true);

-- Reset users sequence
SELECT setval('users_id_seq', (SELECT COALESCE(MAX(id), 1) FROM users), true);

-- Reset courses sequence
SELECT setval('courses_id_seq', (SELECT COALESCE(MAX(id), 1) FROM courses), true);

-- Reset enrollments sequence
SELECT setval('enrollments_id_seq', (SELECT COALESCE(MAX(id), 1) FROM enrollments), true);

-- =====================================================
-- 2. ADD MISSING INDEXES (Performance Optimization)
-- =====================================================
-- V1 thiếu một số indexes quan trọng cho foreign keys

-- Users table - email index (for login queries)
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Users table - username index (for login queries)
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- Users table - role index (for authorization queries)
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- Courses table - foreign key indexes
CREATE INDEX IF NOT EXISTS idx_courses_semester_id ON courses(semester_id);
CREATE INDEX IF NOT EXISTS idx_courses_subject_id ON courses(subject_id);
CREATE INDEX IF NOT EXISTS idx_courses_teacher_id ON courses(teacher_id);
CREATE INDEX IF NOT EXISTS idx_courses_active ON courses(active);

-- Enrollments table - foreign key indexes
CREATE INDEX IF NOT EXISTS idx_enrollments_course_id ON enrollments(course_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_student_id ON enrollments(student_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_status ON enrollments(status);

-- Subjects table - indexes
CREATE INDEX IF NOT EXISTS idx_subjects_code ON subjects(code);
CREATE INDEX IF NOT EXISTS idx_subjects_active ON subjects(active);

-- Semesters table - indexes
CREATE INDEX IF NOT EXISTS idx_semesters_code ON semesters(code);
CREATE INDEX IF NOT EXISTS idx_semesters_active ON semesters(active);
CREATE INDEX IF NOT EXISTS idx_semesters_dates ON semesters(start_date, end_date);

-- =====================================================
-- 3. ADD UPDATED_AT TRIGGER (Auto-update timestamps)
-- =====================================================
-- V1 có updated_at column nhưng không tự động update
-- Tạo trigger để auto-update khi record thay đổi

-- Create generic function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply to all tables
CREATE TRIGGER trg_update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_update_courses_updated_at
    BEFORE UPDATE ON courses
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_update_enrollments_updated_at
    BEFORE UPDATE ON enrollments
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_update_subjects_updated_at
    BEFORE UPDATE ON subjects
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_update_semesters_updated_at
    BEFORE UPDATE ON semesters
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- 4. VALIDATE SEED DATA
-- =====================================================
-- Verify rằng seed data đã được insert đúng

DO $$
DECLARE
    user_count INTEGER;
    course_count INTEGER;
    subject_count INTEGER;
    semester_count INTEGER;
BEGIN
    -- Count records
    SELECT COUNT(*) INTO user_count FROM users;
    SELECT COUNT(*) INTO course_count FROM courses;
    SELECT COUNT(*) INTO subject_count FROM subjects;
    SELECT COUNT(*) INTO semester_count FROM semesters;
    
    -- Validate
    IF user_count < 3 THEN
        RAISE EXCEPTION 'Seed data incomplete: Expected at least 3 users, found %', user_count;
    END IF;
    
    IF course_count < 1 THEN
        RAISE EXCEPTION 'Seed data incomplete: Expected at least 1 course, found %', course_count;
    END IF;
    
    IF subject_count < 3 THEN
        RAISE EXCEPTION 'Seed data incomplete: Expected at least 3 subjects, found %', subject_count;
    END IF;
    
    IF semester_count < 2 THEN
        RAISE EXCEPTION 'Seed data incomplete: Expected at least 2 semesters, found %', semester_count;
    END IF;
    
    -- Log success
    RAISE NOTICE 'Seed data validation PASSED: % users, % courses, % subjects, % semesters',
        user_count, course_count, subject_count, semester_count;
END $$;

-- =====================================================
-- GIẢI THÍCH:
-- =====================================================
-- 1. SEQUENCE RESET:
--    - PostgreSQL SEQUENCE không tự động sync khi insert với explicit ID
--    - Nếu không reset, insert tiếp theo sẽ generate ID đã tồn tại → CONFLICT
--    - setval(..., true) = set sequence VÀ mark là "đã dùng"
--
-- 2. MISSING INDEXES:
--    - V1 chỉ có UNIQUE constraints, thiếu indexes cho queries
--    - Foreign keys PHẢI có index (performance)
--    - Email/username cần index cho login queries
--
-- 3. UPDATED_AT TRIGGER:
--    - V1 có column updated_at nhưng không auto-update
--    - Trigger đảm bảo updated_at luôn đúng khi UPDATE
--    - Reusable function cho tất cả tables
--
-- 4. VALIDATION:
--    - Đảm bảo seed data chạy thành công
--    - Fail-fast nếu có vấn đề
--    - Helpful error messages
-- =====================================================
