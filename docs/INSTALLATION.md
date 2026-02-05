# Hướng dẫn Cài đặt & Triển khai

## Yêu cầu Tiên quyết
*   **Java**: JDK 21
*   **Maven**: 3.8+
*   **Docker & Docker Compose**: Phiên bản mới nhất

## 1. Thiết lập Môi trường

### 1.1 Docker Compose
Sử dụng Docker để chạy PostgreSQL và Redis.

```yaml
# docker-compose.yml
version: '3.8'
services:
  # Database Service
  postgres:
    image: postgres:15-alpine
    container_name: cms_postgres
    environment:
      POSTGRES_DB: cms_db
      POSTGRES_USER: cms_user
      POSTGRES_PASSWORD: cms_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - cms-network

  # Redis Service (Caching & Token Store)
  redis:
    image: redis:7-alpine
    container_name: cms_redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    command: redis-server --appendonly yes --requirepass cms_redis_password
    networks:
      - cms-network

volumes:
  postgres_data:
  redis_data:

networks:
  cms-network:
    driver: bridge
```

## 2. Thiết lập Ứng dụng

### 2.1 Clone Repository
```bash
git clone https://github.com/vn-nws-cms/cms.git
cd cms
```

### 2.2 Cấu hình `application.properties`
Cập nhật file `src/main/resources/application.properties`:

```properties
# --- Database Configuration ---
spring.datasource.url=jdbc:postgresql://localhost:5432/cms_db
spring.datasource.username=cms_user
spring.datasource.password=cms_password
spring.jpa.hibernate.ddl-auto=validate

# --- Redis Configuration ---
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=cms_redis_password
spring.data.redis.timeout=2000

# --- JWT Configuration ---
# Secret Key (Base64 Encoded - ít nhất 256 bit)
jwt.secret=5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437
# Thời hạn Access Token (ms) - 15 phút
jwt.expiration=900000
# Thời hạn Refresh Token (ms) - 7 ngày
jwt.refresh-expiration=604800000
```

### 2.3 Build & Run
```bash
mvn clean install
mvn spring-boot:run
```

## 3. Migration Cơ sở dữ liệu
Flyway được bật mặc định. Khi ứng dụng khởi động, nó sẽ tự động migrate schema từ `src/main/resources/db/migration`.

## 4. Kiểm tra Cài đặt
1.  **Swagger UI**: Truy cập `http://localhost:8088/swagger-ui.html` (Port phụ thuộc cấu hình).
2.  **Redis Test**:
    *   Sử dụng Redis CLI hoặc GUI (như RedisInsight) kết nối tới `localhost:6379`.
    *   Auth bằng password `cms_redis_password`.
    *   Thử lệnh `PING` -> Mong đợi `PONG`.

## 5. Khắc phục Sự cố
*   **Connection Refused**: Kiểm tra xem Docker container `cms_postgres` và `cms_redis` có đang chạy không.
*   **Redis Auth Failed**: Kiểm tra lại mật khẩu trong `application.properties` và `docker-compose.yml`.
