package vn.com.nws.cms.modules.academic.api.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class SubjectUpdateRequest {
    private String name;
    private String code;
    
    @Min(value = 0, message = "Credit must be greater than or equal to 0")
    private Integer credit;
    
    private String description;
    private Boolean active;
}
