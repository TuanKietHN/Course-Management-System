package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Builder;
import lombok.Data;
import vn.com.nws.cms.modules.user.api.dto.UserResponse;

import java.time.LocalDateTime;

@Data
@Builder
public class EnrollmentResponse {
    private Long id;
    private CourseResponse course;
    private UserResponse student;
    private String status;
    private Double grade;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
