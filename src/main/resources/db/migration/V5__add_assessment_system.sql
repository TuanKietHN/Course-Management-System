-- =====================================================
-- Migration V5: Assessment & Evaluation System
-- =====================================================
-- Mục đích: Xây dựng hệ thống đánh giá (Assignments, Quizzes)
--           và theo dõi tiến độ tổng thể của student
-- =====================================================

-- =====================================================
-- 1. ASSIGNMENTS TABLE
-- =====================================================
-- Lưu thông tin bài tập (1 lesson/course có nhiều assignments)
-- Thiết kế: Support file upload, rubric grading, peer review
CREATE TABLE assignments (
    id BIGSERIAL PRIMARY KEY,
    lesson_id BIGINT,                       -- Link to lesson (optional)
    course_id BIGINT NOT NULL,              -- Link to course (required)
    title VARCHAR(255) NOT NULL,            -- Tiêu đề: "Assignment 1: Build a TODO app"
    description TEXT,                       -- Yêu cầu chi tiết (HTML)
    instruction_file_url VARCHAR(500),      -- File hướng dẫn (PDF, DOCX)
    
    -- Grading
    max_points INTEGER NOT NULL DEFAULT 100, -- Điểm tối đa
    passing_points INTEGER,                 -- Điểm đạt (để pass assignment)
    grading_rubric JSONB,                   -- Rubric (tiêu chí chấm điểm)
    
    -- Submission settings
    submission_type VARCHAR(50) NOT NULL,   -- file, text, url, quiz
    max_file_size_mb INTEGER DEFAULT 10,    -- File upload size limit
    allowed_file_types VARCHAR(255),        -- VD: "pdf,docx,zip"
    max_attempts INTEGER DEFAULT 1,         -- Số lần nộp tối đa (0 = unlimited)
    
    -- Deadlines
    available_from TIMESTAMP,               -- Mở từ lúc nào
    due_date TIMESTAMP,                     -- Deadline
    late_submission_allowed BOOLEAN DEFAULT TRUE,
    late_penalty_percentage INTEGER DEFAULT 0, -- Phạt trễ (% điểm)
    
    -- Settings
    is_group_assignment BOOLEAN DEFAULT FALSE,
    max_group_size INTEGER,
    enable_peer_review BOOLEAN DEFAULT FALSE,
    
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT,                      -- Teacher tạo
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE SET NULL,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Constraints
ALTER TABLE assignments
    ADD CONSTRAINT chk_assignments_submission_type
    CHECK (submission_type IN ('file', 'text', 'url', 'quiz', 'code'));

ALTER TABLE assignments
    ADD CONSTRAINT chk_assignments_max_points
    CHECK (max_points > 0);

-- Index
CREATE INDEX idx_assignments_lesson_id ON assignments(lesson_id);
CREATE INDEX idx_assignments_course_id ON assignments(course_id);
CREATE INDEX idx_assignments_due_date ON assignments(due_date);
CREATE INDEX idx_assignments_created_by ON assignments(created_by);
CREATE INDEX idx_assignments_grading_rubric ON assignments USING GIN(grading_rubric);

-- =====================================================
-- 2. ASSIGNMENT_SUBMISSIONS TABLE
-- =====================================================
-- Lưu bài nộp của student
-- Thiết kế: Support multiple attempts, versioning
CREATE TABLE assignment_submissions (
    id BIGSERIAL PRIMARY KEY,
    assignment_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    enrollment_id BIGINT NOT NULL,
    
    -- Submission data
    attempt_number INTEGER NOT NULL DEFAULT 1, -- Lần nộp thứ mấy
    submission_text TEXT,                   -- Text answer (nếu type=text)
    submission_url VARCHAR(500),            -- URL (nếu type=url)
    
    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'draft', -- draft, submitted, graded, returned
    submitted_at TIMESTAMP,                 -- Thời điểm nộp
    is_late BOOLEAN DEFAULT FALSE,          -- Nộp trễ?
    
    -- Grading
    grade NUMERIC(5,2),                     -- Điểm (VD: 85.50)
    feedback TEXT,                          -- Nhận xét của teacher
    graded_by BIGINT,                       -- Teacher chấm
    graded_at TIMESTAMP,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(id) ON DELETE CASCADE,
    FOREIGN KEY (graded_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Constraints
ALTER TABLE assignment_submissions
    ADD CONSTRAINT chk_assignment_submissions_status
    CHECK (status IN ('draft', 'submitted', 'graded', 'returned', 'resubmit_requested'));

ALTER TABLE assignment_submissions
    ADD CONSTRAINT chk_assignment_submissions_grade
    CHECK (grade IS NULL OR (grade >= 0 AND grade <= 100));

-- Index
CREATE INDEX idx_assignment_submissions_assignment_id ON assignment_submissions(assignment_id);
CREATE INDEX idx_assignment_submissions_student_id ON assignment_submissions(student_id);
CREATE INDEX idx_assignment_submissions_enrollment_id ON assignment_submissions(enrollment_id);
CREATE INDEX idx_assignment_submissions_status ON assignment_submissions(status);
CREATE INDEX idx_assignment_submissions_graded_by ON assignment_submissions(graded_by);

-- =====================================================
-- 3. ASSIGNMENT_FILES TABLE
-- =====================================================
-- Lưu files đính kèm submission
-- Thiết kế: Separate table cho file management
CREATE TABLE assignment_submission_files (
    id BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_type VARCHAR(50),
    file_size BIGINT,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (submission_id) REFERENCES assignment_submissions(id) ON DELETE CASCADE
);

CREATE INDEX idx_assignment_submission_files_submission_id ON assignment_submission_files(submission_id);

-- =====================================================
-- 4. QUIZZES TABLE
-- =====================================================
-- Lưu thông tin bài kiểm tra/quiz
-- Thiết kế: Support multiple question types, time limit, randomization
CREATE TABLE quizzes (
    id BIGSERIAL PRIMARY KEY,
    lesson_id BIGINT,                       -- Link to lesson (optional)
    course_id BIGINT NOT NULL,              -- Link to course (required)
    title VARCHAR(255) NOT NULL,
    description TEXT,
    
    -- Quiz settings
    total_points INTEGER NOT NULL DEFAULT 100,
    passing_score INTEGER NOT NULL DEFAULT 70, -- % để pass
    time_limit_minutes INTEGER,             -- Giới hạn thời gian (0 = unlimited)
    max_attempts INTEGER DEFAULT 1,         -- Số lần làm tối đa (0 = unlimited)
    
    -- Question settings
    shuffle_questions BOOLEAN DEFAULT FALSE, -- Xáo trộn câu hỏi
    shuffle_answers BOOLEAN DEFAULT FALSE,   -- Xáo trộn đáp án
    show_correct_answers BOOLEAN DEFAULT TRUE, -- Hiện đáp án đúng sau khi làm
    show_correct_answers_at TIMESTAMP,      -- Hoặc hiện sau thời điểm này
    
    -- Availability
    available_from TIMESTAMP,
    available_until TIMESTAMP,
    
    -- Grading
    auto_grade BOOLEAN DEFAULT TRUE,        -- Tự động chấm điểm
    
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE SET NULL,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Index
CREATE INDEX idx_quizzes_lesson_id ON quizzes(lesson_id);
CREATE INDEX idx_quizzes_course_id ON quizzes(course_id);
CREATE INDEX idx_quizzes_created_by ON quizzes(created_by);

-- =====================================================
-- 5. QUIZ_QUESTIONS TABLE
-- =====================================================
-- Lưu câu hỏi của quiz
-- Thiết kế: Support multiple question types
CREATE TABLE quiz_questions (
    id BIGSERIAL PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    question_type VARCHAR(50) NOT NULL,     -- multiple_choice, true_false, short_answer, essay
    question_text TEXT NOT NULL,            -- Nội dung câu hỏi
    question_image_url VARCHAR(500),        -- Hình ảnh câu hỏi (optional)
    points INTEGER NOT NULL DEFAULT 1,      -- Điểm cho câu này
    order_index INTEGER NOT NULL,           -- Thứ tự câu hỏi
    explanation TEXT,                       -- Giải thích đáp án (hiện sau khi làm)
    
    -- For auto-grading
    correct_answer TEXT,                    -- Đáp án đúng (cho true_false, short_answer)
    correct_answer_ids BIGINT[],            -- IDs of correct answers (cho multiple_choice)
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
);

-- Constraints
ALTER TABLE quiz_questions
    ADD CONSTRAINT chk_quiz_questions_type
    CHECK (question_type IN ('multiple_choice', 'multiple_select', 'true_false', 'short_answer', 'essay', 'matching'));

-- Index
CREATE INDEX idx_quiz_questions_quiz_id ON quiz_questions(quiz_id);
CREATE INDEX idx_quiz_questions_order_index ON quiz_questions(quiz_id, order_index);

-- =====================================================
-- 6. QUIZ_ANSWERS TABLE
-- =====================================================
-- Lưu các đáp án cho câu hỏi multiple choice
CREATE TABLE quiz_answers (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL,
    answer_text TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    order_index INTEGER NOT NULL,
    explanation TEXT,                       -- Giải thích tại sao đúng/sai
    
    FOREIGN KEY (question_id) REFERENCES quiz_questions(id) ON DELETE CASCADE
);

CREATE INDEX idx_quiz_answers_question_id ON quiz_answers(question_id);

-- =====================================================
-- 7. QUIZ_ATTEMPTS TABLE
-- =====================================================
-- Lưu lượt làm quiz của student
CREATE TABLE quiz_attempts (
    id BIGSERIAL PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    enrollment_id BIGINT NOT NULL,
    
    attempt_number INTEGER NOT NULL DEFAULT 1,
    
    -- Attempt data
    started_at TIMESTAMP NOT NULL,
    submitted_at TIMESTAMP,
    time_spent_seconds INTEGER,             -- Thời gian làm (seconds)
    
    -- Scoring
    score NUMERIC(5,2),                     -- Điểm (0-100)
    points_earned NUMERIC(5,2),             -- Số điểm đạt được
    total_points NUMERIC(5,2),              -- Tổng điểm
    passed BOOLEAN,                         -- Đạt/Không đạt
    
    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'in_progress', -- in_progress, submitted, graded
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(id) ON DELETE CASCADE
);

-- Constraints
ALTER TABLE quiz_attempts
    ADD CONSTRAINT chk_quiz_attempts_status
    CHECK (status IN ('in_progress', 'submitted', 'graded'));

-- Index
CREATE INDEX idx_quiz_attempts_quiz_id ON quiz_attempts(quiz_id);
CREATE INDEX idx_quiz_attempts_student_id ON quiz_attempts(student_id);
CREATE INDEX idx_quiz_attempts_enrollment_id ON quiz_attempts(enrollment_id);

-- =====================================================
-- 8. QUIZ_ATTEMPT_ANSWERS TABLE
-- =====================================================
-- Lưu câu trả lời của student cho từng câu hỏi
CREATE TABLE quiz_attempt_answers (
    id BIGSERIAL PRIMARY KEY,
    attempt_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    
    -- Answer data
    selected_answer_ids BIGINT[],           -- Đáp án chọn (cho multiple choice)
    text_answer TEXT,                       -- Câu trả lời text (cho short_answer, essay)
    
    -- Grading
    is_correct BOOLEAN,                     -- Đúng/Sai (auto or manual)
    points_earned NUMERIC(5,2),             -- Điểm đạt được
    feedback TEXT,                          -- Nhận xét (cho essay)
    
    answered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES quiz_questions(id) ON DELETE CASCADE,
    
    UNIQUE(attempt_id, question_id) -- 1 attempt chỉ có 1 answer cho 1 question
);

CREATE INDEX idx_quiz_attempt_answers_attempt_id ON quiz_attempt_answers(attempt_id);
CREATE INDEX idx_quiz_attempt_answers_question_id ON quiz_attempt_answers(question_id);

-- =====================================================
-- 9. CREATE VIEW - Student Quiz Performance
-- =====================================================
CREATE OR REPLACE VIEW v_student_quiz_performance AS
SELECT
    qa.student_id,
    qa.quiz_id,
    q.title AS quiz_title,
    q.course_id,
    COUNT(qa.id) AS total_attempts,
    MAX(qa.score) AS best_score,
    AVG(qa.score) AS average_score,
    MAX(qa.submitted_at) AS last_attempt_date,
    BOOL_OR(qa.passed) AS ever_passed
FROM quiz_attempts qa
JOIN quizzes q ON qa.quiz_id = q.id
WHERE qa.status = 'graded'
GROUP BY qa.student_id, qa.quiz_id, q.title, q.course_id;

-- =====================================================
-- 10. FUNCTION - Auto Grade Quiz
-- =====================================================
-- Function tự động chấm điểm quiz khi submit
CREATE OR REPLACE FUNCTION auto_grade_quiz_attempt()
RETURNS TRIGGER AS $$
DECLARE
    total_points NUMERIC := 0;
    earned_points NUMERIC := 0;
    passing_score INTEGER;
BEGIN
    -- Chỉ chạy khi status chuyển sang 'submitted'
    IF NEW.status = 'submitted' AND OLD.status = 'in_progress' THEN
        
        -- Tính tổng điểm
        SELECT SUM(qq.points) INTO total_points
        FROM quiz_questions qq
        WHERE qq.quiz_id = NEW.quiz_id;
        
        -- Tính điểm đạt được
        SELECT SUM(qaa.points_earned) INTO earned_points
        FROM quiz_attempt_answers qaa
        WHERE qaa.attempt_id = NEW.id;
        
        -- Lấy passing score
        SELECT q.passing_score INTO passing_score
        FROM quizzes q
        WHERE q.id = NEW.quiz_id;
        
        -- Update attempt
        NEW.total_points := total_points;
        NEW.points_earned := COALESCE(earned_points, 0);
        NEW.score := ROUND((COALESCE(earned_points, 0) / NULLIF(total_points, 0)) * 100, 2);
        NEW.passed := (NEW.score >= passing_score);
        NEW.status := 'graded';
        
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger
CREATE TRIGGER trg_auto_grade_quiz_attempt
BEFORE UPDATE ON quiz_attempts
FOR EACH ROW
EXECUTE FUNCTION auto_grade_quiz_attempt();

-- =====================================================
-- GIẢI THÍCH DESIGN DECISIONS:
-- =====================================================
-- 1. ASSIGNMENTS:
--    - submission_type: Linh hoạt (file, text, url, code)
--      + file: Upload PDF, ZIP (common)
--      + text: Essay, report (WYSIWYG editor)
--      + url: Link to GitHub, Google Docs
--      + code: Code submission với syntax highlighting
--    - grading_rubric (JSONB): Structured rubric
--      + VD: {
--          "criteria": [
--            {"name": "Code quality", "points": 30},
--            {"name": "Documentation", "points": 20}
--          ]
--        }
--    - max_attempts: Cho phép nộp lại (học sinh được cải thiện)
--    - late_penalty: Auto tính điểm (VD: trễ 1 ngày -10%)
--
-- 2. ASSIGNMENT_SUBMISSIONS:
--    - attempt_number: Track multiple submissions
--    - is_late: Auto calculate dựa trên due_date
--    - status workflow:
--      draft → submitted → graded → returned
--                      ↓ (optional)
--              resubmit_requested → submitted → ...
--
-- 3. QUIZZES:
--    - shuffle_questions/answers: Chống gian lận
--    - time_limit: Tăng độ khó, giống thi thật
--    - show_correct_answers_at: Chiến lược giảng dạy
--      + Hiện ngay: Student học từ sai lầm
--      + Hiện sau deadline: Tránh chia sẻ đáp án
--    - auto_grade: Most questions auto-graded, except essay
--
-- 4. QUIZ_QUESTIONS:
--    - question_type variations:
--      + multiple_choice: Chọn 1 đáp án đúng
--      + multiple_select: Chọn nhiều đáp án đúng
--      + true_false: Đúng/Sai
--      + short_answer: Text ngắn (auto-grade với keyword matching)
--      + essay: Text dài (manual grade)
--      + matching: Nối câu (advanced)
--    - correct_answer_ids: Array cho flexibility
--      + Multiple select: [1,3,5]
--      + Single choice: [2]
--
-- 5. QUIZ_ATTEMPTS:
--    - Track all attempts: Analytics (student improve over time?)
--    - time_spent_seconds: Detect cheating (quá nhanh?)
--    - Best attempt: Lấy điểm cao nhất (incentivize learning)
--
-- 6. AUTO_GRADE FUNCTION:
--    - Trigger on status change: in_progress → submitted
--    - Calculate score: (earned_points / total_points) * 100
--    - Determine pass/fail: score >= passing_score
--    - For essay questions: Teacher manually grade sau
--
-- 7. PERFORMANCE CONSIDERATIONS:
--    - GIN Index on JSONB: Fast queries on rubric/metadata
--    - Separate tables for files: Easier to manage, backup
--    - Views for analytics: Pre-aggregated data
-- =====================================================
