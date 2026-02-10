package vn.com.nws.cms.modules.academic.application;

import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.modules.academic.api.dto.*;

public interface CourseService {
    PageResponse<CourseResponse> getCourses(CourseFilterRequest request);
    CourseResponse getCourseById(Long id);
    CourseResponse createCourse(CourseCreateRequest request);
    CourseResponse updateCourse(Long id, CourseUpdateRequest request);
    void deleteCourse(Long id);
}
