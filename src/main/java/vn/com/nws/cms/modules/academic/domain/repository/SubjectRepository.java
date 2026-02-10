package vn.com.nws.cms.modules.academic.domain.repository;

import vn.com.nws.cms.modules.academic.domain.model.Subject;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository {
    Subject save(Subject subject);
    Optional<Subject> findById(Long id);
    Optional<Subject> findByCode(String code);
    void deleteById(Long id);
    boolean existsByCode(String code);
    
    List<Subject> search(String keyword, Boolean active, int page, int size);
    long count(String keyword, Boolean active);
}
