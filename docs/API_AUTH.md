# API Documentation: Authentication Module

Base URL: `/api/auth`

## 1. Đăng ký (Register)
*   **Endpoint**: `POST /register`
*   **Description**: Đăng ký tài khoản mới.
*   **Request Body**:
    ```json
    {
      "username": "student1",
      "email": "student1@example.com",
      "password": "password123",
      "role": "STUDENT" // Optional (Default: STUDENT). Values: ADMIN, TEACHER, STUDENT
    }
    ```
*   **Response (200 OK)**:
    ```json
    {
      "status": 200,
      "message": "User registered successfully",
      "data": null
    }
    ```

## 2. Đăng nhập (Login)
*   **Endpoint**: `POST /login`
*   **Description**: Đăng nhập lấy Token.
*   **Request Body**:
    ```json
    {
      "username": "student1",
      "password": "password123"
    }
    ```
*   **Response (200 OK)**:
    ```json
    {
      "status": 200,
      "message": "Login successful",
      "data": {
        "accessToken": "eyJhbGciOi...",
        "refreshToken": "d8a9f8...",
        "tokenType": "Bearer",
        "expiresIn": 900,
        "username": "student1",
        "role": "ROLE_STUDENT"
      }
    }
    ```

## 3. Làm mới Token (Refresh Token)
*   **Endpoint**: `POST /refresh`
*   **Description**: Cấp lại Access Token mới khi hết hạn.
*   **Request Body**:
    ```json
    {
      "refreshToken": "d8a9f8..."
    }
    ```
*   **Response (200 OK)**:
    ```json
    {
      "status": 200,
      "message": "Token refreshed",
      "data": {
        "accessToken": "new_access_token...",
        "refreshToken": "new_refresh_token...",
        "tokenType": "Bearer",
        "expiresIn": 900,
        "username": "student1",
        "role": "ROLE_STUDENT"
      }
    }
    ```

## 4. Đăng xuất (Logout)
*   **Endpoint**: `POST /logout`
*   **Description**: Hủy Refresh Token.
*   **Request Body**:
    ```json
    {
      "refreshToken": "d8a9f8..."
    }
    ```
*   **Response (200 OK)**:
    ```json
    {
      "status": 200,
      "message": "Logged out successfully",
      "data": null
    }
    ```

## 5. Quên Mật khẩu (Forgot Password)
*   **Endpoint**: `POST /forgot-password`
*   **Description**: Gửi token reset mật khẩu qua email (Hiện tại đang log ra console để test).
*   **Request Body**:
    ```json
    {
      "email": "student1@example.com"
    }
    ```
*   **Response (200 OK)**:
    ```json
    {
      "status": 200,
      "message": "Password reset instructions sent to email",
      "data": null
    }
    ```

## 6. Đặt lại Mật khẩu (Reset Password)
*   **Endpoint**: `POST /reset-password`
*   **Description**: Đặt mật khẩu mới sử dụng token nhận được.
*   **Request Body**:
    ```json
    {
      "token": "reset_token_from_email",
      "newPassword": "newPassword123"
    }
    ```
*   **Response (200 OK)**:
    ```json
    {
      "status": 200,
      "message": "Password has been reset successfully",
      "data": null
    }
    ```

## Lưu ý cho Frontend (Vue.js)
1.  **Lưu Token**: Lưu `accessToken` và `refreshToken` vào LocalStorage hoặc Cookie (HttpOnly recommended).
2.  **Interceptor**:
    *   Tự động đính kèm `Authorization: Bearer <accessToken>` cho mọi request (trừ auth).
    *   Xử lý lỗi `401 Unauthorized`:
        *   Nếu lỗi từ API bình thường -> Gọi `/refresh`.
        *   Nếu `/refresh` thành công -> Retry request cũ.
        *   Nếu `/refresh` thất bại -> Redirect về Login & Xóa token.
3.  **CORS**: Đã được cấu hình cho `localhost:3000`.
