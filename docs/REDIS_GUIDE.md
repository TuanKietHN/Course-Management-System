# Hướng dẫn Sử dụng Redis & Caching

## 1. Giới thiệu Redis trong CMS
Trong dự án này, Redis được sử dụng cho 2 mục đích chính:
1.  **Token Store**: Lưu trữ Refresh Token và Blacklist Access Token (để xử lý đăng xuất).
2.  **Application Cache**: Cache các dữ liệu ít thay đổi để tăng tốc độ truy xuất.

## 2. Cài đặt Redis

### 2.1 Sử dụng Docker (Khuyến nghị)
Redis đã được cấu hình trong `docker-compose.yml` của dự án.
```yaml
  redis:
    image: redis:7-alpine
    container_name: cms_redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    command: redis-server --appendonly yes --requirepass cms_redis_password
```
Để khởi chạy:
```bash
docker-compose up -d redis
```

### 2.2 Cài đặt thủ công (Windows)
Nếu không dùng Docker, bạn có thể cài Redis trên Windows:
1.  Tải Memurai (Redis for Windows) hoặc chạy qua WSL2.
2.  Start server: `redis-server`

## 3. Cấu trúc Key trong Redis
Chúng ta tuân thủ quy tắc đặt tên key: `app:module:key`

### 3.1 Authentication Keys
*   **Refresh Token**:
    *   Key: `auth:rt:{username}`
    *   Value: Chuỗi Refresh Token (UUID)
    *   TTL: 7 ngày (604800s)
*   **Blacklist Token** (Token bị hủy khi Logout):
    *   Key: `auth:bl:{accessToken}`
    *   Value: `true`
    *   TTL: Thời gian còn lại của Access Token (ví dụ: 15 phút)

### 3.2 Cache Keys (Ví dụ)
*   **Danh mục Môn học**:
    *   Key: `cms:courses:all`
    *   TTL: 1 giờ
*   **Cấu hình Hệ thống**:
    *   Key: `cms:config:{key}`
    *   TTL: 24 giờ

## 4. Hướng dẫn Debug Redis
Sử dụng **Redis CLI** để kiểm tra dữ liệu.

1.  **Kết nối**:
    ```bash
    docker exec -it cms_redis redis-cli -a cms_redis_password
    ```

2.  **Lệnh cơ bản**:
    *   Kiểm tra Refresh Token của user `admin`:
        ```redis
        GET auth:rt:admin
        ```
    *   Xem thời gian sống còn lại (TTL):
        ```redis
        TTL auth:rt:admin
        ```
    *   Xóa toàn bộ cache (Cẩn thận):
        ```redis
        FLUSHALL
        ```
    *   Xem tất cả keys (Chỉ dùng khi dev):
        ```redis
        KEYS *
        ```

## 5. Tích hợp trong Spring Boot

### 5.1 Dependency
Đã thêm vào `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

### 5.2 Sử dụng `RedisTemplate`
```java
@Autowired
private RedisTemplate<String, Object> redisTemplate;

// Lưu Token
redisTemplate.opsForValue().set("auth:rt:admin", token, 7, TimeUnit.DAYS);

// Lấy Token
String token = (String) redisTemplate.opsForValue().get("auth:rt:admin");

// Xóa Token
redisTemplate.delete("auth:rt:admin");
```
