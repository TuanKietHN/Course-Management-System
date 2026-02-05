# Hướng dẫn Tích hợp Keycloak

## 1. Tổng quan
Hướng dẫn này chi tiết cách thiết lập Keycloak làm Identity Provider (IdP) cho CMS.

## 2. Cấu hình Realm
1.  **Tạo Realm**:
    *   Tên: `cms-realm`
    *   Enabled: BẬT

## 3. Cấu hình Client
### 3.1 Backend Client (Bearer-only / Service Account)
*Về mặt kỹ thuật không bắt buộc nếu chúng ta chỉ validate token, nhưng hữu ích để introspection nếu dùng opaque token. Với JWT, chúng ta chỉ cần Realm public key (lấy qua JWK URI).*

### 3.2 Frontend Client (Public)
*   **Client ID**: `cms-frontend`
*   **Giao thức Client**: OpenID Connect
*   **Access Type**: Public
*   **Valid Redirect URIs**: `http://localhost:3000/*` (URL Frontend)
*   **Web Origins**: `http://localhost:3000` (CORS)

## 4. Vai trò & Nhóm (Roles & Groups)
### 4.1 Realm Roles
Tạo các role sau:
*   `ADMIN`
*   `TEACHER`
*   `STUDENT`

### 4.2 Người dùng (Users)
Tạo người dùng thử nghiệm và gán role:
*   `admin` / `password` -> Role: `ADMIN`
*   `teacher1` / `password` -> Role: `TEACHER`
*   `student1` / `password` -> Role: `STUDENT`

## 5. Cấu hình Token (Tùy chọn)
Để đảm bảo Backend có thể dễ dàng đọc role, bạn có thể map Realm Roles vào claim của token.
1.  Vào **Client Scopes** -> `roles`.
2.  Đảm bảo "Include in Token" được BẬT.
3.  Tên Mapper: `realm roles`.
4.  Tên Token Claim: `realm_access.roles`.

## 6. Chi tiết Tích hợp Backend
Cấu hình Spring Security cần trích xuất role từ `realm_access.roles`.

**Đoạn mã (JwtConverter):**
```java
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");
        if (realmAccess == null || realmAccess.isEmpty()) {
            return new ArrayList<>();
        }
        
        return ((List<String>) realmAccess.get("roles")).stream()
                .map(roleName -> "ROLE_" + roleName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
```
