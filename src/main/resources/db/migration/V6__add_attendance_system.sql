-- =====================================================
-- Migration V6: Attendance System & Performance Optimization
-- =====================================================
-- Mục đích: Hệ thống điểm danh và các indexes/views bổ sung
-- =====================================================

-- =====================================================
-- 1. CLASS_SESSIONS TABLE
-- =====================================================
-- Lưu thông tin buổi học (cho live classes)
-- Thiết kế: Support both online and offline sessions
CREATE TABLE class_sessions (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    lesson_id BIGINT,                       -- Link to lesson (optional)
    
    -- Session info
    title VARCHAR(255) NOT NULL,            -- "Session 1: Introduction"
    session_type VARCHAR(50) NOT NULL,      -- lecture, lab, discussion, exam
    session_mode VARCHAR(20) NOT NULL,      -- online, offline, hybrid
    
    -- Schedule
    scheduled_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    duration_minutes INTEGER,               -- Auto calculate
    
    -- Location
    room_number VARCHAR(50),                -- Phòng học (offline)
    building VARCHAR(100),                  -- Tòa nhà
    online_meeting_url VARCHAR(500),        -- Zoom, Google Meet link (online)
    online_meeting_password VARCHAR(100),
    
    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'scheduled', -- scheduled, ongoing, completed, cancelled
    actual_start_time TIMESTAMP,
    actual_end_time TIMESTAMP,
    
    -- Notes
    description TEXT,
    notes TEXT,                             -- Ghi chú của teacher sau buổi học
    
    teacher_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE SET NULL,
    FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE
);

-- Constraints
ALTER TABLE class_sessions
    ADD CONSTRAINT chk_class_sessions_type
    CHECK (session_type IN ('lecture', 'lab', 'discussion', 'exam', 'review', 'workshop'));

ALTER TABLE class_sessions
    ADD CONSTRAINT chk_class_sessions_mode
    CHECK (session_mode IN ('online', 'offline', 'hybrid'));

ALTER TABLE class_sessions
    ADD CONSTRAINT chk_class_sessions_status
    CHECK (status IN ('scheduled', 'ongoing', 'completed', 'cancelled', 'postponed'));

ALTER TABLE class_sessions
    ADD CONSTRAINT chk_class_sessions_time
    CHECK (end_time > start_time);

-- Index
CREATE INDEX idx_class_sessions_course_id ON class_sessions(course_id);
CREATE INDEX idx_class_sessions_lesson_id ON class_sessions(lesson_id);
CREATE INDEX idx_class_sessions_teacher_id ON class_sessions(teacher_id);
CREATE INDEX idx_class_sessions_scheduled_date ON class_sessions(scheduled_date);
CREATE INDEX idx_class_sessions_status ON class_sessions(status);

-- =====================================================
-- 2. ATTENDANCE TABLE
-- =====================================================
-- Lưu điểm danh cho từng session
-- Thiết kế: Support manual and auto attendance (QR code, face recognition)
CREATE TABLE attendance (
    id BIGSERIAL PRIMARY KEY,
    class_session_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    enrollment_id BIGINT NOT NULL,
    
    -- Attendance status
    status VARCHAR(20) NOT NULL DEFAULT 'absent', -- present, absent, late, excused
    
    -- Check-in data
    checked_in_at TIMESTAMP,                -- Thời điểm check-in
    check_in_method VARCHAR(50),            -- manual, qr_code, face_recognition, auto
    check_in_location VARCHAR(255),         -- GPS location (optional)
    
    -- Additional info
    late_minutes INTEGER DEFAULT 0,         -- Số phút trễ
    notes TEXT,                             -- Ghi chú (VD: lý do vắng)
    
    -- Metadata
    metadata JSONB,                         -- VD: {device: "iPhone", ip: "192.168.1.1"}
    
    recorded_by BIGINT,                     -- Teacher hoặc system
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (class_session_id) REFERENCES class_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(id) ON DELETE CASCADE,
    FOREIGN KEY (recorded_by) REFERENCES users(id) ON DELETE SET NULL,
    
    UNIQUE(class_session_id, student_id) -- 1 student chỉ có 1 attendance record / session
);

-- Constraints
ALTER TABLE attendance
    ADD CONSTRAINT chk_attendance_status
    CHECK (status IN ('present', 'absent', 'late', 'excused'));

ALTER TABLE attendance
    ADD CONSTRAINT chk_attendance_late_minutes
    CHECK (late_minutes >= 0);

-- Index
CREATE INDEX idx_attendance_session_id ON attendance(class_session_id);
CREATE INDEX idx_attendance_student_id ON attendance(student_id);
CREATE INDEX idx_attendance_enrollment_id ON attendance(enrollment_id);
CREATE INDEX idx_attendance_status ON attendance(status);
CREATE INDEX idx_attendance_metadata ON attendance USING GIN(metadata);

-- =====================================================
-- 3. ATTENDANCE_QR_CODES TABLE (Optional)
-- =====================================================
-- Lưu QR code cho mỗi session (nếu dùng QR check-in)
CREATE TABLE attendance_qr_codes (
    id BIGSERIAL PRIMARY KEY,
    class_session_id BIGINT NOT NULL UNIQUE,
    qr_code_token VARCHAR(255) NOT NULL UNIQUE, -- Random token
    qr_code_image_url VARCHAR(500),         -- URL to QR image
    
    -- Validity
    valid_from TIMESTAMP NOT NULL,
    valid_until TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Usage tracking
    scans_count INTEGER DEFAULT 0,
    
    generated_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (class_session_id) REFERENCES class_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (generated_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_attendance_qr_codes_token ON attendance_qr_codes(qr_code_token);
CREATE INDEX idx_attendance_qr_codes_session_id ON attendance_qr_codes(class_session_id);

-- =====================================================
-- 4. CREATE VIEW - Student Attendance Summary
-- =====================================================
-- Tổng hợp attendance của student theo course
CREATE OR REPLACE VIEW v_student_attendance_summary AS
SELECT
    e.student_id,
    e.course_id,
    c.name AS course_name,
    COUNT(DISTINCT cs.id) AS total_sessions,
    COUNT(DISTINCT CASE WHEN a.status = 'present' THEN a.id END) AS present_count,
    COUNT(DISTINCT CASE WHEN a.status = 'late' THEN a.id END) AS late_count,
    COUNT(DISTINCT CASE WHEN a.status = 'absent' THEN a.id END) AS absent_count,
    COUNT(DISTINCT CASE WHEN a.status = 'excused' THEN a.id END) AS excused_count,
    ROUND(
        (COUNT(DISTINCT CASE WHEN a.status IN ('present', 'late') THEN a.id END)::NUMERIC /
         NULLIF(COUNT(DISTINCT cs.id), 0)) * 100,
        2
    ) AS attendance_rate
FROM enrollments e
JOIN courses c ON e.course_id = c.id
LEFT JOIN class_sessions cs ON cs.course_id = c.id AND cs.status = 'completed'
LEFT JOIN attendance a ON a.class_session_id = cs.id AND a.student_id = e.student_id
GROUP BY e.student_id, e.course_id, c.name;

-- =====================================================
-- 5. CREATE VIEW - Teacher Session Schedule
-- =====================================================
-- Lịch dạy của teacher
CREATE OR REPLACE VIEW v_teacher_session_schedule AS
SELECT
    cs.id AS session_id,
    cs.course_id,
    c.name AS course_name,
    cs.title AS session_title,
    cs.scheduled_date,
    cs.start_time,
    cs.end_time,
    cs.session_type,
    cs.session_mode,
    cs.status,
    cs.room_number,
    cs.building,
    t.id AS teacher_id,
    t.employee_code,
    u.username AS teacher_username,
    COUNT(DISTINCT e.id) AS enrolled_students,
    COUNT(DISTINCT a.id) FILTER (WHERE a.status IN ('present', 'late')) AS attended_students
FROM class_sessions cs
JOIN courses c ON cs.course_id = c.id
JOIN teachers t ON cs.teacher_id = t.id
JOIN users u ON t.user_id = u.id
LEFT JOIN enrollments e ON e.course_id = c.id AND e.status = 'active'
LEFT JOIN attendance a ON a.class_session_id = cs.id
GROUP BY 
    cs.id, cs.course_id, c.name, cs.title, cs.scheduled_date,
    cs.start_time, cs.end_time, cs.session_type, cs.session_mode,
    cs.status, cs.room_number, cs.building,
    t.id, t.employee_code, u.username;

-- =====================================================
-- 6. FUNCTION - Auto Create Attendance Records
-- =====================================================
-- Tự động tạo attendance records cho tất cả enrolled students khi session được tạo
CREATE OR REPLACE FUNCTION auto_create_attendance_records()
RETURNS TRIGGER AS $$
BEGIN
    -- Chỉ chạy khi tạo session mới hoặc status chuyển sang 'ongoing'
    IF (TG_OP = 'INSERT') OR 
       (TG_OP = 'UPDATE' AND NEW.status = 'ongoing' AND OLD.status = 'scheduled') THEN
        
        INSERT INTO attendance (class_session_id, student_id, enrollment_id, status)
        SELECT 
            NEW.id,
            e.student_id,
            e.id,
            'absent' -- Default là absent, teacher sẽ mark present
        FROM enrollments e
        WHERE e.course_id = NEW.course_id
          AND e.status = 'active'
        ON CONFLICT (class_session_id, student_id) DO NOTHING; -- Tránh duplicate
        
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger
CREATE TRIGGER trg_auto_create_attendance_records
AFTER INSERT OR UPDATE ON class_sessions
FOR EACH ROW
EXECUTE FUNCTION auto_create_attendance_records();

-- =====================================================
-- 7. FUNCTION - Calculate Session Duration
-- =====================================================
-- Auto tính duration_minutes khi insert/update session
CREATE OR REPLACE FUNCTION calculate_session_duration()
RETURNS TRIGGER AS $$
BEGIN
    NEW.duration_minutes := EXTRACT(EPOCH FROM (NEW.end_time - NEW.start_time)) / 60;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger
CREATE TRIGGER trg_calculate_session_duration
BEFORE INSERT OR UPDATE ON class_sessions
FOR EACH ROW
EXECUTE FUNCTION calculate_session_duration();

-- =====================================================
-- 8. FUNCTION - Auto Calculate Late Minutes
-- =====================================================
-- Tự động tính late_minutes khi check-in
CREATE OR REPLACE FUNCTION calculate_late_minutes()
RETURNS TRIGGER AS $$
DECLARE
    session_start TIMESTAMP;
BEGIN
    -- Chỉ chạy khi status = 'late' và có checked_in_at
    IF NEW.status = 'late' AND NEW.checked_in_at IS NOT NULL THEN
        
        -- Lấy thời gian bắt đầu session
        SELECT 
            cs.scheduled_date + cs.start_time 
        INTO session_start
        FROM class_sessions cs
        WHERE cs.id = NEW.class_session_id;
        
        -- Tính số phút trễ
        NEW.late_minutes := GREATEST(
            0,
            EXTRACT(EPOCH FROM (NEW.checked_in_at - session_start)) / 60
        )::INTEGER;
        
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger
CREATE TRIGGER trg_calculate_late_minutes
BEFORE INSERT OR UPDATE ON attendance
FOR EACH ROW
EXECUTE FUNCTION calculate_late_minutes();

-- =====================================================
-- 9. COMPOSITE INDEXES for Complex Queries
-- =====================================================
-- Index cho query "Lấy tất cả sessions của course X trong tháng Y"
CREATE INDEX idx_class_sessions_course_date ON class_sessions(course_id, scheduled_date DESC);

-- Index cho query "Lấy attendance của student X"
CREATE INDEX idx_attendance_student_date ON attendance(student_id, created_at DESC);

-- Index cho query "Lấy quiz attempts của student theo course"
CREATE INDEX idx_quiz_attempts_student_course ON quiz_attempts(student_id, enrollment_id, submitted_at DESC);

-- Index cho query "Lấy assignments chưa chấm"
CREATE INDEX idx_assignment_submissions_ungraded ON assignment_submissions(status, assignment_id) 
WHERE status = 'submitted';

-- =====================================================
-- 10. MATERIALIZED VIEW - Course Statistics (Advanced)
-- =====================================================
-- Pre-calculate course statistics cho dashboard
CREATE MATERIALIZED VIEW mv_course_statistics AS
SELECT
    c.id AS course_id,
    c.name AS course_name,
    c.status,
    
    -- Enrollment stats
    COUNT(DISTINCT e.id) AS total_enrollments,
    COUNT(DISTINCT e.id) FILTER (WHERE e.status = 'active') AS active_enrollments,
    
    -- Content stats
    COUNT(DISTINCT l.id) AS total_lessons,
    COUNT(DISTINCT a.id) AS total_assignments,
    COUNT(DISTINCT q.id) AS total_quizzes,
    
    -- Session stats
    COUNT(DISTINCT cs.id) AS total_sessions,
    COUNT(DISTINCT cs.id) FILTER (WHERE cs.status = 'completed') AS completed_sessions,
    
    -- Average scores
    ROUND(AVG(asub.grade), 2) AS avg_assignment_score,
    ROUND(AVG(qa.score), 2) AS avg_quiz_score,
    
    -- Engagement
    ROUND(AVG(
        (SELECT completion_percentage 
         FROM v_student_course_progress vcp 
         WHERE vcp.course_id = c.id)
    ), 2) AS avg_completion_rate,
    
    -- Last updated
    NOW() AS last_updated
FROM courses c
LEFT JOIN enrollments e ON e.course_id = c.id
LEFT JOIN lessons l ON l.course_id = c.id AND l.active = TRUE
LEFT JOIN assignments a ON a.course_id = c.id AND a.active = TRUE
LEFT JOIN quizzes q ON q.course_id = c.id AND q.active = TRUE
LEFT JOIN class_sessions cs ON cs.course_id = c.id
LEFT JOIN assignment_submissions asub ON asub.enrollment_id = e.id AND asub.status = 'graded'
LEFT JOIN quiz_attempts qa ON qa.enrollment_id = e.id AND qa.status = 'graded'
GROUP BY c.id, c.name, c.status;

-- Index cho materialized view
CREATE UNIQUE INDEX idx_mv_course_statistics_course_id ON mv_course_statistics(course_id);

-- =====================================================
-- 11. FUNCTION - Refresh Course Statistics
-- =====================================================
-- Schedule này để refresh materialized view (gọi từ scheduled job)
CREATE OR REPLACE FUNCTION refresh_course_statistics()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_course_statistics;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- GIẢI THÍCH DESIGN DECISIONS:
-- =====================================================
-- 1. CLASS_SESSIONS:
--    - Support cả online và offline:
--      + offline: room_number, building
--      + online: meeting_url, password
--      + hybrid: cả 2
--    - actual_start_time/end_time: Ghi nhận thực tế (khác schedule)
--    - duration_minutes: Auto calculate (trigger)
--
-- 2. ATTENDANCE:
--    - check_in_method: Flexibility
--      + manual: Teacher điểm danh tay
--      + qr_code: Student scan QR
--      + face_recognition: AI check-in
--      + auto: Hệ thống tự động (dựa trên online meeting join)
--    - metadata JSONB: Lưu thông tin thêm
--      + Device info (chống fake check-in)
--      + GPS location (verify on-campus)
--      + IP address
--
-- 3. QR CODE CHECK-IN:
--    - qr_code_token: Random UUID
--    - valid_from/until: Time-based (chỉ check-in trong giờ)
--    - Security: Token rotate mỗi session
--    - Workflow:
--      1. Teacher open session → Generate QR
--      2. QR hiển thị trên projector
--      3. Students scan → Auto mark present
--      4. QR expire sau session
--
-- 4. AUTO CREATE ATTENDANCE:
--    - Trigger khi session = 'ongoing'
--    - Tạo sẵn records cho tất cả students (default: absent)
--    - Teacher chỉ cần update status → UI friendly
--
-- 5. LATE_MINUTES CALCULATION:
--    - Auto calculate: checked_in_at - session_start
--    - Use case: Policy "Trễ > 15 phút = absent"
--
-- 6. MATERIALIZED VIEW:
--    - Pre-aggregate data → Dashboard load nhanh
--    - REFRESH CONCURRENTLY: Không block queries
--    - Schedule refresh: Mỗi 1 giờ hoặc end of day
--    - Trade-off: Data không real-time, nhưng fast read
--
-- 7. COMPOSITE INDEXES:
--    - Optimize common query patterns
--    - course_id + scheduled_date: Teacher xem lịch dạy
--    - student_id + created_at: Student xem lịch sử
--
-- 8. PERFORMANCE TIPS:
--    - VACUUM ANALYZE sau khi migration
--    - Monitor slow queries (pg_stat_statements)
--    - Consider partitioning cho bảng lớn (attendance, quiz_attempts)
--      + Partition by DATE (scheduled_date, created_at)
-- =====================================================
