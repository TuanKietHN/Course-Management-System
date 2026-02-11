package vn.com.nws.cms.modules.academic.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.common.audit.AuditEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subject extends AuditEntity {
    private Long id;
    private String name;
    private String code;
    private Integer credits;
    private String description;
    private boolean active;
    private Long departmentId;
    private Integer theoryHours;
    private Integer practiceHours;
}
