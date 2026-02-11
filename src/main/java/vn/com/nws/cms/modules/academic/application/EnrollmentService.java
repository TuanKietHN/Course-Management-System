package vn.com.nws.cms.modules.academic.application;

import vn.com.nws.cms.modules.academic.api.dto.*;

import java.util.List;

public interface EnrollmentService {
    EnrollmentResponse enrollStudent(EnrollmentCreateRequest request);
    EnrollmentResponse updateEnrollment(Long id, EnrollmentUpdateRequest request);
    void deleteEnrollment(Long id);
    
    List<EnrollmentResponse> getStudentEnrollments(Long studentId);
    List<EnrollmentResponse> getCourseEnrollments(Long courseId);
}
