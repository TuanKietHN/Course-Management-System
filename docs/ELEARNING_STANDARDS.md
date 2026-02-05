# Tiêu chuẩn E-learning & So sánh với REST API

## 1. Giới thiệu các Tiêu chuẩn

### 1.1. SCORM (Sharable Content Object Reference Model)
*   **Là gì**: Bộ tiêu chuẩn kỹ thuật cho các sản phẩm E-learning.
*   **Mục đích**: Đảm bảo nội dung học tập (Courseware) có thể chạy trên mọi hệ thống quản lý học tập (LMS).
*   **Cơ chế**:
    *   **Packaging**: Đóng gói nội dung thành file ZIP (IMS Manifest).
    *   **Run-time**: Giao tiếp giữa nội dung (trên trình duyệt) và LMS qua JavaScript API.
*   **Hạn chế**: Chỉ chạy trên trình duyệt, dữ liệu hạn chế (điểm, trạng thái hoàn thành), khó tracking hoạt động offline hoặc social learning.

### 1.2. xAPI (Tin Can API / Experience API)
*   **Là gì**: Thế hệ tiếp theo của SCORM.
*   **Mục đích**: Theo dõi mọi trải nghiệm học tập (Learning Experience), không chỉ là các khóa học online.
*   **Cơ chế**:
    *   Ghi lại hoạt động dưới dạng **Statement**: `Actor` (Ai) `Verb` (Làm gì) `Object` (Cái gì).
        *   Ví dụ: "Nguyen Van A" "đã xem" "Video giới thiệu".
    *   Lưu trữ tại **LRS (Learning Record Store)**.
*   **Ưu điểm**: Linh hoạt, tracking mobile app, game, thực hành thực tế, offline learning.

### 1.3. Open Badges
*   **Là gì**: Tiêu chuẩn xác thực kỹ năng/thành tích kỹ thuật số.
*   **Mục đích**: Cấp chứng chỉ (Badge) có chứa metadata (người cấp, tiêu chí, bằng chứng) có thể xác minh được.
*   **Cơ chế**: File hình ảnh (PNG/SVG) được nhúng metadata JSON.

## 2. So sánh Spring Boot REST API vs xAPI

Nếu bạn muốn xây dựng hệ thống hỗ trợ chuẩn E-learning (đặc biệt là xAPI), cách thiết kế API sẽ khác biệt so với RESTful API thông thường.

| Tiêu chí | RESTful API (Thông thường) | xAPI (Chuẩn E-learning) |
| :--- | :--- | :--- |
| **Tài nguyên (Resource)** | Định nghĩa theo nghiệp vụ (`/users`, `/courses`, `/enrollments`). | Định nghĩa theo chuẩn Statement (`/statements`, `/activities`, `/agents`). |
| **Cấu trúc Dữ liệu** | Tùy chỉnh theo nhu cầu (DTO). | Cứng nhắc theo chuẩn JSON xAPI (Actor, Verb, Object, Result, Context). |
| **Hành động (Verbs)** | HTTP Verbs (`GET`, `POST`, `PUT`, `DELETE`). | `Verb` là một phần của dữ liệu (e.g., `completed`, `passed`, `commented`). API chủ yếu dùng `POST` hoặc `PUT` để gửi statement. |
| **Lưu trữ** | Database quan hệ (Relational DB) thường dùng. | LRS (NoSQL thường phù hợp hơn vì cấu trúc JSON phức tạp và lượng dữ liệu lớn). |
| **Mục tiêu** | Quản lý trạng thái hệ thống (State Management). | Ghi nhật ký luồng hoạt động (Stream of Activities). |

### Ví dụ: Ghi nhận "Sinh viên hoàn thành bài học"

**Cách RESTful API:**
```http
PUT /api/enrollments/{id}/progress
Content-Type: application/json

{
  "status": "COMPLETED",
  "score": 85
}
```

**Cách xAPI:**
```http
POST /xapi/statements
Content-Type: application/json

{
  "actor": {
    "mbox": "mailto:student@example.com",
    "name": "Nguyen Van A"
  },
  "verb": {
    "id": "http://adlnet.gov/expapi/verbs/completed",
    "display": { "en-US": "completed" }
  },
  "object": {
    "id": "http://example.com/course/java-basic",
    "definition": { "name": { "en-US": "Java Basic Course" } }
  },
  "result": {
    "score": { "scaled": 0.85 }
  }
}
```

## 3. Khuyến nghị cho Dự án CMS này
Với yêu cầu hiện tại (Quản lý khóa học nội bộ), bạn **KHÔNG NHẤT THIẾT** phải implement đầy đủ LRS hay xAPI ngay lập tức vì nó rất phức tạp.

Tuy nhiên, để hệ thống "xAPI-ready" (dễ mở rộng sau này), bạn nên:
1.  **Thiết kế Event-Driven**: Khi User hoàn thành khóa học, bắn một Event nội bộ.
2.  **Activity Log**: Tạo bảng log lưu lại các hành động quan trọng (Ai, Làm gì, Lúc nào).
3.  **Tách biệt**: Nếu cần hỗ trợ SCORM/xAPI, nên dùng một module riêng hoặc tích hợp thư viện (như Rustici Engine hoặc các Open Source LRS) thay vì viết lại từ đầu trong Core CMS.
