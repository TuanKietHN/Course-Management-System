# Review dự án CMS (2026-02)

Mục tiêu của bộ tài liệu này:
- Rà soát hiện trạng codebase (backend + frontend)
- Đối chiếu cấu trúc database (migration) với entity/domain hiện có
- Đánh giá mức phù hợp cho hệ thống quản lý khóa học nội bộ
- Đề xuất các chức năng bổ sung và hướng mở rộng xử lý video khóa học

## Danh mục

1. [01-Database.md](01-Database.md)  
   Hiện trạng schema theo migrations V1–V8, những bảng đang được code sử dụng, và các điểm lệch giữa schema–entity.

2. [02-Domain-Entity.md](02-Domain-Entity.md)  
   Đánh giá domain/entity theo góc nhìn “quản lý khóa học nội bộ”, các lệch khái niệm và đề xuất chuẩn hoá.

3. [03-Features-Gaps.md](03-Features-Gaps.md)  
   Tổng hợp chức năng hiện có (API/role) và danh sách tính năng có thể bổ sung theo mức ưu tiên.

4. [04-Video-Extension.md](04-Video-Extension.md)  
   Thiết kế hướng mở rộng để xử lý video khóa học (upload, transcode, phát HLS, phân quyền, tracking tiến độ).

