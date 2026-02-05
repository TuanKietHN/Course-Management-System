# Luồng Use Case (Use Case Flows)

## 1. Luồng Đăng ký học của Sinh viên
*(Giữ nguyên luồng này)*

```mermaid
sequenceDiagram
    actor Student as Sinh viên
    participant Frontend
    participant EnrollmentAPI as API Đăng ký
    participant CourseService as Dịch vụ Khóa học
    participant Database as Cơ sở dữ liệu

    Student->>Frontend: Chọn Lớp học phần
    Frontend->>EnrollmentAPI: POST /api/enrollments
    Note right of Frontend: Header: Bearer <JWT>
    
    EnrollmentAPI->>CourseService: Kiểm tra Điều kiện & Sĩ số
    CourseService->>Database: Đếm số lượng đã đăng ký
    Database-->>CourseService: Trả về số lượng
    
    alt Sĩ số đã đầy
        CourseService-->>EnrollmentAPI: Lỗi: Lớp đã đầy
        EnrollmentAPI-->>Frontend: 400 Bad Request
        Frontend-->>Student: Hiển thị thông báo lỗi
    else Còn chỗ
        EnrollmentAPI->>Database: Lưu bản ghi Đăng ký
        Database-->>EnrollmentAPI: Đã lưu
        EnrollmentAPI-->>Frontend: 201 Created
        Frontend-->>Student: Đăng ký thành công
    end
```

## 2. Luồng Nhập điểm của Giảng viên
*(Giữ nguyên luồng này)*

## 3. Luồng Đăng nhập (Custom JWT + Redis)

```mermaid
sequenceDiagram
    actor User as Người dùng
    participant Frontend
    participant AuthController
    participant AuthService
    participant JwtProvider
    participant Redis
    participant Database

    User->>Frontend: Nhập Username/Password
    Frontend->>AuthController: POST /api/auth/login
    
    AuthController->>AuthService: Xác thực người dùng
    AuthService->>Database: Tìm User & Hash Password
    Database-->>AuthService: Trả về User
    
    alt Sai thông tin
        AuthService-->>AuthController: Lỗi: Sai Credentials
        AuthController-->>Frontend: 401 Unauthorized
    else Đúng thông tin
        AuthService->>JwtProvider: Generate Access Token
        JwtProvider-->>AuthService: Access Token (JWT)
        
        AuthService->>JwtProvider: Generate Refresh Token (UUID)
        JwtProvider-->>AuthService: Refresh Token
        
        AuthService->>Redis: Lưu Refresh Token (Key: rt:{username}, TTL: 7 days)
        Redis-->>AuthService: OK
        
        AuthService-->>AuthController: TokenResponse (Access + Refresh)
        AuthController-->>Frontend: 200 OK (JSON)
    end
```

## 4. Luồng Refresh Token

```mermaid
sequenceDiagram
    participant Frontend
    participant AuthController
    participant AuthService
    participant Redis
    participant JwtProvider

    Frontend->>AuthController: POST /api/auth/refresh (RefreshToken)
    AuthController->>AuthService: verifyRefreshToken(token)
    
    AuthService->>Redis: Get Key rt:{username}
    Redis-->>AuthService: Stored Token
    
    alt Token không khớp hoặc hết hạn
        AuthService-->>AuthController: Lỗi: Token Invalid
        AuthController-->>Frontend: 401 Unauthorized
    else Token hợp lệ
        AuthService->>JwtProvider: Generate New Access Token
        AuthService->>JwtProvider: Generate New Refresh Token
        AuthService->>Redis: Cập nhật Refresh Token mới
        AuthService-->>AuthController: New TokenPair
        AuthController-->>Frontend: 200 OK
    end
```
