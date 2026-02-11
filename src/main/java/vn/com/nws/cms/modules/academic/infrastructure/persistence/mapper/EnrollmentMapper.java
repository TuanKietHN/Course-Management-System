package vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import vn.com.nws.cms.modules.academic.domain.model.Enrollment;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.EnrollmentEntity;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = {CourseMapper.class, UserMapper.class})
public interface EnrollmentMapper {
    Enrollment toDomain(EnrollmentEntity entity);
    EnrollmentEntity toEntity(Enrollment domain);
}
