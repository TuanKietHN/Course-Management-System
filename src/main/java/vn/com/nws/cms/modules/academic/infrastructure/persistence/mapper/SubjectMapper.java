package vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import vn.com.nws.cms.modules.academic.domain.model.Subject;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.SubjectEntity;

@Mapper(componentModel = "spring")
public interface SubjectMapper {
    Subject toDomain(SubjectEntity entity);
    SubjectEntity toEntity(Subject domain);
}
