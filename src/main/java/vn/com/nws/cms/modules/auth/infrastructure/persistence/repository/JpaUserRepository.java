package vn.com.nws.cms.modules.auth.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.UserEntity;

import java.util.Optional;

@Repository
public interface JpaUserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT u FROM UserEntity u LEFT JOIN u.userRoles ur LEFT JOIN ur.role r WHERE (:keyword IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (:roleName IS NULL OR r.name = :roleName)")
    Page<UserEntity> search(String keyword, String roleName, Pageable pageable);
    
    @Query("SELECT COUNT(DISTINCT u) FROM UserEntity u LEFT JOIN u.userRoles ur LEFT JOIN ur.role r WHERE (:keyword IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (:roleName IS NULL OR r.name = :roleName)")
    long count(String keyword, String roleName);
}
