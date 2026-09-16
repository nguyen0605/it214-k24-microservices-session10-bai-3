# BÁO CÁO BÀI TẬP 3: CHUYỂN ĐỔI RESTTEMPLATE SANG WEBCLIENT TRONG SPRING WEBFLUX

## PHẦN 1: PHÂN TÍCH LỖI VÀ CƠ CHẾ HOẠT ĐỘNG

### 1. Nguyên nhân gây lỗi HTTP 500 Internal Server Error và Cạn kiệt Thread

- **Mô hình Thread trong Spring WebFlux**:
  WebFlux hoạt động trên nền tảng Event Loop (thường sử dụng Reactor Netty) với số lượng Thread cực kỳ hạn chế (thường bằng số lõi CPU, ví dụ: 8 - 16 threads). Mô hình này dựa trên nguyên lý **Non-blocking I/O**, trong đó các thread đảm nhận nhiệm vụ xử lý tín hiệu/sự kiện mà không bao giờ bị dừng (block).

- **Cơ chế hoạt động của RestTemplate**:
  `RestTemplate` thuộc hệ sinh thái Spring MVC cũ, hoạt động theo cơ chế **Synchronous & Blocking I/O** (mỗi request chiếm 1 thread). Khi gọi `restTemplate.getForObject(...)`, thread hiện tại sẽ bị tạm dừng (sleep/block) hoàn toàn để chờ phản hồi từ `promotion-service` qua kết nối mạng.

- **Hậu quả khi kết hợp RestTemplate vào WebFlux dưới tải cao (10.000 concurrent users)**:
  1. Khi 10.000 truy cập gửi tới đồng thời, vài Event Loop Thread ít ỏi của WebFlux tiếp nhận request và gọi `restTemplate.getForObject(...)`.
  2. Tất cả Event Loop Thread lập tức bị khóa cứng (blocked) để chờ kết quả mạng từ dịch vụ nội bộ.
  3. Vì tất cả Event Loop Thread đều bị kẹt, hệ thống không còn thread nào để tiếp nhận hay xử lý các request mới đến hoặc xử lý I/O phản hồi.
  4. Xảy ra hiện tượng **Thread Starvation** (Cạn kiệt Thread). Hệ thống bị treo cứng, các kết nối bị timeout hàng loạt và trả về lỗi **HTTP 500 Internal Server Error**.

### 2. Vấn đề xử lý ngoại lệ và khả năng chịu lỗi (Resilience)
- Code cũ tự ném ra `throw new RuntimeException("Promotion service unavailable")` khi `banner == null` hoặc dịch vụ bị chết.
- Trong kiến trúc Microservices, việc dịch vụ phụ trợ chết hoặc chậm không được làm sập dịch vụ chính. Thiếu timeout và fallback khiến lỗi lan truyền (cascading failure), làm sập trang chủ StoreX.

---

## PHẦN 2: GIẢI PHÁP VÀ MÃ NGUỒN TỐI ƯU

### Các cải tiến đã áp dụng:
1. **Chuyển sang WebClient**: Dùng `WebClient` hỗ trợ Reactive, Non-blocking I/O hoàn toàn.
2. **Trả về Mono<Banner>**: Đảm bảo luồng dữ liệu tuân theo chuẩn Reactive Stream.
3. **Cấu hình Timeout 2 giây**: Sử dụng toán tử `.timeout(Duration.ofSeconds(2))`.
4. **Cơ chế Fallback an toàn**: Sử dụng `.onErrorResume(...)` để trả về Banner mặc định với thông báo `"Khuyến mãi đang được cập nhật"` khi dịch vụ phản hồi chậm hoặc bị lỗi.