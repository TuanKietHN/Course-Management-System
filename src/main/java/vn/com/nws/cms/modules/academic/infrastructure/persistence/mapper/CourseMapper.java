package vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.com.nws.cms.modules.academic.domain.model.Course;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.CourseEntity;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = {SubjectMapper.class, SemesterMapper.class, UserMapper.class})
public interface CourseMapper {
    Course toDomain(CourseEntity entity);
    CourseEntity toEntity(Course domain);
}
