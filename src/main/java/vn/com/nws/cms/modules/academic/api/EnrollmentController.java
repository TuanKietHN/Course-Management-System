package vn.com.nws.cms.modules.academic.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.com.nws.cms.common.dto.ApiResponse;
import vn.com.nws.cms.modules.academic.api.dto.*;
import vn.com.nws.cms.modules.academic.application.EnrollmentService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
@Tag(name = "Academic - Enrollment", description = "Quản lý đăng ký học phần")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @Operation(summary = "Đăng ký học phần", description = "Sinh viên đăng ký vào lớp học phần")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enrollStudent(@Valid @RequestBody EnrollmentCreateRequest request) {
        EnrollmentResponse response = enrollmentService.enrollStudent(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng ký học phần thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Cập nhật đăng ký", description = "Cập nhật trạng thái hoặc điểm số (Admin/Teacher)")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> updateEnrollment(
            @PathVariable Long id,
            @Valid @RequestBody EnrollmentUpdateRequest request) {
        EnrollmentResponse response = enrollmentService.updateEnrollment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật đăng ký thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Hủy đăng ký", description = "Hủy đăng ký học phần (Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteEnrollment(@PathVariable Long id) {
        enrollmentService.deleteEnrollment(id);
        return ResponseEntity.ok(ApiResponse.success("Hủy đăng ký thành công", null));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Danh sách học phần của sinh viên", description = "Lấy danh sách các lớp học phần sinh viên đã đăng ký")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getStudentEnrollments(@PathVariable Long studentId) {
        List<EnrollmentResponse> response = enrollmentService.getStudentEnrollments(studentId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách học phần thành công", response));
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Danh sách sinh viên trong lớp", description = "Lấy danh sách sinh viên đăng ký lớp học phần")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getCourseEnrollments(@PathVariable Long courseId) {
        List<EnrollmentResponse> response = enrollmentService.getCourseEnrollments(courseId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sinh viên thành công", response));
    }
}
