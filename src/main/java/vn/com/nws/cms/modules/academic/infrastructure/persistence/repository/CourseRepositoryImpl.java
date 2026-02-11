package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.domain.model.Course;
import vn.com.nws.cms.modules.academic.domain.repository.CourseRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.CourseEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper.CourseMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CourseRepositoryImpl implements CourseRepository {

    private final JpaCourseRepository jpaCourseRepository;
    private final CourseMapper courseMapper;

    @Override
    public Course save(Course course) {
        CourseEntity entity = courseMapper.toEntity(course);
        if (course.getId() != null) {
            entity.setId(course.getId());
        }
        CourseEntity savedEntity = jpaCourseRepository.save(entity);
        return courseMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Course> findById(Long id) {
        return jpaCourseRepository.findById(id).map(courseMapper::toDomain);
    }

    @Override
    public Optional<Course> findByCode(String code) {
        return jpaCourseRepository.findByCode(code).map(courseMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaCourseRepository.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaCourseRepository.existsByCode(code);
    }

    @Override
    public List<Course> search(String keyword, Long semesterId, Long subjectId, Long teacherId, Boolean active, int page, int size) {
        return jpaCourseRepository.search(keyword, semesterId, subjectId, teacherId, active, PageRequest.of(page - 1, size))
                .getContent().stream()
                .map(courseMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count(String keyword, Long semesterId, Long subjectId, Long teacherId, Boolean active) {
        return jpaCourseRepository.count(keyword, semesterId, subjectId, teacherId, active);
    }
}
