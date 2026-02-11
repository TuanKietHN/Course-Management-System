package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.domain.model.Enrollment;
import vn.com.nws.cms.modules.academic.domain.repository.EnrollmentRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.EnrollmentEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper.EnrollmentMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class EnrollmentRepositoryImpl implements EnrollmentRepository {

    private final JpaEnrollmentRepository jpaEnrollmentRepository;
    private final EnrollmentMapper enrollmentMapper;

    @Override
    public Enrollment save(Enrollment enrollment) {
        EnrollmentEntity entity = enrollmentMapper.toEntity(enrollment);
        if (enrollment.getId() != null) {
            entity.setId(enrollment.getId());
        }
        EnrollmentEntity savedEntity = jpaEnrollmentRepository.save(entity);
        return enrollmentMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Enrollment> findById(Long id) {
        return jpaEnrollmentRepository.findById(id).map(enrollmentMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaEnrollmentRepository.deleteById(id);
    }

    @Override
    public boolean existsByCourseIdAndStudentId(Long courseId, Long studentId) {
        return jpaEnrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId);
    }

    @Override
    public List<Enrollment> findByCourseId(Long courseId) {
        return jpaEnrollmentRepository.findByCourseId(courseId).stream()
                .map(enrollmentMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Enrollment> findByStudentId(Long studentId) {
        return jpaEnrollmentRepository.findByStudentId(studentId).stream()
                .map(enrollmentMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByCourseId(Long courseId) {
        return jpaEnrollmentRepository.countByCourseId(courseId);
    }
}
