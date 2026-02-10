# Tài Liệu Cập Nhật Kiến Trúc: Multi-Role & Refactoring

## 1. Thay Đổi Về Nghiệp Vụ: Đa Vai Trò (Multi-Role)

### Hiện Trạng Cũ
- Mỗi User chỉ có 1 Role duy nhất (lưu dạng String: `ROLE_ADMIN`, `ROLE_TEACHER`, `ROLE_STUDENT`).
- Hạn chế: Không thể gán quyền Admin cho một Giảng viên, hoặc một Sinh viên không thể làm Trợ giảng (Teacher role).

### Cập Nhật Mới
- **Hỗ trợ Đa Vai Trò:** Một User có thể sở hữu nhiều Role cùng lúc.
- **Ví dụ:** Một tài khoản có thể vừa là `TEACHER` vừa là `ADMIN`.
- **Cấu trúc dữ liệu:**
  - Database: Bảng `user_roles` (hoặc mapping tương đương) lưu danh sách role của user.
  - Domain Model: `User` có `Set<RoleType> roles`.

## 2. Refactoring Kỹ Thuật

### 2.1. Chuẩn Hóa Role (Enum)
- **Trước đây:** Sử dụng chuỗi cứng (`Hardcoded String`) như `"ROLE_ADMIN"`. Dễ gây lỗi typo.
- **Hiện tại:** Sử dụng Enum `RoleType` nằm trong package `vn.com.nws.cms.domain.enums`.
  - Values: `ADMIN`, `TEACHER`, `STUDENT`.
  - Phương thức `authority()` trả về chuẩn Spring Security (`ROLE_...`).

### 2.2. Tách AuthService (SRP)
Class `AuthService` cũ đã được tách thành 3 service nhỏ hơn để tuân thủ nguyên lý Single Responsibility Principle (SRP):

1.  **`AuthenticationService`**:
    - Chịu trách nhiệm: Đăng nhập (`login`), Đăng xuất (`logout`), Làm mới token (`refreshToken`).
    - Xử lý Token Rotation và bảo mật Session.
2.  **`RegistrationService`**:
    - Chịu trách nhiệm: Đăng ký tài khoản mới (`register`).
    - Validate username/email và mã hóa mật khẩu.
3.  **`PasswordService`**:
    - Chịu trách nhiệm: Quên mật khẩu (`forgotPassword`) và Đặt lại mật khẩu (`resetPassword`).
    - Gửi email reset và xử lý token reset trong Redis.

### 2.3. Cập Nhật Domain & Entity
- **User (Domain)**: Chuyển field `role` (String) thành `roles` (`Set<RoleType>`).
- **UserEntity (Infrastructure)**: Sử dụng `@ElementCollection` để map `Set<RoleType>` xuống DB.

## 3. Hướng Dẫn Naming Convention (User vs IAM)

**Câu hỏi:** *Module `user` đổi tên thành `IAM` có đúng không?*

**Trả lời:**
- **IAM (Identity and Access Management)** là thuật ngữ chuẩn trong ngành, bao hàm cả việc Quản lý danh tính (`User Profile`, `Account`) và Quản lý truy cập (`Auth`, `Permission`).
- Trong các dự án lớn:
  - `IAM Service` thường là một microservice riêng biệt handling toàn bộ việc đăng ký, đăng nhập, phân quyền, quản lý user.
  - Nếu dự án của bạn gộp chung `modules/auth` và `modules/user`, việc đổi tên thư mục cha thành `modules/iam` là **RẤT HỢP LÝ**.
  - Tuy nhiên, nếu bạn muốn giữ cấu trúc module tách biệt:
    - `auth`: Chuyên về protocol (JWT, OAuth2, Login).
    - `user`: Chuyên về thông tin cá nhân (Avatar, Profile, Bio).
    - Thì việc giữ nguyên tên cũng không sai. Nhưng xu hướng hiện đại là gộp chung vào `Identity` hoặc `IAM`.

## 4. Các File Đã Thay Đổi/Tạo Mới
- `vn.com.nws.cms.domain.enums.RoleType` (New)
- `vn.com.nws.cms.modules.auth.application.AuthenticationService` (New)
- `vn.com.nws.cms.modules.auth.application.RegistrationService` (New)
- `vn.com.nws.cms.modules.auth.application.PasswordService` (New)
- `vn.com.nws.cms.modules.auth.application.AuthService` (Deleted)
- `vn.com.nws.cms.modules.auth.api.AuthController` (Updated)
- `vn.com.nws.cms.modules.auth.domain.model.User` (Updated)
- `vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.UserEntity` (Updated)
