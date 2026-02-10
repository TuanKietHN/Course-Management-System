package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Data;

@Data
public class SubjectFilterRequest {
    private String keyword;
    private Boolean active;
    private int page = 1;
    private int size = 10;
}
