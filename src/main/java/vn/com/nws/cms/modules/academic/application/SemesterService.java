package vn.com.nws.cms.modules.academic.application;

import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.modules.academic.api.dto.*;

public interface SemesterService {
    PageResponse<SemesterResponse> getSemesters(SemesterFilterRequest request);
    SemesterResponse getSemesterById(Long id);
    SemesterResponse createSemester(SemesterCreateRequest request);
    SemesterResponse updateSemester(Long id, SemesterUpdateRequest request);
    void deleteSemester(Long id);
    SemesterResponse getActiveSemester();
}
