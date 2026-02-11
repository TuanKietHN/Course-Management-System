package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SemesterUpdateRequest {
    private String name;
    private String code;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;
}
