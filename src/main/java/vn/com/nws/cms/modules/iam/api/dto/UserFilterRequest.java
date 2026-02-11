package vn.com.nws.cms.modules.iam.api.dto;

import lombok.Data;

@Data
public class UserFilterRequest {
    private String keyword; // Search by username or email
    private String role;
    private int page = 1;
    private int size = 10;
}
