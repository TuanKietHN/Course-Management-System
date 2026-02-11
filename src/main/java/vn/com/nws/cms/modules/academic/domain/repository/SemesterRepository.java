package vn.com.nws.cms.modules.academic.domain.repository;

import vn.com.nws.cms.modules.academic.domain.model.Semester;

import java.util.List;
import java.util.Optional;

public interface SemesterRepository {
    Semester save(Semester semester);
    Optional<Semester> findById(Long id);
    Optional<Semester> findByCode(String code);
    void deleteById(Long id);
    boolean existsByCode(String code);
    
    List<Semester> search(String keyword, Boolean active, int page, int size);
    long count(String keyword, Boolean active);
    
    Optional<Semester> findActiveSemester();
}
