package vn.com.nws.cms.modules.academic.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnrollmentCreateRequest {
    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    @NotNull(message = "Student ID is required")
    private Long studentId;
}
