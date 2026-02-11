package vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import vn.com.nws.cms.modules.academic.domain.model.Semester;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.SemesterEntity;

@Mapper(componentModel = "spring")
public interface SemesterMapper {
    Semester toDomain(SemesterEntity entity);
    SemesterEntity toEntity(Semester domain);
}
