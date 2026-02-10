package vn.com.nws.cms.modules.user.application;

import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.modules.user.api.dto.*;

public interface UserService {
    PageResponse<UserResponse> getUsers(UserFilterRequest request);
    UserResponse getUserById(Long id);
    UserResponse createUser(UserCreateRequest request);
    UserResponse updateUser(Long id, UserUpdateRequest request);
    void deleteUser(Long id);
}
