# Kế hoạch Triển khai Dự án Course Management System (CMS)

## 1. Tổng quan Dự án
*   **Tên dự án**: Course Management System (CMS)
*   **Mục tiêu**: Xây dựng hệ thống quản lý khóa học nội bộ.
*   **Kiến trúc**: Modular Monolith (Clean Architecture) hướng tới Microservices.
*   **Môi trường**: Java 21, Spring Boot.

## 2. Công nghệ & Công cụ (Tech Stack)

### Core Framework
*   **Java Development Kit (JDK)**: 21
*   **Spring Boot**: 4.0.2 (Hiện tại theo cấu hình) / *Khuyến nghị sử dụng bản Stable mới nhất (3.3.x)*
*   **Build Tool**: Maven

### Dependencies (Hiện tại & Bổ sung)
| Group | Artifact | Mục đích | Trạng thái |
|-------|----------|----------|------------|
| `org.springframework.boot` | `spring-boot-starter-webmvc` | REST API | Có sẵn |
| `org.springframework.boot` | `spring-boot-starter-data-jpa` | ORM / Database | Có sẵn |
| `org.springframework.boot` | `spring-boot-starter-security` | Authentication/Authorization | Có sẵn |
| `org.springframework.boot` | `spring-boot-starter-validation` | Data Validation | Có sẵn |
| `org.postgresql` | `postgresql` | Database Driver | Có sẵn |
| `org.flywaydb` | `flyway-core` | DB Migration | Có sẵn |
| `org.springframework.boot` | `spring-boot-starter-cache` | Caching | Có sẵn |
| `org.projectlombok` | `lombok` | Giảm boilerplate code | **Cần bổ sung** |
| `org.springdoc` | `springdoc-openapi-starter-webmvc-ui` | API Documentation | **Cần bổ sung** |
| `io.jsonwebtoken` | `jjwt-api`, `jjwt-impl`, `jjwt-jackson` | JWT Handling | **Cần bổ sung** |
| `org.mapstruct` | `mapstruct` | DTO Mapping (Optional) | **Cần bổ sung** |

## 3. Kiến trúc Hệ thống (Clean Code Structure)

Dự án được tổ chức theo cấu trúc **Modular Monolith**, phân tách rõ ràng giữa các module nghiệp vụ và các tầng (layers).

### Cấu trúc Layer (trong mỗi Module)
1.  **API Layer (`api`)**:
    *   Controllers: Tiếp nhận request.
    *   DTOs (Request/Response): Contract giao tiếp với client.
2.  **Application Layer (`application`)**:
    *   Services/UseCases: Xử lý nghiệp vụ (Business Logic), điều phối luồng dữ liệu.
3.  **Domain Layer (`domain`)**:
    *   Entities (Model): Đối tượng nghiệp vụ cốt lõi.
    *   Repository Interfaces: Định nghĩa hành vi truy xuất dữ liệu (không phụ thuộc framework cụ thể).
    *   Domain Events: Sự kiện nghiệp vụ (nếu có).
4.  **Infrastructure Layer (`infrastructure`)**:
    *   Persistence: Implement Repository (JPA).
    *   External: Tích hợp dịch vụ bên thứ 3 (Email, Storage, etc.).

## 4. Cấu trúc Thư mục Chi tiết

```
vn.edu.nws.cms
│
├── CmsApplication.java
│
├── common (Shared Kernel)
│   ├── config
│   │   ├── SecurityConfig.java
│   │   ├── JpaConfig.java
│   │   ├── OpenApiConfig.java
│   │   └── FlywayConfig.java
│   ├── exception
│   │   ├── GlobalExceptionHandler.java
│   │   ├── BusinessException.java
│   │   ├── ResourceNotFoundException.java
│   │   └── ErrorResponse.java
│   ├── dto
│   │   ├── PageResponse.java
│   │   ├── ApiResponse.java
│   ├── util
│   │   └── DateUtils.java
│   ├── constant
│   │   └── AppConstants.java
│   └── audit
│       └── AuditEntity.java (Base Entity cho created_at, updated_at)
│
└── modules
    ├── auth (Quản lý xác thực)
    │   ├── api
    │   ├── application
    │   ├── domain
    │   └── infrastructure
    │
    ├── user (Quản lý người dùng)
    │   ├── api
    │   ├── application
    │   ├── domain
    │   └── infrastructure
    │
    ├── academic (Quản lý học vụ: Khóa học, Học kỳ, Lớp học phần)
    │   ├── api
    │   ├── application
    │   ├── domain
    │   └── infrastructure
    │
    ├── enrollment (Quản lý đăng ký học)
    │   ├── api
    │   ├── application
    │   ├── domain
    │   └── infrastructure
    │
    ├── grading (Quản lý điểm số)
    │   ├── api
    │   ├── application
    │   ├── domain
    │   └── infrastructure
    │
    └── report (Báo cáo & Thống kê)
        ├── api
        ├── application
        └── infrastructure
```

## 5. Lộ trình Thực hiện (Roadmap)

### Giai đoạn 1: Khởi tạo & Common Framework
- [ ] Cập nhật `pom.xml` với các dependencies cần thiết.
- [ ] Thiết lập cấu trúc package `common` và `modules`.
- [ ] Cấu hình Database (PostgreSQL) và Migration (Flyway).
- [ ] Thiết lập Security (JWT Filter, Config) và Exception Handling global.
- [ ] Cấu hình Swagger/OpenAPI.

### Giai đoạn 2: Module Core (Auth & User)
- [ ] Implement `auth`: Login, Register, Refresh Token.
- [ ] Implement `user`: CRUD User, Role management.
- [ ] Unit Test cơ bản cho Service layer.

### Giai đoạn 3: Module Nghiệp vụ (Academic)
- [ ] Implement `academic`: Course, Semester, CourseSection.
- [ ] Thiết lập quan hệ database giữa các entities.

### Giai đoạn 4: Module Vận hành (Enrollment & Grading)
- [ ] Implement `enrollment`: Đăng ký học phần, Hủy học phần.
- [ ] Implement `grading`: Nhập điểm, Tính GPA.

### Giai đoạn 5: Báo cáo & Mở rộng
- [ ] Implement `report`: Xuất bảng điểm (PDF/Excel).
- [ ] Tối ưu hóa Performance (Caching).
- [ ] Mở rộng các tính năng nâng cao (Cảnh báo học vụ, Thi trực tuyến).

## 6. Checklist Chất lượng (Definition of Done)
- [ ] **Clean Code**: Tuân thủ naming convention, SOLID principles.
- [ ] **Validation**: Input được validate chặt chẽ tại API layer.
- [ ] **Error Handling**: API trả về lỗi chuẩn format `ErrorResponse`.
- [ ] **Auditing**: Dữ liệu có `created_at`, `updated_at`, `created_by`.
- [ ] **Documentation**: API có đầy đủ document trên Swagger.
