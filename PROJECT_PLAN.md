# Kế hoạch Triển khai Dự án Course Management System (CMS)

## 1. Tổng quan Dự án
*   **Tên dự án**: Course Management System (CMS)
*   **Mục tiêu**: Xây dựng hệ thống quản lý khóa học nội bộ hiện đại, hiệu năng cao.
*   **Kiến trúc**: Modular Monolith (Clean Architecture).
*   **Môi trường**: Java 21, Spring Boot 4.0.2 (hoặc 3.3.x stable).

## 2. Công nghệ & Công cụ (Tech Stack)

### Core Framework
*   **Java Development Kit (JDK)**: 21
*   **Spring Boot**: 4.0.2
*   **Build Tool**: Maven

### Security & Authentication
*   **Cơ chế**: Custom JWT Authentication (Stateful/Stateless Hybrid).
*   **Token**:
    *   **Access Token**: JWT (ngắn hạn, 15-30 phút).
    *   **Refresh Token**: UUID hoặc Random String (dài hạn, 7-30 ngày), lưu trong **Redis**.
*   **Thư viện**: JJWT (Java JWT).
*   **Caching**: **Redis** (Lưu Refresh Token, Blacklist Token, Cache dữ liệu).

### Data & Mapping
*   **Database**: PostgreSQL.
*   **Migration**: Flyway.
*   **ORM**: JPA / Hibernate.
*   **Mapper**: MapStruct.
*   **Boilerplate**: Lombok.

### Dependencies
| Group | Artifact | Mục đích |
|-------|----------|----------|
| `org.springframework.boot` | `spring-boot-starter-webmvc` | REST API |
| `org.springframework.boot` | `spring-boot-starter-data-jpa` | ORM |
| `org.springframework.boot` | `spring-boot-starter-security` | Authentication/Authorization |
| `org.springframework.boot` | `spring-boot-starter-data-redis` | Redis Client (Jedis/Lettuce) |
| `io.jsonwebtoken` | `jjwt-api` | JWT generation/parsing |
| `org.mapstruct` | `mapstruct` | Object Mapping |
| `org.projectlombok` | `lombok` | Giảm code thừa |
| `org.springdoc` | `springdoc-openapi-starter-webmvc-ui` | API Docs (Swagger) |

## 3. Kiến trúc Hệ thống (Clean Code Structure)

### Cấu trúc Layer
1.  **API Layer (`api`)**: Controllers, DTOs.
2.  **Application Layer (`application`)**: Services, UseCases.
3.  **Domain Layer (`domain`)**: Entities, Repositories (Interfaces).
4.  **Infrastructure Layer (`infrastructure`)**:
    *   **Persistence**: JPA Repositories.
    *   **Security**: JWT Filter, UserDetailsService, RedisTokenStore.

### Mô hình Bảo mật (JWT + Redis)
1.  **Login**:
    *   User gửi username/password.
    *   Server xác thực -> Tạo Access Token (JWT) & Refresh Token.
    *   Lưu Refresh Token vào Redis (Key: `rt:{username}`, Value: `{token}`, TTL: 7 days).
    *   Trả về cả 2 token cho Client.
2.  **Request**:
    *   Client gửi Access Token trong Header.
    *   Server (JwtFilter) validate signature & expiry.
3.  **Refresh**:
    *   Access Token hết hạn -> Client gọi API Refresh kèm Refresh Token.
    *   Server check Refresh Token trong Redis. Nếu khớp -> Tạo cặp token mới -> Cập nhật Redis.
4.  **Logout**:
    *   Xóa Refresh Token khỏi Redis.
    *   (Optional) Thêm Access Token hiện tại vào Blacklist (Redis) cho đến khi hết hạn.

## 4. Cấu trúc Thư mục Chi tiết

```
vn.com.nws.cms
│
├── CmsApplication.java
│
├── common (Shared Kernel)
│   ├── config
│   │   ├── SecurityConfig.java
│   │   ├── RedisConfig.java (Cấu hình RedisTemplate)
│   │   └── AppConfig.java
│   ├── security
│   │   ├── JwtProvider.java (Generate/Validate Token)
│   │   ├── JwtAuthenticationFilter.java
│   │   └── CustomUserDetailsService.java
│   ├── exception
│   └── dto
│
└── modules
    ├── auth (Quản lý đăng nhập/đăng ký)
    │   ├── api
    │   │   ├── AuthController.java (Login, Refresh, Logout)
    │   │   └── dto
    │   │       ├── LoginRequest.java
    │   │       └── TokenResponse.java
    │   ├── application
    │   │   └── AuthService.java
    │   └── domain
    │       └── model
    │           └── User.java (Entity User/Role)
    │
    ├── academic
    ├── enrollment
    ├── grading
    └── report
```

## 5. Lộ trình Thực hiện (Roadmap)

### Giai đoạn 1: Foundation & Security
- [ ] Setup Project & Dependencies (Redis, JWT).
- [ ] Cấu hình Docker Compose (PostgreSQL, Redis).
- [ ] Implement `auth` module: Login, Logout, Refresh Token với Redis.
- [ ] Implement Security Filter Chain.

### Giai đoạn 2: Modules Core
- [ ] Implement Academic Module.
- [ ] Tích hợp Redis Cache cho các dữ liệu ít thay đổi (Ví dụ: Danh sách môn học).

### Giai đoạn 3: Business Logic
- [ ] Enrollment & Grading.
- [ ] Reporting.
