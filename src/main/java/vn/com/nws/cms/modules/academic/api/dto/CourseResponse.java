package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Builder;
import lombok.Data;
import vn.com.nws.cms.modules.user.api.dto.UserResponse;

import java.time.LocalDateTime;

@Data
@Builder
public class CourseResponse {
    private Long id;
    private String name;
    private String code;
    private Integer maxStudents;
    private Integer currentStudents;
    private boolean active;
    
    private SubjectResponse subject;
    private SemesterResponse semester;
    private UserResponse teacher;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
