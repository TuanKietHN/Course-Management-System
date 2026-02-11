package vn.com.nws.cms.modules.academic.application;

import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.modules.academic.api.dto.*;

public interface SubjectService {
    PageResponse<SubjectResponse> getSubjects(SubjectFilterRequest request);
    SubjectResponse getSubjectById(Long id);
    SubjectResponse createSubject(SubjectCreateRequest request);
    SubjectResponse updateSubject(Long id, SubjectUpdateRequest request);
    void deleteSubject(Long id);
}
