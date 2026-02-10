package vn.com.nws.cms.modules.user.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
