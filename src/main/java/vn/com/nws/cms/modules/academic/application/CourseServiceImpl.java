package vn.com.nws.cms.modules.academic.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.modules.academic.api.dto.*;
import vn.com.nws.cms.modules.academic.domain.model.Course;
import vn.com.nws.cms.modules.academic.domain.model.Semester;
import vn.com.nws.cms.modules.academic.domain.model.Subject;
import vn.com.nws.cms.modules.academic.domain.repository.CourseRepository;
import vn.com.nws.cms.modules.academic.domain.repository.SemesterRepository;
import vn.com.nws.cms.modules.academic.domain.repository.SubjectRepository;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;
import vn.com.nws.cms.modules.user.api.dto.UserResponse;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;
    private final SemesterRepository semesterRepository;
    private final UserRepository userRepository;

    @Override
    public PageResponse<CourseResponse> getCourses(CourseFilterRequest request) {
        List<Course> courses = courseRepository.search(
                request.getKeyword(), 
                request.getSemesterId(), 
                request.getSubjectId(), 
                request.getTeacherId(), 
                request.getActive(), 
                request.getPage(), 
                request.getSize());
        
        long totalElements = courseRepository.count(
                request.getKeyword(), 
                request.getSemesterId(), 
                request.getSubjectId(), 
                request.getTeacherId(), 
                request.getActive());
        
        int totalPages = (int) Math.ceil((double) totalElements / request.getSize());

        List<CourseResponse> responses = courses.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<CourseResponse>builder()
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .data(responses)
                .build();
    }

    @Override
    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Course not found"));
        return toResponse(course);
    }

    @Override
    @Transactional
    public CourseResponse createCourse(CourseCreateRequest request) {
        if (courseRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Course code already exists");
        }

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new BusinessException("Subject not found"));

        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new BusinessException("Semester not found"));

        User teacher = null;
        if (request.getTeacherId() != null) {
            teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new BusinessException("Teacher not found"));
            // Can add role check here if needed
        }

        Course course = Course.builder()
                .name(request.getName())
                .code(request.getCode())
                .maxStudents(request.getMaxStudents())
                .currentStudents(0)
                .active(request.isActive())
                .subject(subject)
                .semester(semester)
                .teacher(teacher)
                .build();

        course = courseRepository.save(course);
        return toResponse(course);
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(Long id, CourseUpdateRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Course not found"));

        if (request.getCode() != null && !request.getCode().equals(course.getCode())) {
            if (courseRepository.existsByCode(request.getCode())) {
                throw new BusinessException("Course code already exists");
            }
            course.setCode(request.getCode());
        }

        if (request.getName() != null) course.setName(request.getName());
        if (request.getMaxStudents() != null) course.setMaxStudents(request.getMaxStudents());
        if (request.getActive() != null) course.setActive(request.getActive());

        if (request.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new BusinessException("Subject not found"));
            course.setSubject(subject);
        }

        if (request.getSemesterId() != null) {
            Semester semester = semesterRepository.findById(request.getSemesterId())
                    .orElseThrow(() -> new BusinessException("Semester not found"));
            course.setSemester(semester);
        }

        if (request.getTeacherId() != null) {
            User teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new BusinessException("Teacher not found"));
            course.setTeacher(teacher);
        }

        course = courseRepository.save(course);
        return toResponse(course);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.findById(id).isPresent()) {
            throw new BusinessException("Course not found");
        }
        courseRepository.deleteById(id);
    }

    private CourseResponse toResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .name(course.getName())
                .code(course.getCode())
                .maxStudents(course.getMaxStudents())
                .currentStudents(course.getCurrentStudents())
                .active(course.isActive())
                .subject(SubjectResponse.builder()
                        .id(course.getSubject().getId())
                        .name(course.getSubject().getName())
                        .code(course.getSubject().getCode())
                        .credit(course.getSubject().getCredits())
                        .build())
                .semester(SemesterResponse.builder()
                        .id(course.getSemester().getId())
                        .name(course.getSemester().getName())
                        .code(course.getSemester().getCode())
                        .build())
                .teacher(course.getTeacher() != null ? UserResponse.builder()
                        .id(course.getTeacher().getId())
                        .username(course.getTeacher().getUsername())
                        .email(course.getTeacher().getEmail())
                        .role(course.getTeacher().getRoles().stream().map(Enum::name).collect(Collectors.joining(",")))
                        .build() : null)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}
