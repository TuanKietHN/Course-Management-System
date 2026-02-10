package vn.com.nws.cms.modules.academic.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.common.audit.AuditEntity;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Semester extends AuditEntity {
    private Long id;
    private String name;
    private String code;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
}
