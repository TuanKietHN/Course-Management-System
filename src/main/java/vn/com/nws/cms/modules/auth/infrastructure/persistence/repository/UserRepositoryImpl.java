package vn.com.nws.cms.modules.auth.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.domain.enums.RoleType;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.RoleEntity;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.UserEntity;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.UserRoleEntity;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.UserRoleId;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.mapper.UserMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaUserRepository;
    private final JpaRoleRepository jpaRoleRepository;
    private final UserMapper userMapper;

    @Override
    public User save(User user) {
        UserEntity entity;

        // Ensure ID is preserved if updating
        if (user.getId() != null) {
            // Fetch existing entity to avoid detaching or issues with updates
            Optional<UserEntity> existingOpt = jpaUserRepository.findById(user.getId());
            if (existingOpt.isPresent()) {
                UserEntity existing = existingOpt.get();
                UserEntity mappedEntity = userMapper.toEntity(user);
                
                // Copy non-relational fields
                existing.setUsername(mappedEntity.getUsername());
                existing.setPassword(mappedEntity.getPassword());
                existing.setEmail(mappedEntity.getEmail());
                existing.setAvatar(mappedEntity.getAvatar());
                entity = existing;
            } else {
                entity = userMapper.toEntity(user);
                entity.setId(user.getId());
            }
        } else {
            entity = userMapper.toEntity(user);
        }

        // Map roles manually using UserRoleEntity
        if (user.getRoles() != null) {
            // Clear existing roles if we are updating, or start fresh
            if (entity.getUserRoles() == null) {
                entity.setUserRoles(new ArrayList<>());
            } else {
                entity.getUserRoles().clear();
            }

            final UserEntity finalEntity = entity; // Make final for lambda use
            for (RoleType roleType : user.getRoles()) {
                jpaRoleRepository.findByName(roleType.authority())
                        .ifPresent(roleEntity -> {
                            UserRoleEntity userRole = new UserRoleEntity();
                            // If iam ID is null (new iam), ID part will be null.
                            // However, we can set association and let JPA handle it if we use @MapsId properly or save iam first.
                            // With composite key and @MapsId, we usually just set the relation.
                            userRole.setId(new UserRoleId(finalEntity.getId(), roleEntity.getId())); 
                            userRole.setUser(finalEntity);
                            userRole.setRole(roleEntity);
                            userRole.setAssignedAt(LocalDateTime.now());
                            finalEntity.getUserRoles().add(userRole);
                        });
            }
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
        String roleName = null;
        if (role != null && !role.isEmpty()) {
            if (!role.startsWith("ROLE_")) {
                roleName = "ROLE_" + role;
            } else {
                roleName = role;
            }
        }
        Page<UserEntity> entities = jpaUserRepository.search(keyword, roleName, PageRequest.of(page - 1, size));
        return entities.getContent().stream()
                .map(userMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count(String keyword, String role) {
        String roleName = null;
        if (role != null && !role.isEmpty()) {
            if (!role.startsWith("ROLE_")) {
                roleName = "ROLE_" + role;
            } else {
                roleName = role;
            }
        }
        return jpaUserRepository.count(keyword, roleName);
    }
}
