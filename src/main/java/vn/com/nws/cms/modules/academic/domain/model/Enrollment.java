package vn.com.nws.cms.modules.academic.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.common.audit.AuditEntity;
import vn.com.nws.cms.modules.auth.domain.model.User;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment extends AuditEntity {
    private Long id;
    private Course course;
    private User student;
    private String status; // ENROLLED, COMPLETED, DROPPED, CANCELLED
    private Double grade;
}
