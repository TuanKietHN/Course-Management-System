package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.domain.model.Semester;
import vn.com.nws.cms.modules.academic.domain.repository.SemesterRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.SemesterEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper.SemesterMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SemesterRepositoryImpl implements SemesterRepository {

    private final JpaSemesterRepository jpaSemesterRepository;
    private final SemesterMapper semesterMapper;

    @Override
    public Semester save(Semester semester) {
        SemesterEntity entity = semesterMapper.toEntity(semester);
        if (semester.getId() != null) {
            entity.setId(semester.getId());
        }
        SemesterEntity savedEntity = jpaSemesterRepository.save(entity);
        return semesterMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Semester> findById(Long id) {
        return jpaSemesterRepository.findById(id).map(semesterMapper::toDomain);
    }

    @Override
    public Optional<Semester> findByCode(String code) {
        return jpaSemesterRepository.findByCode(code).map(semesterMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaSemesterRepository.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaSemesterRepository.existsByCode(code);
    }

    @Override
    public List<Semester> search(String keyword, Boolean active, int page, int size) {
        return jpaSemesterRepository.search(keyword, active, PageRequest.of(page - 1, size))
                .getContent().stream()
                .map(semesterMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count(String keyword, Boolean active) {
        return jpaSemesterRepository.count(keyword, active);
    }

    @Override
    public Optional<Semester> findActiveSemester() {
        return jpaSemesterRepository.findByActiveTrue().map(semesterMapper::toDomain);
    }
}
