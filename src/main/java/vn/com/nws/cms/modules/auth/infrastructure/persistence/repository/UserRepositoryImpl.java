package vn.com.nws.cms.modules.auth.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.UserEntity;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.mapper.UserMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaUserRepository;
    private final UserMapper userMapper;

    @Override
    public User save(User user) {
        UserEntity entity = userMapper.toEntity(user);
        // Ensure ID is preserved if updating
        if (user.getId() != null) {
            entity.setId(user.getId());
        }
        UserEntity savedEntity = jpaUserRepository.save(entity);
        return userMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaUserRepository.findById(id).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaUserRepository.findByUsername(username).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email).map(userMapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaUserRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmail(email);
    }

    @Override
    public void deleteById(Long id) {
        jpaUserRepository.deleteById(id);
    }

    @Override
    public List<User> search(String keyword, String role, int page, int size) {
        Page<UserEntity> entities = jpaUserRepository.search(keyword, role, PageRequest.of(page - 1, size));
        return entities.getContent().stream()
                .map(userMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count(String keyword, String role) {
        return jpaUserRepository.count(keyword, role);
    }
}
