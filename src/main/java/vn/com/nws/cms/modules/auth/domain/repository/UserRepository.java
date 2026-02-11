package vn.com.nws.cms.modules.auth.domain.repository;

import vn.com.nws.cms.modules.auth.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    void deleteById(Long id);
    
    // Pagination support
    List<User> search(String keyword, String role, int page, int size);
    long count(String keyword, String role);
}
