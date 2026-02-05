# Kế hoạch Triển khai Dự án Course Management System (CMS)

## 1. Tổng quan Dự án
*   **Tên dự án**: Course Management System (CMS)
*   **Mục tiêu**: Xây dựng hệ thống quản lý khóa học nội bộ hiện đại, hỗ trợ chuẩn E-learning.
*   **Kiến trúc**: Modular Monolith (Clean Architecture).
*   **Môi trường**: Java 21, Spring Boot 4.0.2.

## 2. Công nghệ & Công cụ (Tech Stack)

### Backend (Spring Boot)
*   **Core**: Java 21, Spring Boot 4.0.2.
*   **Database**: PostgreSQL.
*   **Auth**: OAuth2 Resource Server (JWT) + Database (Refresh Token) - **Đã loại bỏ Redis**.
*   **API Docs**: Swagger/OpenAPI.
*   **Mapping**: MapStruct.

### Frontend (Vue.js)
*   **Framework**: Vue 3 (Composition API).
*   **State Management**: Pinia.
*   **Build Tool**: Vite.
*   **Router**: Vue Router.
*   **HTTP Client**: Axios.
*   **Quality**: ESLint, Prettier.
*   **DevTools**: Vite Plugin Vue Devtools.
*   **Language**: TypeScript.

## 3. Cấu trúc Dự án & Clean Architecture

### Nguyên tắc Thiết kế
*   **Dependency Rule**: Domain Layer không phụ thuộc vào bất kỳ layer nào khác.
*   **Database Agnostic**: Core Logic không phụ thuộc vào SQL/NoSQL. Sử dụng Repository Interface để trừu tượng hóa.
*   **UI Agnostic**: Backend chỉ trả về JSON, không phụ thuộc Frontend (Vue/React).

### Cấu trúc Layer chi tiết
1.  **Domain Layer** (`domain`):
    *   **Entities**: POJO thuần túy (hạn chế JPA annotation nếu cần tách biệt triệt để).
    *   **Repository Interfaces**: Contract (e.g., `UserRepository`).
2.  **Application Layer** (`application`):
    *   **Services**: Implement Use Cases.
    *   **DTOs**: Input/Output cho API.
3.  **Infrastructure Layer** (`infrastructure`):
    *   **Persistence**: Implement Repository Interface (JPA/Hibernate).
    *   **External Services**: Email, Storage impl.
4.  **API Layer** (`api`):
    *   **Controllers**: REST endpoints.

### Cấu trúc Thư mục Monorepo

```
cms-root/
├── pom.xml (Root Build Config)
├── src/ (Spring Boot Backend)
│   ├── main/java/vn/com/nws/cms/...
│   │   ├── common/
│   │   └── modules/
│   │       ├── auth/
│   │       ├── user/
│   │       ├── academic/
│   │       ├── storage/ (New)
│   │       └── notification/ (New)
│   └── main/resources/
│
└── frontend/ (Vue.js Project)
    ├── package.json
    ├── vite.config.ts
    ├── src/
    │   ├── api/ (Axios services)
    │   ├── assets/
    │   ├── components/
    │   ├── layouts/
    │   │   ├── AdminLayout.vue (Sidebar, Header for Admin)
    │   │   └── UserLayout.vue (TopNav for Students)
    │   ├── router/
    │   ├── stores/ (Pinia)
    │   └── views/
    └── public/
```

### Các Module Bổ sung (Để hoàn thiện Clean Arch)
1.  **Storage Module**: Xử lý file upload (Interface: `StorageService`). Implement: Local/S3.
2.  **Notification Module**: Xử lý gửi mail/thông báo (Interface: `NotificationSender`). Implement: JavaMail.
3.  **Audit Module**: Ghi log hệ thống độc lập.

### Quy trình Build & Run
1.  **Development**:
    *   Spring Boot: Run port `8080`.
    *   Vue.js: Run `npm run dev` port `5173`.
    *   **Proxy**: Vite config proxy `/api` -> `localhost:8080`.
2.  **Production**:
    *   Maven Plugin (`frontend-maven-plugin`) sẽ:
        1.  Chạy `npm install`.
        2.  Chạy `npm run build`.
        3.  Copy folder `dist` vào `src/main/resources/static`.
    *   Kết quả: 1 file JAR duy nhất chứa cả Backend và Frontend.

## 4. Coding Conventions & Rules

### Null Safety
*   **Entity & DTO**: Bắt buộc sử dụng Annotation Validation (`@NotNull`, `@NotBlank`) hoặc `Optional`.
*   **Service**: Kiểm tra null đầu vào, trả về Exception cụ thể nếu dữ liệu thiếu.

### Frontend Standards
*   **Internationalization (i18n)**: Cấu trúc code hỗ trợ đa ngôn ngữ ngay từ đầu (`vue-i18n`).
*   **Component**: Chia nhỏ component, tái sử dụng (Button, Input, Table).

## 5. Lộ trình Thực hiện (Roadmap)

### Giai đoạn 1: Foundation (Backend) - Đã hoàn thành
- [x] Setup Spring Boot, Security (OAuth2), Database.
- [x] Implement Auth Module (Login, Register) với Refresh Token lưu DB.

### Giai đoạn 2: Frontend Setup (Vue.js)
- [ ] Khởi tạo dự án Vue 3 + Vite + TS trong thư mục `frontend`.
- [ ] Cấu hình Axios & Interceptor (Auto attach Token).
- [ ] Setup Layouts (Admin/User) & Router.
- [ ] Implement trang Login/Register kết nối API.

### Giai đoạn 3: Core Features (Fullstack)
- [ ] **User Management**: API + UI (Profile, Admin Dashboard).
- [ ] **Academic**: API + UI (Quản lý Học kỳ, Môn học).

### Giai đoạn 4: E-learning Features
- [ ] Tham khảo chuẩn xAPI/SCORM cho cấu trúc dữ liệu khóa học.
- [ ] Implement chức năng học tập (Bài giảng, Video, Quiz).
