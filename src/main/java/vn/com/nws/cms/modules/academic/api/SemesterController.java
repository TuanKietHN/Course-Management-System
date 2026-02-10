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
import vn.com.nws.cms.modules.academic.application.SemesterService;

@RestController
@RequestMapping("/api/v1/semesters")
@RequiredArgsConstructor
@Tag(name = "Academic - Semester", description = "Quản lý học kỳ")
public class SemesterController {

    private final SemesterService semesterService;

    @GetMapping
    @Operation(summary = "Danh sách học kỳ", description = "Lấy danh sách học kỳ có phân trang và tìm kiếm")
    public ResponseEntity<ApiResponse<PageResponse<SemesterResponse>>> getSemesters(
            @Parameter(description = "Từ khóa tìm kiếm (tên, mã)") @RequestParam(required = false) String keyword,
            @Parameter(description = "Trạng thái hoạt động") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Số trang (bắt đầu từ 1)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "10") int size
    ) {
        SemesterFilterRequest request = new SemesterFilterRequest();
        request.setKeyword(keyword);
        request.setActive(active);
        request.setPage(page);
        request.setSize(size);

        PageResponse<SemesterResponse> response = semesterService.getSemesters(request);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách học kỳ thành công", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết học kỳ", description = "Lấy thông tin chi tiết của một học kỳ")
    public ResponseEntity<ApiResponse<SemesterResponse>> getSemesterById(@PathVariable Long id) {
        SemesterResponse response = semesterService.getSemesterById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin học kỳ thành công", response));
    }

    @GetMapping("/active")
    @Operation(summary = "Học kỳ hiện tại", description = "Lấy thông tin học kỳ đang hoạt động")
    public ResponseEntity<ApiResponse<SemesterResponse>> getActiveSemester() {
        SemesterResponse response = semesterService.getActiveSemester();
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin học kỳ hiện tại thành công", response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo học kỳ mới", description = "Tạo mới một học kỳ (Admin)")
    public ResponseEntity<ApiResponse<SemesterResponse>> createSemester(@Valid @RequestBody SemesterCreateRequest request) {
        SemesterResponse response = semesterService.createSemester(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo học kỳ thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật học kỳ", description = "Cập nhật thông tin học kỳ (Admin)")
    public ResponseEntity<ApiResponse<SemesterResponse>> updateSemester(
            @PathVariable Long id,
            @Valid @RequestBody SemesterUpdateRequest request) {
        SemesterResponse response = semesterService.updateSemester(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật học kỳ thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa học kỳ", description = "Xóa học kỳ (Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteSemester(@PathVariable Long id) {
        semesterService.deleteSemester(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa học kỳ thành công", null));
    }
}
