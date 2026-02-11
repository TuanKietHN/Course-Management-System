package vn.com.nws.cms.modules.academic.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.common.audit.AuditEntity;

@Entity
@Table(name = "subjects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectEntity extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private Integer credits;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean active;
    
    // New fields from V3 migration
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private DepartmentEntity department;
    
    @Column(name = "theory_hours")
    private Integer theoryHours;
    
    @Column(name = "practice_hours")
    private Integer practiceHours;
    
    // Note: prerequisite_subject_ids is BIGINT[] in Postgres. 
    // JPA handling of arrays can be complex. 
    // For simplicity, we might skip mapping it directly or use a custom type if needed.
    // Or we can map it as List<Long> with appropriate Hibernate types.
}
