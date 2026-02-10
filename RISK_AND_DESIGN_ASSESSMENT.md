# Đánh Giá Rủi Ro và Vi Phạm Nguyên Tắc Thiết Kế

## 1. Các Vi Phạm Nguyên Tắc Thiết Kế (Design Principles)

### 1.1. Vi Phạm SOLID

*   **Single Responsibility Principle (SRP) - Nguyên lý đơn nhiệm**:
    *   **Vấn đề**: Class `AuthService` đang đảm nhận quá nhiều trách nhiệm: Đăng ký, Đăng nhập, Refresh Token, Reset Password, Logout.
    *   **Hậu quả**: Class này sẽ phình to rất nhanh (`God Class`), gây khó khăn khi sửa đổi logic xác thực mà không làm ảnh hưởng đến logic đăng ký.
    *   **Đề xuất**: Tách thành `LoginService`, `RegistrationService`, `TokenService`.

*   **Open/Closed Principle (OCP) - Nguyên lý Đóng/Mở**:
    *   **Vấn đề**: Các vai trò (`role`) đang được sử dụng dưới dạng chuỗi cứng (`Hardcoded String`) như `"ROLE_ADMIN"`, `"ROLE_STUDENT"` rải rác khắp nơi trong code (Service, Controller).
    *   **Hậu quả**: Khi muốn thêm một Role mới (ví dụ: `ROLE_MANAGER`), lập trình viên phải tìm và sửa code ở nhiều file khác nhau (Switch-case logic), dễ gây lỗi sót.

### 1.2. Vi Phạm Domain-Driven Design (DDD)

*   **Anemic Domain Model (Mô hình tên miền thiếu máu)**:
    *   **Vấn đề**: Các Entity như `User`, `Course` hiện tại chỉ đóng vai trò chứa dữ liệu (với các annotation `@Data`, Getter/Setter) mà hoàn toàn thiếu logic nghiệp vụ. Logic kiểm tra (Validation) đang bị đẩy hết sang Service Layer.
    *   **Nguyên tắc DDD**: Entity nên giàu logic (Rich Model) để tự đảm bảo tính đúng đắn của dữ liệu (Invariants).

*   **Primitive Obsession (Ám ảnh kiểu nguyên thủy)**:
    *   **Vấn đề**: Sử dụng `String` để đại diện cho `Role`, `Email`, `Phone`.
    *   **Hậu quả**: Không tận dụng được type-safety của Java. Dễ xảy ra lỗi như gán nhầm chuỗi bất kỳ vào `role`.

## 2. Phân Tích Nguy Cơ Tiềm Ẩn (Risks)

### 2.1. Rủi ro về Nghiệp vụ (Business Risks)
*   **Hạn chế mô hình Role**: Việc chỉ cho phép 1 role/user là rủi ro lớn nhất. Trong thực tế các trường đại học, một người thường xuyên kiêm nhiệm (VD: Giảng viên kiêm Trưởng khoa/Admin, Sinh viên kiêm Trợ giảng). Kiến trúc hiện tại chặn đứng khả năng này.
*   **Khó mở rộng quy trình nghiệp vụ**: Do logic nằm chặt trong Service (Procedural code), việc thay đổi quy trình (ví dụ: thêm bước phê duyệt khi đăng ký) sẽ rủi ro cao.

### 2.2. Rủi ro về Bảo mật & Bảo trì (Security & Maintainability Risks)
*   **Magic Strings**: Việc dùng String cho Role (`"ROLE_ADMIN"`) rất dễ gây lỗi đánh máy (Typo). Ví dụ: gõ nhầm thành `"ROLE_ADMN"` sẽ làm hổng lỗ hổng bảo mật mà Compiler không phát hiện được.
*   **Coupling (Sự phụ thuộc)**: Nếu module `academic` gọi trực tiếp Entity của `user` (thay vì qua Interface hoặc DTO), hai module này sẽ bị dính chặt vào nhau, làm mất đi ý nghĩa của việc tách module ban đầu.

## 3. Kiến nghị Cải thiện
1.  **Refactor Role**: Chuyển `role` từ `String` sang `Set<Role>` (Many-to-Many relationship).
2.  **Sử dụng Enum**: Định nghĩa `RoleType` enum để quản lý danh sách role.
3.  **Tách nhỏ Service**: Áp dụng SRP để chia nhỏ `AuthService`.
4.  **Rich Domain Model**: Di chuyển logic validate từ Service vào bên trong Entity.
