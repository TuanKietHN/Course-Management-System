package vn.com.nws.cms.common.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.modules.academic.domain.model.Course;
import vn.com.nws.cms.modules.academic.domain.model.Semester;
import vn.com.nws.cms.modules.academic.domain.model.Subject;
import vn.com.nws.cms.modules.academic.domain.repository.CourseRepository;
import vn.com.nws.cms.modules.academic.domain.repository.SemesterRepository;
import vn.com.nws.cms.modules.academic.domain.repository.SubjectRepository;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SemesterRepository semesterRepository;
    private final SubjectRepository subjectRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("=== Starting Data Seeder ===");

        seedUsers();
        seedSemesters();
        seedSubjects();
        seedCourses();

        log.info("=== Data Seeder completed successfully ===");
    }

    /* =========================
       USERS
       ========================= */

    private void seedUsers() {
        seedUserIfNotExists(
                "admin",
                "admin@nws.com.vn",
                "admin123",
                "ROLE_ADMIN"
        );

        seedUserIfNotExists(
                "teacher",
                "teacher@nws.com.vn",
                "teacher123",
                "ROLE_TEACHER"
        );

        seedUserIfNotExists(
                "student",
                "student@nws.com.vn",
                "student123",
                "ROLE_STUDENT"
        );

        log.info("Users seeding done.");
    }

    private void seedUserIfNotExists(
            String username,
            String email,
            String rawPassword,
            String role
    ) {
        if (userRepository.existsByEmail(email)) {
            log.info("User [{}] already exists, skipping", email);
            return;
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .build();

        userRepository.save(user);
        log.info("Seeded user [{}]", email);
    }

    /* =========================
       SEMESTERS
       ========================= */

    private void seedSemesters() {
        seedSemesterIfNotExists(
                "HK1_2425",
                "Học kỳ 1 2024-2025",
                LocalDate.of(2024, 9, 1),
                LocalDate.of(2025, 1, 15),
                true
        );

        seedSemesterIfNotExists(
                "HK2_2425",
                "Học kỳ 2 2024-2025",
                LocalDate.of(2025, 1, 20),
                LocalDate.of(2025, 6, 15),
                false
        );

        log.info("Semesters seeding done.");
    }

    private void seedSemesterIfNotExists(
            String code,
            String name,
            LocalDate startDate,
            LocalDate endDate,
            boolean active
    ) {
        if (semesterRepository.findByCode(code).isPresent()) {
            log.info("Semester [{}] already exists, skipping", code);
            return;
        }

        Semester semester = Semester.builder()
                .code(code)
                .name(name)
                .startDate(startDate)
                .endDate(endDate)
                .active(active)
                .build();

        semesterRepository.save(semester);
        log.info("Seeded semester [{}]", code);
    }

    /* =========================
       SUBJECTS
       ========================= */

    private void seedSubjects() {
        seedSubjectIfNotExists(
                "JAVA001",
                "Lập trình Java căn bản",
                3,
                "Môn học cung cấp kiến thức nền tảng về ngôn ngữ lập trình Java."
        );

        seedSubjectIfNotExists(
                "WEB002",
                "Lập trình Web với Spring Boot",
                4,
                "Xây dựng ứng dụng web hiện đại sử dụng Spring Boot framework."
        );

        seedSubjectIfNotExists(
                "DB003",
                "Cơ sở dữ liệu",
                3,
                "Kiến thức về thiết kế và quản trị cơ sở dữ liệu quan hệ."
        );

        log.info("Subjects seeding done.");
    }

    private void seedSubjectIfNotExists(
            String code,
            String name,
            int credit,
            String description
    ) {
        if (subjectRepository.findByCode(code).isPresent()) {
            log.info("Subject [{}] already exists, skipping", code);
            return;
        }

        Subject subject = Subject.builder()
                .code(code)
                .name(name)
                .credit(credit)
                .description(description)
                .active(true)
                .build();

        subjectRepository.save(subject);
        log.info("Seeded subject [{}]", code);
    }

    /* =========================
       COURSES
       ========================= */

    private void seedCourses() {
        if (courseRepository.findByCode("JAVA001_HK1_01").isPresent()) {
            log.info("Course [JAVA001_HK1_01] already exists, skipping");
            return;
        }

        Semester semester = semesterRepository.findByCode("HK1_2425")
                .orElseThrow(() -> new IllegalStateException("Semester HK1_2425 not found"));

        Subject subject = subjectRepository.findByCode("JAVA001")
                .orElseThrow(() -> new IllegalStateException("Subject JAVA001 not found"));

        User teacher = userRepository.findByUsername("teacher")
                .orElseThrow(() -> new IllegalStateException("Teacher user not found"));

        Course course = Course.builder()
                .name("Lớp Java 01 - HK1")
                .code("JAVA001_HK1_01")
                .maxStudents(30)
                .currentStudents(0)
                .active(true)
                .semester(semester)
                .subject(subject)
                .teacher(teacher)
                .build();

        courseRepository.save(course);
        log.info("Seeded course [JAVA001_HK1_01]");
    }
}
