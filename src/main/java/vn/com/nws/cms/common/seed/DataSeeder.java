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
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SemesterRepository semesterRepository;
    private final SubjectRepository subjectRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting Data Seeder...");

        if (userRepository.count("", "") == 0) {
            seedUsers();
        }

        if (semesterRepository.count("", null) == 0) {
            seedSemesters();
        }

        if (subjectRepository.count("", null) == 0) {
            seedSubjects();
        }

        if (courseRepository.count("", null, null, null, null) == 0) {
            seedCourses();
        }

        log.info("Data Seeder completed successfully.");
    }

    private void seedUsers() {
        // Admin
        User admin = User.builder()
                .username("admin")
                .email("admin@nws.com.vn")
                .password(passwordEncoder.encode("admin123"))
                .role("ROLE_ADMIN")
                .build();
        userRepository.save(admin);

        // Teacher
        User teacher = User.builder()
                .username("teacher")
                .email("teacher@nws.com.vn")
                .password(passwordEncoder.encode("teacher123"))
                .role("ROLE_TEACHER")
                .build();
        userRepository.save(teacher);

        // Student
        User student = User.builder()
                .username("student")
                .email("student@nws.com.vn")
                .password(passwordEncoder.encode("student123"))
                .role("ROLE_STUDENT")
                .build();
        userRepository.save(student);

        log.info("Seeded Users.");
    }

    private void seedSemesters() {
        Semester hk1_2024 = Semester.builder()
                .name("Học kỳ 1 2024-2025")
                .code("HK1_2425")
                .startDate(LocalDate.of(2024, 9, 1))
                .endDate(LocalDate.of(2025, 1, 15))
                .active(true)
                .build();
        semesterRepository.save(hk1_2024);

        Semester hk2_2024 = Semester.builder()
                .name("Học kỳ 2 2024-2025")
                .code("HK2_2425")
                .startDate(LocalDate.of(2025, 1, 20))
                .endDate(LocalDate.of(2025, 6, 15))
                .active(false)
                .build();
        semesterRepository.save(hk2_2024);

        log.info("Seeded Semesters.");
    }

    private void seedSubjects() {
        Subject java = Subject.builder()
                .name("Lập trình Java căn bản")
                .code("JAVA001")
                .credit(3)
                .description("Môn học cung cấp kiến thức nền tảng về ngôn ngữ lập trình Java.")
                .active(true)
                .build();
        subjectRepository.save(java);

        Subject web = Subject.builder()
                .name("Lập trình Web với Spring Boot")
                .code("WEB002")
                .credit(4)
                .description("Xây dựng ứng dụng web hiện đại sử dụng Spring Boot framework.")
                .active(true)
                .build();
        subjectRepository.save(web);

        Subject db = Subject.builder()
                .name("Cơ sở dữ liệu")
                .code("DB003")
                .credit(3)
                .description("Kiến thức về thiết kế và quản trị cơ sở dữ liệu quan hệ.")
                .active(true)
                .build();
        subjectRepository.save(db);

        log.info("Seeded Subjects.");
    }

    private void seedCourses() {
        Semester hk1 = semesterRepository.findByCode("HK1_2425").orElseThrow();
        Subject java = subjectRepository.findByCode("JAVA001").orElseThrow();
        User teacher = userRepository.findByUsername("teacher").orElseThrow();

        Course courseJava = Course.builder()
                .name("Lớp Java 01 - HK1")
                .code("JAVA001_HK1_01")
                .maxStudents(30)
                .currentStudents(0)
                .active(true)
                .semester(hk1)
                .subject(java)
                .teacher(teacher)
                .build();
        courseRepository.save(courseJava);

        log.info("Seeded Courses.");
    }
}
