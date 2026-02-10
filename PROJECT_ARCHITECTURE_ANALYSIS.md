# Phân Tích Kiến Trúc Dự Án CMS

## 1. Tại sao tách Module User và Academic?

Việc tách biệt module `user` (hoặc `auth`) ra khỏi `academic` là một quyết định thiết kế hợp lý theo hướng **Modular Monolith** hoặc **Domain-Driven Design (DDD)**. Dưới đây là các lý do chi tiết:

### 1.1. Separation of Concerns (Phân tách mối quan tâm)
*   **Module `user`/`auth`**: Chịu trách nhiệm về **Identity & Access Management (IAM)**. Nó trả lời câu hỏi "Người này là ai?" và "Họ có quyền truy cập hệ thống không?". Logic ở đây bao gồm đăng ký, đăng nhập, quên mật khẩu, quản lý token (JWT).
*   **Module `academic`**: Chịu trách nhiệm về **Core Domain (Nghiệp vụ cốt lõi)** của hệ thống quản lý đào tạo. Nó xử lý các luồng nghiệp vụ như mở lớp, đăng ký học, nhập điểm.

Việc trộn lẫn logic xác thực (kỹ thuật) với logic đào tạo (nghiệp vụ) sẽ tạo ra một khối code khổng lồ (Monolith), khó bảo trì và nâng cấp.

### 1.2. Bounded Contexts (Ngữ cảnh giới hạn) trong DDD
Trong DDD, cùng một thực thể "Người dùng" nhưng ở các ngữ cảnh khác nhau sẽ có ý nghĩa khác nhau:
*   Trong **Auth Context**: User là một `Account` (username, password, roles).
*   Trong **Academic Context**: User đó đóng vai trò là `Student` (có MSSV, GPA) hoặc `Teacher` (có bằng cấp, khoa).

Việc tách module giúp mô hình hóa dữ liệu chính xác hơn. `Student` không cần biết về `passwordHash`, và `Account` không cần biết về `GPA`.

### 1.3. Khả năng mở rộng và tái sử dụng
Nếu sau này hệ thống cần tích hợp với một hệ thống khác (ví dụ: Thư viện, Kế toán), module `user` có thể được tái sử dụng để xác thực trung tâm (SSO) mà không kéo theo logic của `academic`.

---

## 2. Phân tích cơ chế Role (Vai trò)

**Câu hỏi:** *User này có vừa làm admin, vừa làm user, vừa làm teacher được không?*

**Trả lời:** **KHÔNG**. Hệ thống hiện tại **chỉ hỗ trợ duy nhất 1 vai trò** cho mỗi người dùng tại một thời điểm.

### Bằng chứng từ mã nguồn:

1.  **Entity Definition (`User.java`, `UserEntity.java`)**:
    Thuộc tính `role` được định nghĩa là một `String` đơn lẻ, không phải là một danh sách (`List` hay `Set`).
    ```java
    // Trích đoạn code hiện tại
    private String role; 
    ```
    Để hỗ trợ đa vai trò, cấu trúc cần phải là `Set<String> roles` hoặc `Set<Role> roles`.

2.  **Authentication Logic (`AuthService.java`)**:
    Khi tạo `UserDetails` để xác thực, hệ thống sử dụng `Collections.singletonList`, nghĩa là chỉ tạo ra danh sách chứa đúng 1 phần tử.
    ```java
    // Logic hiện tại trong code
    Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
    ```

### Hệ quả:
Một giảng viên (Teacher) sẽ không thể có quyền Admin để quản lý hệ thống trừ khi họ tạo một tài khoản khác. Điều này làm giảm tính linh hoạt của hệ thống trong thực tế.
