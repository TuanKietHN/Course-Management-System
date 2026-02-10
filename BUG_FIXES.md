# Tổng hợp lỗi và cách khắc phục

## 1. Lỗi: Spring Data Redis quét nhầm Repository
**Mô tả lỗi:**
Trong log khởi động có các dòng cảnh báo:
```
Spring Data Redis - Could not safely identify store assignment for repository candidate interface vn.com.nws.cms.modules.academic.infrastructure.persistence.repository.JpaCourseRepository
```
Điều này xảy ra do Spring Data Redis tự động quét tất cả các interface kế thừa `Repository` và cố gắng tạo bean cho chúng, nhưng các Repository này lại là JPA Repository.

**Nguyên nhân:**
Spring Boot tự động cấu hình cả JPA và Redis. Nếu không chỉ định rõ gói (package) nào dùng cho JPA, gói nào dùng cho Redis, thì cả hai sẽ cố gắng quét toàn bộ dự án, gây xung đột hoặc cảnh báo.

**Cách khắc phục:**
Cấu hình rõ ràng phạm vi quét (scan base packages) cho JPA Repositories và tắt tính năng tự động quét của Redis Repository (vì dự án này chỉ dùng RedisTemplate, không dùng Redis Repository).

1.  Tạo/Cập nhật `JpaConfig.java`: Thêm `@EnableJpaRepositories` trỏ đến các package chứa JPA Repository.
2.  Cập nhật `RedisConfig.java`: Thêm `@EnableRedisRepositories(enabled = false)` hoặc chỉ định package rỗng nếu không dùng Redis Repository. Tuy nhiên, đơn giản nhất là cấu hình JPA Repositories thật cụ thể để Redis không "nhận vơ".

## 2. Lỗi: Database chưa có bảng (Table not found)
**Mô tả lỗi:**
Ứng dụng chạy nhưng không có bảng nào trong Database.

**Nguyên nhân:**
Mặc định Spring Boot không tự động tạo schema từ Entity nếu không được cấu hình, hoặc cấu hình `spring.jpa.hibernate.ddl-auto` đang là `none` hoặc `validate`.

**Cách khắc phục:**
Cập nhật `application.properties`:
```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

## 3. Lỗi: Swagger 500 Failed to load API definition
**Mô tả lỗi:**
Truy cập Swagger UI bị lỗi 500 khi tải `/v3/api-docs`.

**Nguyên nhân:**
Do cấu hình CORS (Cross-Origin Resource Sharing) chặn request từ trình duyệt đến API Docs, hoặc thiếu cấu hình cho phép truy cập public vào các endpoint của Swagger.

**Cách khắc phục:**
1.  Cập nhật `WebConfig.java`: Thêm `http://localhost:8081` vào danh sách `allowedOrigins`.
2.  Cập nhật `SecurityConfig.java`: Đảm bảo `authorizeHttpRequests` cho phép truy cập `/v3/api-docs/**` và `/swagger-ui/**`.

## 4. Lỗi: Warning "Multiple Spring Data modules found"
**Mô tả lỗi:**
Log báo "Multiple Spring Data modules found, entering strict repository configuration mode".

**Nguyên nhân:**
Có cả Spring Data JPA và Spring Data Redis trong classpath.

**Cách khắc phục:**
Đây là cảnh báo bình thường. Tuy nhiên để "sạch" log và tối ưu khởi động, nên cấu hình rõ ràng `@EnableJpaRepositories` như ở mục 1.
