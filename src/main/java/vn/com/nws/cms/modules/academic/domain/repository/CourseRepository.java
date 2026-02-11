package vn.com.nws.cms.modules.academic.domain.repository;

import vn.com.nws.cms.modules.academic.domain.model.Course;

import java.util.List;
import java.util.Optional;

public interface CourseRepository {
    Course save(Course course);
    Optional<Course> findById(Long id);
    Optional<Course> findByCode(String code);
    void deleteById(Long id);
    boolean existsByCode(String code);
    
    List<Course> search(String keyword, Long semesterId, Long subjectId, Long teacherId, Boolean active, int page, int size);
    long count(String keyword, Long semesterId, Long subjectId, Long teacherId, Boolean active);
}
