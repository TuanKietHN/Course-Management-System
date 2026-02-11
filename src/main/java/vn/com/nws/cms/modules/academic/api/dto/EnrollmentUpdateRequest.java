package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Data;

@Data
public class EnrollmentUpdateRequest {
    private String status;
    private Double grade;
}
