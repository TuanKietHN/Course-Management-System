package vn.com.nws.cms.modules.auth.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.com.nws.cms.domain.enums.RoleType;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.UserEntity;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.UserRoleEntity;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    @Mapping(target = "roles", source = "userRoles", qualifiedByName = "toRoleTypes")
    User toDomain(UserEntity entity);

    @Mapping(target = "userRoles", ignore = true) // Handled manually
    UserEntity toEntity(User domain);

    @Named("toRoleTypes")
    default Set<RoleType> toRoleTypes(List<UserRoleEntity> userRoles) {
        if (userRoles == null) return Collections.emptySet();
        return userRoles.stream()
                .map(userRole -> {
                    try {
                        return RoleType.valueOf(userRole.getRole().getName().replace("ROLE_", ""));
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(roleType -> roleType != null)
                .collect(Collectors.toSet());
    }
}
