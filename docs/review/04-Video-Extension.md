# 04. Mở rộng xử lý video khóa học (định hướng kiến trúc)

## 1) Tiền đề từ dự án hiện tại

Dự án đã có sẵn các “building blocks” phù hợp để làm video:
- MinIO client + service upload + presigned URL
- RabbitMQ (đang dùng cho email) → có thể tái dùng cho job async (transcode/thumbnails)
- Schema V4 đã có `lesson_contents.video_url` + `student_lesson_progress.progress_data` (JSONB) để tracking vị trí xem

Điểm cần chuẩn hoá trước khi làm video:
- Thống nhất `courses` là “khóa học e-learning” hay “lớp học phần” (xem 02-Domain-Entity.md)
- Thống nhất Teacher (`users` vs `teachers`)

## 2) Mục tiêu video: tối thiểu nên có gì?

### 2.1. Yêu cầu chức năng
- Teacher upload video (thường MP4) cho lesson/content
- Hệ thống tự xử lý:
  - tạo thumbnail
  - chuyển mã (transcode) sang HLS (m3u8 + ts/fmp4 segments) để phát mượt theo mạng
- Student xem video theo quyền:
  - phải enroll hoặc lesson cho phép preview
- Tracking tiến độ:
  - lưu vị trí xem, % hoàn thành, thời gian xem

### 2.2. Yêu cầu phi chức năng
- Không chặn request upload lâu (upload xong trả nhanh, xử lý async)
- Không public lộ URL/video nếu không đủ quyền
- Có trạng thái xử lý + retry + audit lỗi

## 3) Đề xuất mô hình dữ liệu cho media/video

Nếu muốn đi “chắc và sạch”, nên tách video metadata riêng thay vì chỉ dùng `lesson_contents.video_url`.

Gợi ý bảng (tối giản):
- `media_assets`
  - `id`, `type` (video/audio/file), `original_object`, `mime_type`, `size_bytes`, `checksum`, `created_by`, timestamps
- `video_assets`
  - `media_asset_id` (PK/FK), `duration_seconds`, `width`, `height`, `status` (uploaded/processing/ready/failed), `error_message`
- `video_renditions`
  - `video_asset_id`, `profile` (360p/720p/1080p), `hls_object_prefix`, `bitrate_kbps`
- `video_thumbnails`
  - `video_asset_id`, `object_name`, `time_offset_seconds`
- `media_processing_jobs`
  - `id`, `media_asset_id`, `job_type` (transcode/thumbnail), `status`, `attempts`, `payload` (JSONB), `last_error`

Gắn kết với lesson:
- `lesson_contents` thêm `media_asset_id` (nullable) để link tới video/file trong MinIO

Nếu muốn tận dụng schema V4 hiện có trước:
- Có thể giữ `lesson_contents.video_url` nhưng sẽ khó quản trị job/trạng thái và khó mở rộng nhiều rendition.

## 4) Pipeline xử lý video (MinIO + RabbitMQ)

### 4.1. Luồng upload
1. Teacher gọi API upload video cho lesson content.
2. Backend lưu file gốc vào MinIO:
   - bucket: `videos` (đề xuất tách khỏi `avatars`)
   - objectName: `courses/{courseId}/lessons/{lessonId}/{uuid}/source.mp4`
3. Backend tạo `media_asset`/`video_asset` với status `uploaded`.
4. Backend publish message lên RabbitMQ: `VIDEO_TRANSCODE_REQUESTED` chứa `videoAssetId` + objectName gốc.

### 4.2. Worker xử lý (có thể là cùng service hoặc service riêng)
1. Worker nhận message.
2. Tải file gốc (hoặc stream) từ MinIO.
3. Chạy FFmpeg:
   - tạo HLS multi-bitrate (vd 360p/720p)
   - tạo thumbnails (vd 3-5 ảnh theo mốc thời gian)
4. Upload output vào MinIO:
   - `.../{uuid}/hls/master.m3u8`
   - `.../{uuid}/hls/720p/segment_000.ts` ...
   - `.../{uuid}/thumbs/0001.jpg` ...
5. Update DB status `ready` + metadata duration/size, renditions, thumbnails.
6. Nếu lỗi: update `failed` + log `error_message`, cho phép retry theo `attempts`.

## 5) Phát video và phân quyền truy cập

### 5.1. Quyền xem
Quyền xem nên check theo:
- enrollment: student đã enroll course/offering liên quan
- preview: lesson/is_preview cho phép xem trước
- role: teacher/admin có quyền xem toàn bộ

### 5.2. Cách cấp URL
Với MinIO presigned URL có 2 lựa chọn:
1. Presign cho master playlist + segments:
   - Đơn giản nhưng phải xử lý việc presign nhiều segment (m3u8 kéo nhiều file)
2. Backend proxy:
   - Endpoint dạng `/api/v1/media/hls/{videoId}/...` kiểm tra quyền rồi stream từ MinIO
   - Tốn băng thông backend nhưng kiểm soát quyền tốt và không phải presign từng segment

Khuyến nghị cho nội bộ:
- Bắt đầu bằng backend proxy (đúng quyền, dễ triển khai)
- Khi cần scale, chuyển sang CDN/nginx + signed cookies/tokens

## 6) Tracking tiến độ xem

Tận dụng schema V4:
- `student_lesson_progress.progress_data` JSONB lưu:
  - `video_asset_id`
  - `position_seconds`
  - `watched_percentage`
  - `last_ping_at`

API đề xuất:
- `POST /api/v1/lessons/{lessonId}/progress` (student) cập nhật position/percentage
- `GET /api/v1/courses/{courseId}/progress` (student/teacher) xem tổng hợp (có sẵn view `v_student_course_progress`)

## 7) Các nâng cấp “nên có” khi production

- Giới hạn kích thước/định dạng upload (content-type + sniffing)
- Antivirus scan hoặc sandbox (nếu upload file bên thứ 3)
- Watermark động (nếu cần chống leak)
- Caption/subtitles (SRT/VTT), transcript (ASR)
- Lifecycle policy cho MinIO (dọn video draft hoặc bản gốc nếu không cần)

