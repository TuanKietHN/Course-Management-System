package vn.com.nws.cms.modules.user.api.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateRequest {
    
    @Email(message = "Email should be valid")
    private String email;

    private String role;
    
    private String password; // Optional: update password if provided
}
