package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.EnrollmentEntity;

import java.util.List;

@Repository
public interface JpaEnrollmentRepository extends JpaRepository<EnrollmentEntity, Long> {
    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);
    List<EnrollmentEntity> findByCourseId(Long courseId);
    List<EnrollmentEntity> findByStudentId(Long studentId);
    long countByCourseId(Long courseId);
}
