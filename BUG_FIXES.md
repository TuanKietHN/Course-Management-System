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

## 5. Vấn đề: Gửi Email đồng bộ gây chậm thao tác người dùng
**Mô tả:**
Trước đây (hoặc tiềm ẩn), việc gửi email xác nhận đăng ký hoặc quên mật khẩu được thực hiện trực tiếp trong luồng xử lý API (synchronous). Điều này làm tăng độ trễ phản hồi cho người dùng và có nguy cơ lỗi timeout nếu SMTP server phản hồi chậm.

**Cách khắc phục:**
Đã chuyển sang cơ chế xử lý bất đồng bộ (Asynchronous) sử dụng **RabbitMQ**.
1.  API chỉ đẩy message vào Queue và trả về phản hồi ngay lập tức cho người dùng.
2.  `EmailConsumer` sẽ đọc message từ Queue và thực hiện gửi email trong nền.
3.  Cấu hình tại `RabbitMqConfig`, `EmailProducer`, và `EmailConsumer`.

## 6. Vấn đề: Lưu trữ file ảnh (Avatar)
**Mô tả:**
Chưa có giải pháp lưu trữ file tập trung, ảnh profile.

**Cách khắc phục:**
Đã tích hợp **MinIO** (S3 Compatible Storage).
1.  Thêm `MinioConfig` và `StorageService`.
2.  Refactor `UserService` để hỗ trợ upload avatar lên MinIO bucket `avatars`.
