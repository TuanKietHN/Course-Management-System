package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Data;

@Data
public class CourseFilterRequest {
    private String keyword;
    private Long semesterId;
    private Long subjectId;
    private Long teacherId;
    private Boolean active;
    private int page = 1;
    private int size = 10;
}
