package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.CourseEntity;

import java.util.Optional;

@Repository
public interface JpaCourseRepository extends JpaRepository<CourseEntity, Long> {
    Optional<CourseEntity> findByCode(String code);
    boolean existsByCode(String code);

    @Query("SELECT c FROM CourseEntity c WHERE " +
           "(:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:semesterId IS NULL OR c.semester.id = :semesterId) AND " +
           "(:subjectId IS NULL OR c.subject.id = :subjectId) AND " +
           "(:teacherId IS NULL OR c.teacher.id = :teacherId) AND " +
           "(:active IS NULL OR c.active = :active)")
    Page<CourseEntity> search(String keyword, Long semesterId, Long subjectId, Long teacherId, Boolean active, Pageable pageable);

    @Query("SELECT COUNT(c) FROM CourseEntity c WHERE " +
           "(:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:semesterId IS NULL OR c.semester.id = :semesterId) AND " +
           "(:subjectId IS NULL OR c.subject.id = :subjectId) AND " +
           "(:teacherId IS NULL OR c.teacher.id = :teacherId) AND " +
           "(:active IS NULL OR c.active = :active)")
    long count(String keyword, Long semesterId, Long subjectId, Long teacherId, Boolean active);
}
