package vn.com.nws.cms.modules.user.application;

import org.springframework.web.multipart.MultipartFile;
import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.modules.user.api.dto.*;

public interface UserService {
    PageResponse<UserResponse> getUsers(UserFilterRequest request);
    UserResponse getUserById(Long id);
    UserResponse createUser(UserCreateRequest request);
    UserResponse updateUser(Long id, UserUpdateRequest request);
    UserResponse uploadAvatar(Long id, MultipartFile file);
    void deleteUser(Long id);
}
