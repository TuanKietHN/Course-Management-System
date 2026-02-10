package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.domain.model.Subject;
import vn.com.nws.cms.modules.academic.domain.repository.SubjectRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.SubjectEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper.SubjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SubjectRepositoryImpl implements SubjectRepository {

    private final JpaSubjectRepository jpaSubjectRepository;
    private final SubjectMapper subjectMapper;

    @Override
    public Subject save(Subject subject) {
        SubjectEntity entity = subjectMapper.toEntity(subject);
        if (subject.getId() != null) {
            entity.setId(subject.getId());
        }
        SubjectEntity savedEntity = jpaSubjectRepository.save(entity);
        return subjectMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Subject> findById(Long id) {
        return jpaSubjectRepository.findById(id).map(subjectMapper::toDomain);
    }

    @Override
    public Optional<Subject> findByCode(String code) {
        return jpaSubjectRepository.findByCode(code).map(subjectMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaSubjectRepository.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaSubjectRepository.existsByCode(code);
    }

    @Override
    public List<Subject> search(String keyword, Boolean active, int page, int size) {
        return jpaSubjectRepository.search(keyword, active, PageRequest.of(page - 1, size))
                .getContent().stream()
                .map(subjectMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count(String keyword, Boolean active) {
        return jpaSubjectRepository.count(keyword, active);
    }
}
