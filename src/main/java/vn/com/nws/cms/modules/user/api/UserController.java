package vn.com.nws.cms.modules.user.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.com.nws.cms.common.dto.ApiResponse;
import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.modules.user.api.dto.*;
import vn.com.nws.cms.modules.user.application.UserService;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "Quản lý người dùng (Admin only)")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Danh sách người dùng", description = "Lấy danh sách người dùng có phân trang và tìm kiếm")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getUsers(
            @Parameter(description = "Từ khóa tìm kiếm (username, email)") @RequestParam(required = false) String keyword,
            @Parameter(description = "Lọc theo vai trò (ADMIN, TEACHER, STUDENT)") @RequestParam(required = false) String role,
            @Parameter(description = "Số trang (bắt đầu từ 1)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "10") int size
    ) {
        UserFilterRequest request = new UserFilterRequest();
        request.setKeyword(keyword);
        request.setRole(role);
        request.setPage(page);
        request.setSize(size);

        PageResponse<UserResponse> response = userService.getUsers(request);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách người dùng thành công", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết người dùng", description = "Lấy thông tin chi tiết của một người dùng")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin người dùng thành công", response));
    }

    @PostMapping
    @Operation(summary = "Tạo người dùng mới", description = "Tạo mới một người dùng (Admin)")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo người dùng thành công", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật người dùng", description = "Cập nhật thông tin người dùng")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật người dùng thành công", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa người dùng", description = "Xóa vĩnh viễn người dùng khỏi hệ thống")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa người dùng thành công", null));
    }
}
