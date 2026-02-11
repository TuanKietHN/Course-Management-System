-- =====================================================
-- Migration V4: Lesson & Content Management System
-- =====================================================
-- Mục đích: Xây dựng hệ thống bài học hỗ trợ đa dạng content types
--           (Video, PDF, SCORM, HTML, Quiz...)
-- =====================================================

-- =====================================================
-- 1. LESSONS TABLE
-- =====================================================
-- Lưu thông tin bài học (1 course có nhiều lessons)
-- Thiết kế: Support ordering, prerequisites, và different types
CREATE TABLE lessons (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,            -- Tiêu đề: "Bài 1: Introduction to Java"
    description TEXT,                       -- Mô tả ngắn bài học
    lesson_type VARCHAR(50) NOT NULL,       -- video, reading, quiz, assignment, scorm
    order_index INTEGER NOT NULL,           -- Thứ tự trong course (1, 2, 3...)
    duration_minutes INTEGER,               -- Thời lượng ước tính (phút)
    is_preview BOOLEAN DEFAULT FALSE,       -- Cho phép preview (khi chưa enroll)
    is_mandatory BOOLEAN DEFAULT TRUE,      -- Bắt buộc học (ảnh hưởng progress)
    prerequisite_lesson_ids BIGINT[],       -- Các bài cần học trước (array)
    points INTEGER DEFAULT 0,               -- Điểm khi hoàn thành (cho gamification)
    published_at TIMESTAMP,                 -- NULL = draft, NOT NULL = published
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

-- Constraints
ALTER TABLE lessons
    ADD CONSTRAINT chk_lessons_type
    CHECK (lesson_type IN ('video', 'reading', 'quiz', 'assignment', 'scorm', 'live_session', 'discussion'));

ALTER TABLE lessons
    ADD CONSTRAINT chk_lessons_order_index
    CHECK (order_index > 0);

-- Index
CREATE INDEX idx_lessons_course_id ON lessons(course_id);
CREATE INDEX idx_lessons_order_index ON lessons(course_id, order_index);
CREATE INDEX idx_lessons_type ON lessons(lesson_type);
CREATE INDEX idx_lessons_published_at ON lessons(published_at);

-- =====================================================
-- 2. LESSON_CONTENTS TABLE
-- =====================================================
-- Lưu nội dung thực tế của bài học (1 lesson có nhiều contents)
-- Thiết kế: Polymorphic content (video, text, file, embed...)
CREATE TABLE lesson_contents (
    id BIGSERIAL PRIMARY KEY,
    lesson_id BIGINT NOT NULL,
    content_type VARCHAR(50) NOT NULL,      -- video, text, pdf, scorm, embed, file
    title VARCHAR(255),                     -- Tiêu đề content (optional)
    order_index INTEGER NOT NULL,           -- Thứ tự trong lesson
    
    -- Content fields (dùng theo content_type)
    text_content TEXT,                      -- HTML content (khi type=text)
    video_url VARCHAR(500),                 -- Video URL (khi type=video)
    video_duration INTEGER,                 -- Duration (seconds)
    file_url VARCHAR(500),                  -- File URL (PDF, SCORM, etc.)
    file_size BIGINT,                       -- File size (bytes)
    embed_code TEXT,                        -- Embed HTML (YouTube, Vimeo...)
    
    -- Metadata (JSON for flexibility)
    metadata JSONB,                         -- VD: {"provider": "youtube", "video_id": "abc123"}
    
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
);

-- Constraints
ALTER TABLE lesson_contents
    ADD CONSTRAINT chk_lesson_contents_type
    CHECK (content_type IN ('video', 'text', 'pdf', 'scorm', 'embed', 'file', 'audio'));

ALTER TABLE lesson_contents
    ADD CONSTRAINT chk_lesson_contents_order_index
    CHECK (order_index > 0);

-- Index
CREATE INDEX idx_lesson_contents_lesson_id ON lesson_contents(lesson_id);
CREATE INDEX idx_lesson_contents_order_index ON lesson_contents(lesson_id, order_index);
CREATE INDEX idx_lesson_contents_type ON lesson_contents(content_type);
CREATE INDEX idx_lesson_contents_metadata ON lesson_contents USING GIN(metadata); -- For JSONB queries

-- =====================================================
-- 3. LESSON_ATTACHMENTS TABLE
-- =====================================================
-- Lưu file đính kèm bài học (slides, code samples, resources...)
-- Thiết kế: Separate table để dễ manage files
CREATE TABLE lesson_attachments (
    id BIGSERIAL PRIMARY KEY,
    lesson_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,        -- original_file_name.pdf
    file_url VARCHAR(500) NOT NULL,         -- S3/local storage URL
    file_type VARCHAR(50),                  -- pdf, zip, docx...
    file_size BIGINT,                       -- bytes
    description TEXT,                       -- Mô tả file: "Slide bài giảng"
    download_count INTEGER DEFAULT 0,       -- Số lượt download (tracking)
    is_required BOOLEAN DEFAULT FALSE,      -- File bắt buộc download?
    uploaded_by BIGINT,                     -- Teacher upload (user_id)
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Index
CREATE INDEX idx_lesson_attachments_lesson_id ON lesson_attachments(lesson_id);
CREATE INDEX idx_lesson_attachments_uploaded_by ON lesson_attachments(uploaded_by);

-- =====================================================
-- 4. LESSON_SECTIONS TABLE (Optional - for grouping)
-- =====================================================
-- Nhóm các lessons thành sections (VD: Section 1: Introduction, Section 2: Core Concepts)
-- Thiết kế: Hierarchical structure
CREATE TABLE lesson_sections (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,            -- "Section 1: Introduction to Java"
    description TEXT,
    order_index INTEGER NOT NULL,           -- Thứ tự section trong course
    is_collapsed BOOLEAN DEFAULT FALSE,     -- UI: section collapsed by default?
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

-- Index
CREATE INDEX idx_lesson_sections_course_id ON lesson_sections(course_id);
CREATE INDEX idx_lesson_sections_order_index ON lesson_sections(course_id, order_index);

-- =====================================================
-- 5. ALTER LESSONS - Add section_id
-- =====================================================
-- Link lesson to section
ALTER TABLE lessons ADD COLUMN section_id BIGINT;

ALTER TABLE lessons
    ADD CONSTRAINT fk_lessons_section
    FOREIGN KEY (section_id) REFERENCES lesson_sections(id) ON DELETE SET NULL;

CREATE INDEX idx_lessons_section_id ON lessons(section_id);

-- =====================================================
-- 6. STUDENT_LESSON_PROGRESS TABLE
-- =====================================================
-- Theo dõi tiến độ học của student cho từng lesson
-- Thiết kế: Track completion, time spent, last accessed
CREATE TABLE student_lesson_progress (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,             -- user_id
    lesson_id BIGINT NOT NULL,
    enrollment_id BIGINT NOT NULL,          -- Link to enrollment
    
    -- Progress tracking
    status VARCHAR(20) NOT NULL DEFAULT 'not_started', -- not_started, in_progress, completed
    completion_percentage INTEGER DEFAULT 0, -- 0-100
    time_spent_seconds INTEGER DEFAULT 0,   -- Tổng thời gian học (seconds)
    
    -- Timestamps
    started_at TIMESTAMP,                   -- Lần đầu tiên học
    completed_at TIMESTAMP,                 -- Hoàn thành lúc nào
    last_accessed_at TIMESTAMP,             -- Lần cuối truy cập
    
    -- Metadata (JSON for flexibility)
    progress_data JSONB,                    -- VD: {"video_position": 120, "quiz_score": 85}
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(id) ON DELETE CASCADE,
    
    UNIQUE(student_id, lesson_id) -- 1 student chỉ có 1 progress record cho 1 lesson
);

-- Constraints
ALTER TABLE student_lesson_progress
    ADD CONSTRAINT chk_student_lesson_progress_status
    CHECK (status IN ('not_started', 'in_progress', 'completed'));

ALTER TABLE student_lesson_progress
    ADD CONSTRAINT chk_student_lesson_progress_percentage
    CHECK (completion_percentage >= 0 AND completion_percentage <= 100);

-- Index
CREATE INDEX idx_student_lesson_progress_student_id ON student_lesson_progress(student_id);
CREATE INDEX idx_student_lesson_progress_lesson_id ON student_lesson_progress(lesson_id);
CREATE INDEX idx_student_lesson_progress_enrollment_id ON student_lesson_progress(enrollment_id);
CREATE INDEX idx_student_lesson_progress_status ON student_lesson_progress(status);
CREATE INDEX idx_student_lesson_progress_data ON student_lesson_progress USING GIN(progress_data);

-- =====================================================
-- 7. CREATE VIEW - Course Progress Summary
-- =====================================================
-- View tổng hợp tiến độ học của student cho từng course
CREATE OR REPLACE VIEW v_student_course_progress AS
SELECT
    e.id AS enrollment_id,
    e.student_id,
    e.course_id,
    c.name AS course_name,
    COUNT(l.id) AS total_lessons,
    COUNT(CASE WHEN slp.status = 'completed' THEN 1 END) AS completed_lessons,
    ROUND(
        (COUNT(CASE WHEN slp.status = 'completed' THEN 1 END)::NUMERIC / 
         NULLIF(COUNT(l.id), 0)) * 100, 
        2
    ) AS completion_percentage,
    SUM(COALESCE(slp.time_spent_seconds, 0)) AS total_time_spent_seconds,
    MAX(slp.last_accessed_at) AS last_accessed_at
FROM enrollments e
JOIN courses c ON e.course_id = c.id
LEFT JOIN lessons l ON l.course_id = c.id AND l.active = TRUE AND l.is_mandatory = TRUE
LEFT JOIN student_lesson_progress slp ON slp.lesson_id = l.id AND slp.student_id = e.student_id
GROUP BY e.id, e.student_id, e.course_id, c.name;

-- =====================================================
-- 8. FUNCTION - Auto Update Enrollment Progress
-- =====================================================
-- Function tự động cập nhật progress của enrollment khi lesson progress thay đổi
CREATE OR REPLACE FUNCTION update_enrollment_progress()
RETURNS TRIGGER AS $$
BEGIN
    -- Tính lại completion percentage của enrollment
    UPDATE enrollments e
    SET 
        updated_at = CURRENT_TIMESTAMP
        -- Note: Nếu enrollments có field 'progress', update ở đây
    WHERE e.id = NEW.enrollment_id;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger
CREATE TRIGGER trg_update_enrollment_progress
AFTER INSERT OR UPDATE ON student_lesson_progress
FOR EACH ROW
EXECUTE FUNCTION update_enrollment_progress();

-- =====================================================
-- GIẢI THÍCH DESIGN DECISIONS:
-- =====================================================
-- 1. LESSONS TABLE:
--    - lesson_type: Phân loại để render UI khác nhau
--      + video: Player với progress tracking
--      + quiz: Interactive assessment
--      + scorm: SCORM player (E-learning standard)
--    - prerequisite_lesson_ids: Array cho flexible dependencies
--      + VD: Lesson 5 cần hoàn thành Lesson 2 VÀ Lesson 3
--      + Query: WHERE NEW.lesson_id = ANY(prerequisite_lesson_ids)
--    - is_preview: Marketing (cho học sinh xem trước khi enroll)
--    - points: Gamification (leaderboard, achievements)
--
-- 2. LESSON_CONTENTS TABLE:
--    - Polymorphic content: 1 lesson có thể có nhiều content types
--      + VD: Lesson "Introduction" có: video + text + pdf slides
--    - metadata JSONB: Flexible cho từng content type
--      + Video: {provider: "youtube", video_id: "...", captions: [...]}
--      + SCORM: {scorm_version: "1.2", manifest_url: "..."}
--    - GIN Index trên JSONB: Query nhanh metadata
--
-- 3. LESSON_SECTIONS:
--    - Optional: Một số course ngắn không cần sections
--    - UI tốt hơn: "Collapse" section để dễ navigate
--    - Typical structure:
--      Course
--        └─ Section 1: Introduction
--            ├─ Lesson 1.1: Overview
--            └─ Lesson 1.2: Setup
--        └─ Section 2: Core Concepts
--            ├─ Lesson 2.1: Variables
--            └─ Lesson 2.2: Functions
--
-- 4. STUDENT_LESSON_PROGRESS:
--    - UNIQUE(student_id, lesson_id): 1 record duy nhất
--    - progress_data JSONB: Lưu state cụ thể
--      + Video: {"last_position": 120} (giây thứ 120)
--      + Quiz: {"answers": [...], "score": 85}
--      + SCORM: {suspend_data: "...", completion_status: "completed"}
--    - time_spent_seconds: Analytics (xem student dành bao nhiêu thời gian)
--
-- 5. VIEW v_student_course_progress:
--    - Aggregation: Tổng hợp từ lesson progress
--    - Performance: Có thể tạo Materialized View nếu data lớn
--    - Use case: Dashboard "Bạn đã hoàn thành 75% khóa học"
--
-- 6. TRIGGER update_enrollment_progress:
--    - Real-time update: Khi học xong 1 lesson, enrollment progress tự động cập nhật
--    - Alternative: Scheduled job (batch update mỗi 5 phút)
--      + Pro: Giảm DB load
--      + Con: Progress không real-time
-- =====================================================
