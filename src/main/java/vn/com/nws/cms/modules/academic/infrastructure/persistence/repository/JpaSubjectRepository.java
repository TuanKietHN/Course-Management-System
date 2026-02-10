package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.SubjectEntity;

import java.util.Optional;

@Repository
public interface JpaSubjectRepository extends JpaRepository<SubjectEntity, Long> {
    Optional<SubjectEntity> findByCode(String code);
    boolean existsByCode(String code);

    @Query("SELECT s FROM SubjectEntity s WHERE (:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (:active IS NULL OR s.active = :active)")
    Page<SubjectEntity> search(String keyword, Boolean active, Pageable pageable);

    @Query("SELECT COUNT(s) FROM SubjectEntity s WHERE (:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (:active IS NULL OR s.active = :active)")
    long count(String keyword, Boolean active);
}
