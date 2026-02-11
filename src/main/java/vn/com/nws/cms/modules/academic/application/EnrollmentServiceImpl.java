package vn.com.nws.cms.modules.academic.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.modules.academic.api.dto.*;
import vn.com.nws.cms.modules.academic.domain.model.Course;
import vn.com.nws.cms.modules.academic.domain.model.Enrollment;
import vn.com.nws.cms.modules.academic.domain.repository.CourseRepository;
import vn.com.nws.cms.modules.academic.domain.repository.EnrollmentRepository;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;
import vn.com.nws.cms.modules.iam.api.dto.UserResponse;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public EnrollmentResponse enrollStudent(EnrollmentCreateRequest request) {
        if (enrollmentRepository.existsByCourseIdAndStudentId(request.getCourseId(), request.getStudentId())) {
            throw new BusinessException("Student already enrolled in this course");
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new BusinessException("Course not found"));

        if (!course.isActive()) {
            throw new BusinessException("Course is not active");
        }

        if (course.getCurrentStudents() >= course.getMaxStudents()) {
            throw new BusinessException("Course is full");
        }

        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new BusinessException("Student not found"));

        // Increase current students count
        course.setCurrentStudents(course.getCurrentStudents() + 1);
        courseRepository.save(course);

        Enrollment enrollment = Enrollment.builder()
                .course(course)
                .student(student)
                .status("ENROLLED")
                .build();

        enrollment = enrollmentRepository.save(enrollment);
        return toResponse(enrollment);
    }

    @Override
    @Transactional
    public EnrollmentResponse updateEnrollment(Long id, EnrollmentUpdateRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Enrollment not found"));

        if (request.getStatus() != null) {
            // If dropping, decrease count
            if ("DROPPED".equals(request.getStatus()) && !"DROPPED".equals(enrollment.getStatus())) {
                Course course = enrollment.getCourse();
                course.setCurrentStudents(course.getCurrentStudents() - 1);
                courseRepository.save(course);
            }
            enrollment.setStatus(request.getStatus());
        }
        
        if (request.getGrade() != null) {
            enrollment.setGrade(request.getGrade());
        }

        enrollment = enrollmentRepository.save(enrollment);
        return toResponse(enrollment);
    }

    @Override
    @Transactional
    public void deleteEnrollment(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Enrollment not found"));

        // Decrease count
        Course course = enrollment.getCourse();
        if (course.getCurrentStudents() > 0) {
            course.setCurrentStudents(course.getCurrentStudents() - 1);
            courseRepository.save(course);
        }

        enrollmentRepository.deleteById(id);
    }

    @Override
    public List<EnrollmentResponse> getStudentEnrollments(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentResponse> getCourseEnrollments(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private EnrollmentResponse toResponse(Enrollment enrollment) {
        // Simplified mapping for brevity
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .status(enrollment.getStatus())
                .grade(enrollment.getGrade())
                .createdAt(enrollment.getCreatedAt())
                .updatedAt(enrollment.getUpdatedAt())
                .course(CourseResponse.builder()
                        .id(enrollment.getCourse().getId())
                        .name(enrollment.getCourse().getName())
                        .code(enrollment.getCourse().getCode())
                        .build())
                .student(UserResponse.builder()
                        .id(enrollment.getStudent().getId())
                        .username(enrollment.getStudent().getUsername())
                        .email(enrollment.getStudent().getEmail())
                        .build())
                .build();
    }
}
