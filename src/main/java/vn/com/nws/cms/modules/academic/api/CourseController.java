package vn.com.nws.cms.modules.academic.api;

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
import vn.com.nws.cms.modules.academic.api.dto.*;
import vn.com.nws.cms.modules.academic.application.CourseService;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Tag(name = "Academic - Course", description = "Quản lý lớp học phần")
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    @Operation(summary = "Danh sách lớp học phần", description = "Lấy danh sách lớp học phần có phân trang và tìm kiếm")
    public ResponseEntity<ApiResponse<PageResponse<CourseResponse>>> getCourses(
            @Parameter(description = "Từ khóa tìm kiếm (tên, mã)") @RequestParam(required = false) String keyword,
            @Parameter(description = "ID học kỳ") @RequestParam(required = false) Long semesterId,
            @Parameter(description = "ID môn học") @RequestParam(required = false) Long subjectId,
            @Parameter(description = "ID giảng viên") @RequestParam(required = false) Long teacherId,
            @Parameter(description = "Trạng thái hoạt động") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Số trang (bắt đầu từ 1)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "10") int size
    ) {
        CourseFilterRequest request = new CourseFilterRequest();
        request.setKeyword(keyword);
        request.setSemesterId(semesterId);
        request.setSubjectId(subjectId);
        request.setTeacherId(teacherId);
        request.setActive(active);
        request.setPage(page);
        request.setSize(size);

        PageResponse<CourseResponse> response = courseService.getCourses(request);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách lớp học phần thành công", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết lớp học phần", description = "Lấy thông tin chi tiết của một lớp học phần")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(@PathVariable Long id) {
        CourseResponse response = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin lớp học phần thành công", response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo lớp học phần mới", description = "Tạo mới một lớp học phần (Admin)")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(@Valid @RequestBody CourseCreateRequest request) {
        CourseResponse response = courseService.createCourse(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo lớp học phần thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật lớp học phần", description = "Cập nhật thông tin lớp học phần (Admin)")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseUpdateRequest request) {
        CourseResponse response = courseService.updateCourse(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật lớp học phần thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa lớp học phần", description = "Xóa lớp học phần (Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa lớp học phần thành công", null));
    }
}
