package vn.com.nws.cms.modules.academic.api.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CourseUpdateRequest {
    private String name;
    private String code;
    
    @Min(value = 1, message = "Max students must be at least 1")
    private Integer maxStudents;
    
    private Long subjectId;
    private Long semesterId;
    private Long teacherId;
    private Boolean active;
}
