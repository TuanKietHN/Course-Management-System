package vn.com.nws.cms.modules.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.common.audit.AuditEntity;
import vn.com.nws.cms.domain.enums.RoleType;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends AuditEntity {

    private Long id;
    private String username;
    private String password;
    private String email;
    private String avatar;
    private Set<RoleType> roles;
}
