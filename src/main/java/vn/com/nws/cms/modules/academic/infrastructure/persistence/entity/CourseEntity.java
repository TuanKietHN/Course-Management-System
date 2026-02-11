package vn.com.nws.cms.modules.academic.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.common.audit.AuditEntity;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.UserEntity;

import java.time.LocalDate;

@Entity
@Table(name = "courses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseEntity extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description; // V3

    @Column(name = "thumbnail_url")
    private String thumbnailUrl; // V3

    @Column(nullable = false)
    private String status; // V3: draft, published, archived

    @Column(nullable = false)
    private Integer credits; // V3

    @Column(nullable = false)
    private Integer maxStudents;

    @Column(nullable = false)
    private Integer currentStudents;

    @Column(name = "duration_weeks")
    private Integer durationWeeks; // V3

    private String level; // V3

    private String language; // V3

    @Column(name = "enrollment_start_date")
    private LocalDate enrollmentStartDate; // V3

    @Column(name = "enrollment_end_date")
    private LocalDate enrollmentEndDate; // V3

    @Column(nullable = false)
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private SubjectEntity subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private SemesterEntity semester;

    // NOTE: In V3 migration, courses table has teacher_id (from V1) AND department_id.
    // However, V3 also introduces a 'teachers' table.
    // The relationship 'teacher_id' in courses table refers to 'users' table in V1, but logically should refer to 'teachers' table if we want rich teacher profile.
    // Let's check V1 migration content again or V3.
    // V3 says: "LEFT JOIN teachers t ON c.teacher_id = t.id" in the view.
    // This implies teacher_id in courses refers to TEACHERS table, NOT USERS table.
    // BUT V1 said: FOREIGN KEY (teacher_id) REFERENCES users(id).
    // So there is a conflict or change in FK reference.
    // Since V3 doesn't explicitly drop/add FK for teacher_id in courses, it remains pointing to USERS.
    // However, the View joins on teachers t.id.
    // IF teachers.id == users.id (which is not guaranteed as teachers.id is BIGSERIAL), then it's messy.
    // Teachers table has user_id FK.
    // Let's assume for now teacher_id refers to Users (as per V1) for compatibility, 
    // OR we should have migrated it to refer to Teachers. 
    // Given the View "LEFT JOIN teachers t ON c.teacher_id = t.id", it strongly suggests c.teacher_id should be Teacher ID.
    // BUT checking V3 again: It does NOT alter courses.teacher_id constraint.
    // So it still points to USERS.
    // The View might be wrong or assuming teachers.id is synced/same? No, teachers has its own ID.
    // Actually, looking at the View: `LEFT JOIN teachers t ON c.teacher_id = t.id`.
    // If c.teacher_id is a User ID (from V1), then joining with t.id is WRONG unless t.id matches User ID.
    // A safer bet for now is to keep it as UserEntity, but ideally it should be TeacherEntity.
    // Let's keep UserEntity for safety as per V1 constraint.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private UserEntity teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private DepartmentEntity department; // V3
}
