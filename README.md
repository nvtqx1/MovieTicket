<p align="center">
  <h1 align="center">🎬 TicketRush — Hệ Thống Đặt Vé Xem Phim Trực Tuyến</h1>
  <p align="center">
    <em>Online Movie Ticket Booking System</em>
  </p>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen?style=for-the-badge&logo=spring-boot" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/React-19.2.4-blue?style=for-the-badge&logo=react" alt="React"/>
  <img src="https://img.shields.io/badge/MySQL-8.0-orange?style=for-the-badge&logo=mysql" alt="MySQL"/>
  <img src="https://img.shields.io/badge/Redis-7.2-red?style=for-the-badge&logo=redis" alt="Redis"/>
  <img src="https://img.shields.io/badge/Kafka-7.7.0-black?style=for-the-badge&logo=apachekafka" alt="Kafka"/>
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker" alt="Docker"/>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk" alt="Java 17"/>
  <img src="https://img.shields.io/badge/Vite-8.0.4-646CFF?style=flat-square&logo=vite" alt="Vite"/>
  <img src="https://img.shields.io/badge/TailwindCSS-4.2.2-06B6D4?style=flat-square&logo=tailwindcss" alt="Tailwind"/>
  <img src="https://img.shields.io/badge/License-MIT-green?style=flat-square" alt="License"/>
</p>

---

## 📖 Mục Lục

- [Giới Thiệu](#-giới-thiệu)
- [Tính Năng Chính](#-tính-năng-chính)
- [Kiến Trúc Hệ Thống](#-kiến-trúc-hệ-thống)
- [Công Nghệ Sử Dụng](#-công-nghệ-sử-dụng)
- [Cấu Trúc Dự Án](#-cấu-trúc-dự-án)
- [Thiết Kế Cơ Sở Dữ Liệu](#-thiết-kế-cơ-sở-dữ-liệu)
- [API Endpoints](#-api-endpoints)
- [Yêu Cầu Hệ Thống](#-yêu-cầu-hệ-thống)
- [Hướng Dẫn Cài Đặt](#-hướng-dẫn-cài-đặt)
- [Hướng Dẫn Sử Dụng](#-hướng-dẫn-sử-dụng)
- [Screenshots](#-screenshots)
- [Kiểm Thử](#-kiểm-thử)
- [Đóng Góp](#-đóng-góp)
- [Giấy Phép](#-giấy-phép)
- [Thông Tin Liên Hệ](#-thông-tin-liên-hệ)

---

## 📌 Giới Thiệu

**TicketRush** là hệ thống đặt vé xem phim trực tuyến full-stack, được phát triển như một đề tài khóa luận tốt nghiệp. Hệ thống cho phép người dùng duyệt phim, chọn suất chiếu, đặt ghế theo thời gian thực và thanh toán trực tuyến. Hệ thống cũng cung cấp bảng điều khiển quản trị (Admin Dashboard) với các công cụ phân tích doanh thu, quản lý phim, rạp, suất chiếu và người dùng.

### 🎯 Mục Tiêu Đề Tài

- Xây dựng hệ thống đặt vé phim trực tuyến hoàn chỉnh với kiến trúc **Client-Server**.
- Áp dụng các kỹ thuật **xử lý đồng thời** (Pessimistic Locking) để ngăn chặn **race condition** khi đặt ghế.
- Tích hợp **WebSocket** để cập nhật trạng thái ghế theo thời gian thực.
- Sử dụng **Apache Kafka** cho hệ thống hàng đợi ảo (Virtual Queue) nhằm xử lý lượng truy cập lớn.
- Triển khai kiến trúc **microservice-ready** với Docker containerization.

### 📋 Thông Tin Khóa Luận

| Thông Tin | Chi Tiết |
|-----------|----------|
| **Đề tài** | Xây dựng hệ thống đặt vé xem phim trực tuyến |
| **Loại** | Khóa luận tốt nghiệp |
| **Ngôn ngữ** | Java, JavaScript |
| **Mô hình** | Full-stack Web Application |
| **Trạng thái** | ✅ Hoàn thành |

---

## ✨ Tính Năng Chính

### 👤 Phía Người Dùng (User)

| Tính Năng | Mô Tả |
|-----------|--------|
| 🔐 **Đăng ký / Đăng nhập** | Xác thực JWT, quản lý phiên đăng nhập |
| 🎬 **Duyệt phim** | Tìm kiếm, lọc theo thể loại, phân trang |
| 🏢 **Xem rạp chiếu** | Danh sách rạp, suất chiếu theo phim |
| 💺 **Đặt ghế thời gian thực** | Chọn ghế với cập nhật trạng thái live qua WebSocket |
| 🎫 **Đặt vé & Thanh toán** | Quy trình đặt vé hoàn chỉnh với thanh toán |
| 📱 **Mã QR vé điện tử** | Tạo vé điện tử với mã QR |
| 🏷️ **Mã giảm giá / Voucher** | Nhập và xác thực mã giảm giá real-time |
| ⭐ **Đánh giá phim** | Viết review, cho điểm, vote |
| 👤 **Hồ sơ cá nhân** | Quản lý thông tin, lịch sử đặt vé |

### 🛡️ Phía Quản Trị (Admin)

| Tính Năng | Mô Tả |
|-----------|--------|
| 📊 **Dashboard phân tích** | Thống kê doanh thu, biểu đồ tương tác |
| 🎬 **Quản lý phim** | CRUD phim, thể loại, trạng thái |
| 🏢 **Quản lý rạp & phòng** | Cấu hình rạp, phòng chiếu, sơ đồ ghế |
| 🕐 **Quản lý suất chiếu** | Lập lịch chiếu, phân công phòng |
| 👥 **Quản lý người dùng** | Xem, chặn, quản lý tài khoản |
| 🏷️ **Quản lý voucher** | Tạo và quản lý mã giảm giá, flash sale |
| 💰 **Phân tích doanh thu** | Doanh thu theo phim, rạp, thời gian |

### 🚀 Tính Năng Nâng Cao

- **⚡ Hàng đợi ảo (Virtual Queue)** — Sử dụng Kafka xử lý hàng nghìn request đồng thời
- **🔒 Pessimistic Locking** — Ngăn chặn đặt trùng ghế (double-booking) với `SELECT...FOR UPDATE`
- **📡 WebSocket Real-time** — Cập nhật trạng thái ghế tức thì cho tất cả người dùng
- **⏱️ Seat Hold** — Giữ ghế 10 phút với countdown timer tự động giải phóng
- **🗄️ Redis Caching** — Tăng hiệu suất truy vấn với cache layer

---

## 🏗 Kiến Trúc Hệ Thống

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                             │
│  ┌──────────────────┐          ┌──────────────────────────┐     │
│  │   User App       │          │    Admin App             │     │
│  │   (React + Vite) │          │    (React + Vite)        │     │
│  │   Port: 5173     │          │    Port: 5174            │     │
│  └────────┬─────────┘          └─────────┬────────────────┘     │
│           │           REST API / WebSocket│                      │
└───────────┼──────────────────────────────┼──────────────────────┘
            │                              │
┌───────────┴──────────────────────────────┴──────────────────────┐
│                      SERVER LAYER                                │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              Spring Boot Application                      │   │
│  │              (Port: 8080)                                 │   │
│  │  ┌────────────┐ ┌──────────┐ ┌─────────────────────┐     │   │
│  │  │ Controllers│ │ Services │ │    Repositories      │     │   │
│  │  │  (22 APIs) │→│(24 logic)│→│   (18 JPA repos)     │     │   │
│  │  └────────────┘ └──────────┘ └─────────────────────┘     │   │
│  │  ┌────────────┐ ┌──────────┐ ┌─────────────────────┐     │   │
│  │  │  Security  │ │WebSocket │ │   Kafka Workers      │     │   │
│  │  │  (JWT)     │ │ (STOMP)  │ │   (Queue Processing) │     │   │
│  │  └────────────┘ └──────────┘ └─────────────────────┘     │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────┬───────────────────────────────────────┘
                          │
┌─────────────────────────┴───────────────────────────────────────┐
│                    DATA & MESSAGING LAYER                         │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐   ┌───────────┐  │
│  │  MySQL   │    │  Redis   │    │  Kafka   │   │ Zookeeper │  │
│  │  8.0.45  │    │  7.2     │    │  7.7.0   │   │           │  │
│  │  :3306   │    │  :6379   │    │  :9092   │   │  :2181    │  │
│  └──────────┘    └──────────┘    └──────────┘   └───────────┘  │
│                    Docker Compose                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Mô Hình Kiến Trúc

- **Layered Architecture (N-Tier)**: Controller → Service → Repository
- **Design Patterns**: Repository, DTO, Singleton, Strategy, Dependency Injection
- **Nguyên tắc SOLID**: Áp dụng xuyên suốt dự án
- **Event-Driven**: Kafka cho xử lý bất đồng bộ
- **Real-time Communication**: WebSocket (SockJS + STOMP)

---

## 🛠 Công Nghệ Sử Dụng

### Backend

| Công Nghệ | Phiên Bản | Mục Đích |
|------------|-----------|----------|
| **Spring Boot** | 4.0.5 | Framework chính |
| **Java** | 17 | Ngôn ngữ lập trình |
| **Spring Security** | — | Xác thực & phân quyền |
| **Spring Data JPA** | — | ORM & truy vấn CSDL |
| **Spring WebSocket** | — | Giao tiếp thời gian thực |
| **Spring Kafka** | — | Message queue |
| **MySQL** | 8.0.45 | Cơ sở dữ liệu chính |
| **Redis** | 7.2 | Caching & session |
| **Apache Kafka** | 7.7.0 | Hàng đợi tin nhắn |
| **Flyway** | — | Database migration |
| **JWT (JJWT)** | 0.11.5 | Token xác thực |
| **Lombok** | 1.18.30 | Giảm boilerplate code |
| **ZXing** | 3.5.3 | Tạo mã QR |
| **Springdoc OpenAPI** | 3.0.3 | Tài liệu API (Swagger) |

### Frontend

| Công Nghệ | Phiên Bản | Mục Đích |
|------------|-----------|----------|
| **React** | 19.2.4 | UI Library |
| **Vite** | 8.0.4 | Build tool |
| **React Router** | 7.14.1 | Routing SPA |
| **Tailwind CSS** | 4.2.2 | Styling framework |
| **Recharts** | 3.8.1 | Biểu đồ & đồ thị |
| **SockJS + STOMP** | 1.6.1 / 7.3.0 | WebSocket client |
| **Lucide React** | 1.8.0 | Icon library |
| **Axios** | — | HTTP client |

### DevOps & Công Cụ

| Công Nghệ | Mục Đích |
|------------|----------|
| **Docker & Docker Compose** | Container hóa infrastructure |
| **Maven** | Build & quản lý dependency (Backend) |
| **npm** | Package manager (Frontend) |
| **Postman** | Kiểm thử API |
| **Swagger UI** | Tài liệu API tương tác |
| **ESLint** | Linting code JavaScript |
| **Git** | Version control |

---

## 📁 Cấu Trúc Dự Án

```
MovieTicket/
│
├── Backend/                          # Spring Boot Application
│   ├── src/
│   │   └── main/
│   │       ├── java/com/ticketrush/backend/
│   │       │   ├── config/           # Cấu hình (CORS, Redis, Kafka, WebSocket)
│   │       │   ├── controller/       # REST Controllers (22 controllers)
│   │       │   ├── dto/              # Data Transfer Objects (35+ DTOs)
│   │       │   ├── entity/           # JPA Entities (17 entities)
│   │       │   ├── exception/        # Global Exception Handler
│   │       │   ├── repository/       # Spring Data JPA Repositories (18 repos)
│   │       │   ├── scheduler/        # Scheduled Tasks
│   │       │   ├── security/         # JWT Filter, Auth Config
│   │       │   ├── service/          # Business Logic (24 services)
│   │       │   ├── util/             # Utility Classes
│   │       │   └── worker/           # Kafka Workers
│   │       └── resources/
│   │           ├── application.properties
│   │           └── db/migration/     # Flyway migration scripts
│   ├── docker-compose.yml            # MySQL, Redis, Kafka, Zookeeper
│   ├── Dockerfile                    # Backend containerization
│   └── pom.xml                       # Maven dependencies
│
├── Frontend/                         # React Application
│   ├── src/
│   │   ├── components/               # Reusable UI Components (50+)
│   │   ├── pages/                    # Page Components (19 pages)
│   │   ├── services/                 # API Service Layer (11 services)
│   │   ├── hooks/                    # Custom React Hooks
│   │   ├── context/                  # Authentication Context
│   │   ├── utils/                    # Utility Functions
│   │   ├── assets/                   # Static Assets
│   │   ├── App.jsx                   # Main App (combined)
│   │   ├── AppAdmin.jsx              # Admin App Entry
│   │   ├── AppUser.jsx               # User App Entry
│   │   └── main.jsx                  # Application Bootstrap
│   ├── vite.config.js                # Vite config (development)
│   ├── vite.admin.config.js          # Admin build config
│   ├── vite.user.config.js           # User build config
│   ├── vercel.json                   # Vercel deployment config
│   └── package.json                  # npm dependencies
│
├── MovieTicket_postman_collection.json  # Postman API Collection
└── README.md                         # Tài liệu dự án
```

---

## 🗄 Thiết Kế Cơ Sở Dữ Liệu

### Sơ Đồ Quan Hệ (ER Diagram)

```
┌──────────┐     ┌──────────┐     ┌──────────────┐
│  users   │────<│  roles   │     │   movies     │
│──────────│     └──────────┘     │──────────────│
│ id       │                      │ id           │
│ email    │                      │ title        │
│ password │                      │ genre        │
│ fullName │                      │ duration     │
│ phone    │                      │ releaseDate  │
│ gender   │                      │ poster       │
│ birthDate│                      │ description  │
└────┬─────┘                      └──────┬───────┘
     │                                   │
     │  ┌──────────────┐                 │
     └─>│ reservations │<────────────────┘
        │──────────────│     ┌──────────────┐
        │ id           │────>│  showtimes   │
        │ status       │     │──────────────│     ┌──────────┐
        │ totalPrice   │     │ id           │────>│  rooms   │
        │ createdAt    │     │ startTime    │     │──────────│
        └──────┬───────┘     │ endTime      │     │ id       │
               │             │ price        │     │ name     │
               │             └──────────────┘     │ capacity │
        ┌──────┴───────┐                          └────┬─────┘
        │   tickets    │                               │
        │──────────────│     ┌──────────────┐          │
        │ id           │     │    seats     │<─────────┘
        │ qrCode       │────>│──────────────│
        │ status       │     │ id           │
        └──────────────┘     │ row          │
                             │ column       │
        ┌──────────────┐     │ seatType     │
        │   payments   │     │ status       │
        │──────────────│     └──────────────┘
        │ id           │
        │ amount       │     ┌──────────────┐
        │ method       │     │   reviews    │
        │ status       │     │──────────────│
        └──────────────┘     │ id           │
                             │ rating       │
        ┌──────────────┐     │ comment      │
        │  vouchers    │     │ votes        │
        │──────────────│     └──────────────┘
        │ id           │
        │ code         │     ┌──────────────┐
        │ discount     │     │ queue_tokens │
        │ expiry       │     │──────────────│
        └──────────────┘     │ id           │
                             │ token        │
        ┌──────────────┐     │ status       │
        │  theaters    │     └──────────────┘
        │──────────────│
        │ id           │
        │ name         │
        │ address      │
        │ city         │
        └──────────────┘
```

### Danh Sách Bảng (17+ tables)

| STT | Tên Bảng | Mô Tả |
|-----|----------|--------|
| 1 | `users` | Thông tin người dùng |
| 2 | `roles` | Vai trò (USER, ADMIN) |
| 3 | `movies` | Thông tin phim |
| 4 | `theaters` | Thông tin rạp chiếu |
| 5 | `rooms` | Phòng chiếu trong rạp |
| 6 | `room_seats` | Quan hệ phòng - ghế |
| 7 | `seats` | Thông tin ghế ngồi |
| 8 | `seat_types` | Loại ghế (Standard, VIP, Premium) |
| 9 | `showtimes` | Suất chiếu phim |
| 10 | `reservations` | Đơn đặt vé |
| 11 | `payments` | Thông tin thanh toán |
| 12 | `tickets` | Vé điện tử |
| 13 | `reviews` | Đánh giá phim |
| 14 | `review_votes` | Bình chọn review |
| 15 | `vouchers` | Mã giảm giá |
| 16 | `queue_tokens` | Token hàng đợi ảo |
| 17 | `user_blocks` | Quản lý chặn người dùng |

---

## 🌐 API Endpoints

### Tổng Quan: **60+ REST API Endpoints** qua **22 Controllers**

<details>
<summary><b>🔐 Authentication (3 endpoints)</b></summary>

| Method | Endpoint | Mô Tả |
|--------|----------|--------|
| `POST` | `/api/auth/register` | Đăng ký tài khoản |
| `POST` | `/api/auth/login` | Đăng nhập |
| `POST` | `/api/auth/refresh-token` | Làm mới token |

</details>

<details>
<summary><b>🎬 Movies (6+ endpoints)</b></summary>

| Method | Endpoint | Mô Tả |
|--------|----------|--------|
| `GET` | `/api/movies` | Danh sách phim (phân trang) |
| `GET` | `/api/movies/{id}` | Chi tiết phim |
| `GET` | `/api/movies/search` | Tìm kiếm phim |
| `POST` | `/api/admin/movies` | Tạo phim mới (Admin) |
| `PUT` | `/api/admin/movies/{id}` | Cập nhật phim (Admin) |
| `DELETE` | `/api/admin/movies/{id}` | Xóa phim (Admin) |

</details>

<details>
<summary><b>🏢 Theaters & Rooms (6+ endpoints)</b></summary>

| Method | Endpoint | Mô Tả |
|--------|----------|--------|
| `GET` | `/api/theaters` | Danh sách rạp |
| `GET` | `/api/theaters/{id}` | Chi tiết rạp |
| `POST` | `/api/admin/theaters` | Tạo rạp mới (Admin) |
| `PUT` | `/api/admin/theaters/{id}` | Cập nhật rạp (Admin) |

</details>

<details>
<summary><b>💺 Seats & Booking (8+ endpoints)</b></summary>

| Method | Endpoint | Mô Tả |
|--------|----------|--------|
| `GET` | `/api/seats/{showtimeId}` | Sơ đồ ghế theo suất chiếu |
| `POST` | `/api/seats/lock` | Khóa ghế (giữ chỗ) |
| `POST` | `/api/seats/unlock` | Mở khóa ghế |
| `POST` | `/api/admin/seats/generate` | Tạo sơ đồ ghế (Admin) |

</details>

<details>
<summary><b>🎫 Reservations & Tickets (10+ endpoints)</b></summary>

| Method | Endpoint | Mô Tả |
|--------|----------|--------|
| `POST` | `/api/reservations/create` | Tạo đơn đặt vé |
| `POST` | `/api/reservations/confirm` | Xác nhận đơn |
| `GET` | `/api/reservations/{id}` | Chi tiết đơn |
| `DELETE` | `/api/reservations/{id}` | Hủy đơn |
| `GET` | `/api/tickets` | Danh sách vé |
| `GET` | `/api/tickets/{id}` | Chi tiết vé |
| `GET` | `/api/qr/generate/{reservationId}` | Tạo mã QR |

</details>

<details>
<summary><b>💳 Payment & Voucher (7+ endpoints)</b></summary>

| Method | Endpoint | Mô Tả |
|--------|----------|--------|
| `POST` | `/api/payments/checkout` | Thanh toán |
| `POST` | `/api/payments/callback` | Payment callback |
| `POST` | `/api/vouchers` | Tạo voucher (Admin) |
| `GET` | `/api/vouchers/check/{code}` | Kiểm tra mã giảm giá |
| `GET` | `/api/vouchers` | Danh sách voucher |

</details>

<details>
<summary><b>📊 Dashboard & Analytics (2+ endpoints)</b></summary>

| Method | Endpoint | Mô Tả |
|--------|----------|--------|
| `GET` | `/api/admin/dashboard/stats` | Thống kê tổng quan |
| `GET` | `/api/admin/dashboard/revenue` | Dữ liệu doanh thu |

</details>

<details>
<summary><b>⚡ Queue & Reviews (7+ endpoints)</b></summary>

| Method | Endpoint | Mô Tả |
|--------|----------|--------|
| `POST` | `/api/queue/join` | Tham gia hàng đợi |
| `GET` | `/api/queue/status/{token}` | Kiểm tra trạng thái |
| `POST` | `/api/reviews` | Viết đánh giá |
| `GET` | `/api/reviews/movie/{movieId}` | Đánh giá theo phim |
| `PUT` | `/api/reviews/{id}/vote` | Vote đánh giá |

</details>

> 📄 **Tài liệu API chi tiết**: Truy cập Swagger UI tại `http://localhost:8080/swagger-ui.html` sau khi chạy Backend.
>
> 📦 **Postman Collection**: Import file `MovieTicket_postman_collection.json` vào Postman để test toàn bộ API.

---

## 💻 Yêu Cầu Hệ Thống

### Phần Mềm Bắt Buộc

| Phần Mềm | Phiên Bản Tối Thiểu | Tải Về |
|-----------|---------------------|--------|
| **Java JDK** | 17+ | [Download](https://adoptium.net/) |
| **Node.js** | 18+ | [Download](https://nodejs.org/) |
| **Docker Desktop** | Latest | [Download](https://www.docker.com/products/docker-desktop/) |
| **Maven** | 3.9+ | [Download](https://maven.apache.org/) |
| **Git** | Latest | [Download](https://git-scm.com/) |

### Phần Cứng Khuyến Nghị

| Thành Phần | Yêu Cầu |
|------------|----------|
| **RAM** | ≥ 8 GB (Docker containers cần ~4 GB) |
| **CPU** | ≥ 4 cores |
| **Disk** | ≥ 10 GB trống |
| **OS** | Windows 10/11, macOS, Linux |

---

## 🚀 Hướng Dẫn Cài Đặt

### Bước 1: Clone Repository

```bash
git clone https://github.com/<your-username>/MovieTicket.git
cd MovieTicket
```

### Bước 2: Khởi Động Infrastructure (Docker)

```bash
cd Backend
docker-compose up -d
```

> ⏳ Đợi khoảng **30-60 giây** để MySQL, Redis, Kafka, Zookeeper khởi động hoàn tất.

Kiểm tra trạng thái containers:

```bash
docker-compose ps
```

Kết quả mong đợi:

```
NAME         STATUS
mysql        running (0.0.0.0:3306->3306)
redis        running (0.0.0.0:6379->6379)
kafka        running (0.0.0.0:9092->9092)
zookeeper    running (0.0.0.0:2181->2181)
```

### Bước 3: Chạy Backend

```bash
cd Backend

# Sử dụng Maven Wrapper
./mvnw spring-boot:run

# Hoặc sử dụng Maven global
mvn spring-boot:run
```

> ✅ Backend sẽ chạy tại: `http://localhost:8080`
>
> 📄 Swagger UI: `http://localhost:8080/swagger-ui.html`

### Bước 4: Chạy Frontend

```bash
cd Frontend

# Cài đặt dependencies
npm install

# Chạy app đầy đủ (User + Admin)
npm run dev

# Hoặc chạy riêng từng app:
npm run dev:user    # User app tại port 5173
npm run dev:admin   # Admin app tại port 5174
```

### Bước 5: Truy Cập Ứng Dụng

| Ứng Dụng | URL | Mô Tả |
|-----------|-----|--------|
| **User App** | `http://localhost:5173` | Giao diện người dùng |
| **Admin App** | `http://localhost:5174` | Bảng quản trị |
| **Backend API** | `http://localhost:8080/api` | REST API |
| **Swagger UI** | `http://localhost:8080/swagger-ui.html` | Tài liệu API |

### ⚡ Quick Start (Tất Cả Trong 1)

```bash
# Terminal 1: Infrastructure
cd Backend && docker-compose up -d

# Terminal 2: Backend
cd Backend && ./mvnw spring-boot:run

# Terminal 3: Frontend
cd Frontend && npm install && npm run dev
```

---

## 📖 Hướng Dẫn Sử Dụng

### Quy Trình Đặt Vé (User Flow)

```
1. Đăng ký / Đăng nhập
        │
        ▼
2. Duyệt danh sách phim ──→ Tìm kiếm / Lọc
        │
        ▼
3. Xem chi tiết phim
        │
        ▼
4. Chọn rạp & suất chiếu
        │
        ▼
5. Chọn ghế (Real-time seat map)
   ├── Ghế Standard 💺
   ├── Ghế VIP 🟡
   └── Ghế Premium 🔴
        │
        ▼
6. Giữ ghế (10 phút countdown)
        │
        ▼
7. Nhập mã giảm giá (tùy chọn)
        │
        ▼
8. Thanh toán
        │
        ▼
9. Nhận vé điện tử + QR Code
        │
        ▼
10. Xem vé trong hồ sơ cá nhân
```

### Tài Khoản Mặc Định

| Vai Trò | Email | Mật Khẩu |
|---------|-------|-----------|
| **Admin** | `admin@ticketrush.com` | `admin123` |
| **User** | `user@ticketrush.com` | `user123` |

> ⚠️ **Lưu ý**: Tài khoản mặc định được tạo bởi Data Seeder khi khởi động lần đầu.

---

## 📸 Screenshots

> 📌 *Thêm screenshots của ứng dụng vào thư mục `docs/screenshots/` và cập nhật phần này.*

<!--
### Trang Chủ
![Home Page](docs/screenshots/home.png)

### Danh Sách Phim
![Movies Page](docs/screenshots/movies.png)

### Chọn Ghế (Real-time)
![Seat Selection](docs/screenshots/seat-selection.png)

### Admin Dashboard
![Admin Dashboard](docs/screenshots/admin-dashboard.png)

### Thanh Toán
![Checkout](docs/screenshots/checkout.png)
-->

---

## 🧪 Kiểm Thử

### Unit Tests (Backend)

```bash
cd Backend
./mvnw test
```

### API Testing (Postman)

1. Mở **Postman**
2. Import file `MovieTicket_postman_collection.json`
3. Thiết lập biến môi trường:
   - `base_url`: `http://localhost:8080`
   - `token`: JWT token sau khi đăng nhập
4. Chạy các request theo thứ tự trong collection

### Kiểm Thử Thủ Công

| Test Case | Mô Tả | Kết Quả Mong Đợi |
|-----------|--------|-------------------|
| Đăng ký tài khoản mới | Nhập thông tin hợp lệ | Tạo tài khoản thành công |
| Đăng nhập | Email + password đúng | Nhận JWT token |
| Tìm kiếm phim | Nhập từ khóa | Hiển thị kết quả liên quan |
| Đặt ghế đồng thời | 2 user cùng đặt 1 ghế | Chỉ 1 user đặt thành công |
| Seat hold timeout | Giữ ghế > 10 phút | Ghế tự động giải phóng |
| Voucher validation | Nhập mã giảm giá | Áp dụng giảm giá real-time |
| WebSocket real-time | User A đặt ghế | User B thấy ghế đổi trạng thái |

---

## 🔑 Các Kỹ Thuật Nổi Bật

### 1. Pessimistic Locking — Ngăn Chặn Race Condition

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT s FROM Seat s WHERE s.id = :seatId")
Optional<Seat> findByIdWithLock(@Param("seatId") Long seatId);
```

> Sử dụng `SELECT...FOR UPDATE` để đảm bảo chỉ **một giao dịch** có thể khóa ghế tại cùng một thời điểm, ngăn chặn hoàn toàn tình trạng **double-booking**.

### 2. Virtual Queue — Xử Lý Traffic Burst

```
User Request → Kafka Producer → Queue Topic → Kafka Consumer → Process Booking
```

> Khi có hàng nghìn người cùng truy cập (flash sale), hệ thống hàng đợi ảo sử dụng Kafka để **xếp hàng và xử lý tuần tự**, tránh quá tải server.

### 3. WebSocket Real-time — Cập Nhật Tức Thì

```
Seat Status Change → SeatRealtimeService → STOMP Broadcast → All Connected Clients
```

> Mỗi khi trạng thái ghế thay đổi (AVAILABLE → LOCKED → BOOKED), tất cả người dùng đang xem cùng suất chiếu sẽ nhận được **cập nhật tức thì** mà không cần refresh trang.

---

## 🤝 Đóng Góp

Dự án này được phát triển như đề tài khóa luận tốt nghiệp. Mọi đóng góp và góp ý đều được hoan nghênh!

1. **Fork** repository
2. Tạo **feature branch** (`git checkout -b feature/AmazingFeature`)
3. **Commit** thay đổi (`git commit -m 'Add some AmazingFeature'`)
4. **Push** lên branch (`git push origin feature/AmazingFeature`)
5. Mở **Pull Request**

---

## 📄 Giấy Phép

Dự án này được phân phối theo giấy phép **MIT License**. Xem file [LICENSE](LICENSE) để biết thêm chi tiết.

---

## 📞 Thông Tin Liên Hệ

| Thông Tin | Chi Tiết |
|-----------|----------|
| **Tác giả** | *[Họ và tên sinh viên]* |
| **MSSV** | *[Mã số sinh viên]* |
| **Email** | *[Email sinh viên]* |
| **Trường** | *[Tên trường đại học]* |
| **Khoa** | *[Tên khoa]* |
| **GVHD** | *[Tên giảng viên hướng dẫn]* |
| **Năm** | 2026 |

---

<p align="center">
  <b>⭐ Nếu dự án này hữu ích, hãy cho một star trên GitHub! ⭐</b>
</p>

<p align="center">
  Made with ❤️ for Graduation Thesis
</p>