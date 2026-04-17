# 📋 API Test Scenarios - MovieTicket Backend

Tài liệu này chứa tất cả kịch bản test API cho dự án MovieTicket. Mỗi kịch bản bao gồm request, expected response, và các bước thực hiện trên Postman.

---

## 🔐 NGÀY 1: Authentication APIs

### Scenario 1.1: Register User (Đăng ký người dùng mới)

**Endpoint:** `POST /api/v1/auth/register`

**Purpose:** Đăng ký tài khoản mới

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "userName": "john_doe",
  "email": "john@example.com",
  "password": "SecurePassword123",
  "phoneNumber": "0123456789",
  "dateOfBirth": "1990-01-15",
  "gender": "Male"
}
```

**Expected Response (200 OK):**
```json
{
  "message": "User registered successfully!"
}
```

**Test Steps on Postman:**
1. Create new request: POST http://localhost:8080/api/v1/auth/register
2. Set Headers: Content-Type: application/json
3. Paste request body JSON
4. Click Send
5. Verify response status = 200 OK

**Success Criteria:**
- ✅ Status code: 200
- ✅ Response contains success message
- ✅ User exists in database

**Error Test Case:**
```json
{
  "userName": "john_doe",
  "email": "john@example.com"
}
```
**Expected:** 400 Bad Request (missing required fields)

---

### Scenario 1.2: Login User (Đăng nhập)

**Endpoint:** `POST /api/v1/auth/login`

**Purpose:** Đăng nhập và lấy JWT token

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "SecurePassword123"
}
```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwiaWF0IjoxNzE0OTczMDAwLCJleHAiOjE3MTQ5NzY2MDB9.abc123...",
  "username": "john@example.com",
  "roles": ["ROLE_USER"]
}
```

**Test Steps on Postman:**
1. Create new request: POST http://localhost:8080/api/v1/auth/login
2. Set Headers: Content-Type: application/json
3. Paste request body JSON
4. Click Send
5. **IMPORTANT:** Copy JWT token from response
6. Save token in Postman variable: `{{jwt_token}}`

**How to Save Token in Postman Variable:**
1. Click "Tests" tab
2. Add script:
```javascript
var jsonData = pm.response.json();
pm.environment.set("jwt_token", jsonData.token);
```
3. Now use `{{jwt_token}}` in Authorization header for other requests

**Success Criteria:**
- ✅ Status code: 200
- ✅ Response contains JWT token
- ✅ Token is not empty

**Error Test Cases:**
```json
// Wrong password
{
  "email": "john@example.com",
  "password": "WrongPassword"
}
```
**Expected:** 401 Unauthorized

```json
// Non-existent user
{
  "email": "nonexistent@example.com",
  "password": "AnyPassword"
}
```
**Expected:** 401 Unauthorized

---

### Scenario 1.3: Get Current User Info (Lấy thông tin user hiện tại)

**Endpoint:** `GET /api/v1/auth/me`

**Purpose:** Lấy thông tin user đang đăng nhập

**Request Headers:**
```
Authorization: Bearer {{jwt_token}}
```

**Request Body:** (None)

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "username": "john@example.com",
  "roles": ["ROLE_USER"]
}
```

**Test Steps on Postman:**
1. Create new request: GET http://localhost:8080/api/v1/auth/me
2. Set Headers:
   - Authorization: Bearer {{jwt_token}}
3. Click Send
4. Verify response contains user info

**Success Criteria:**
- ✅ Status code: 200
- ✅ Response contains user id, username, roles

**Error Test Case (No Token):**
- Remove Authorization header
- Expected: 401 Unauthorized

---

## 🎬 NGÀY 5-6: Master Data APIs

### Scenario 2.1: Get All Movies (Lấy danh sách phim)

**Endpoint:** `GET /api/v1/movies`

**Purpose:** Lấy danh sách tất cả phim đang chiếu

**Request Headers:**
```
Authorization: Bearer {{jwt_token}}
Content-Type: application/json
```

**Request Body:** (None)

**Expected Response (200 OK):**
```json
[
  {
    "id": 1,
    "title": "Avatar",
    "description": "A paraplegic Marine dispatched to the moon Pandora...",
    "releaseYear": 2009,
    "genre": "Science Fiction",
    "posterImageUrl": "https://example.com/avatar.jpg"
  },
  {
    "id": 2,
    "title": "Avengers: Endgame",
    "description": "After the devastating events...",
    "releaseYear": 2019,
    "genre": "Action",
    "posterImageUrl": "https://example.com/avengers.jpg"
  }
]
```

**Test Steps on Postman:**
1. Create new request: GET http://localhost:8080/api/v1/movies
2. Set Headers:
   - Authorization: Bearer {{jwt_token}}
   - Content-Type: application/json
3. Click Send
4. Verify list is returned

**Success Criteria:**
- ✅ Status code: 200
- ✅ Response is array of movies
- ✅ Each movie has id, title, genre, etc.

---

### Scenario 2.2: Get Movie Details (Lấy chi tiết phim theo ID)

**Endpoint:** `GET /api/v1/movies/{id}`

**Purpose:** Lấy thông tin chi tiết một phim

**Request Headers:**
```
Authorization: Bearer {{jwt_token}}
```

**URL Parameter:**
- id = 1 (Movie ID)

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "title": "Avatar",
  "description": "A paraplegic Marine dispatched to the moon Pandora...",
  "releaseYear": 2009,
  "genre": "Science Fiction",
  "posterImageUrl": "https://example.com/avatar.jpg"
}
```

**Test Steps on Postman:**
1. Create new request: GET http://localhost:8080/api/v1/movies/1
2. Set Headers:
   - Authorization: Bearer {{jwt_token}}
3. Click Send
4. Verify single movie returned

**Success Criteria:**
- ✅ Status code: 200
- ✅ Response contains movie with id=1

**Error Test Case (Invalid ID):**
- URL: GET http://localhost:8080/api/v1/movies/9999
- Expected: 404 Not Found

---

### Scenario 2.3: Get All Theaters (Lấy danh sách rạp)

**Endpoint:** `GET /api/v1/theaters`

**Purpose:** Lấy danh sách tất cả rạp chiếu phim

**Request Headers:**
```
Authorization: Bearer {{jwt_token}}
```

**Request Body:** (None)

**Expected Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "CGV Landmark 81",
    "location": "106 Nguyen Hue, District 1, HCMC",
    "capacity": 500
  },
  {
    "id": 2,
    "name": "BHD Star Cineplex",
    "location": "74 Nguyen Hue, District 1, HCMC",
    "capacity": 350
  }
]
```

**Test Steps on Postman:**
1. Create new request: GET http://localhost:8080/api/v1/theaters
2. Set Headers:
   - Authorization: Bearer {{jwt_token}}
3. Click Send
4. Verify list of theaters returned

**Success Criteria:**
- ✅ Status code: 200
- ✅ Response is array of theaters
- ✅ Each theater has id, name, location, capacity

---

### Scenario 2.4: Get Theater Details (Lấy chi tiết rạp)

**Endpoint:** `GET /api/v1/theaters/{id}`

**Purpose:** Lấy thông tin chi tiết một rạp

**Request Headers:**
```
Authorization: Bearer {{jwt_token}}
```

**URL Parameter:**
- id = 1

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "name": "CGV Landmark 81",
  "location": "106 Nguyen Hue, District 1, HCMC",
  "capacity": 500
}
```

**Test Steps on Postman:**
1. Create new request: GET http://localhost:8080/api/v1/theaters/1
2. Set Headers:
   - Authorization: Bearer {{jwt_token}}
3. Click Send

**Success Criteria:**
- ✅ Status code: 200
- ✅ Response contains theater with id=1

---

### Scenario 2.5: Get Showtimes (Lấy suất chiếu)

**Endpoint:** `GET /api/v1/showtimes`

**Purpose:** Tìm suất chiếu theo phim/rạp và ngày chiếu chính xác

**Request Headers:**
```
Authorization: Bearer {{jwt_token}}
```

**Test Case A: By Movie and Date**

**URL:** `GET /api/v1/showtimes?movieId=1&showDate=2026-04-13`

**Purpose:** Lấy tất cả suất chiếu của phim 1 **vào ngày 2026-04-13 chính xác**

**Expected Response (200 OK):**
```json
[
  {
    "id": 1,
    "showDate": "2026-04-13",
    "showTime": "10:00:00",
    "price": 120000,
    "totalSeats": 150,
    "availableSeats": 45,
    "isFlashSale": false,
    "movie": {
      "id": 1,
      "title": "Avatar",
      "posterImageUrl": "https://...",
      "genre": "Science Fiction"
    },
    "theater": {
      "id": 1,
      "name": "CGV Landmark 81",
      "location": "106 Nguyen Hue, District 1, HCMC"
    }
  },
  {
    "id": 3,
    "showDate": "2026-04-13",
    "showTime": "13:30:00",
    "price": 120000,
    "totalSeats": 150,
    "availableSeats": 80,
    "isFlashSale": true,
    "movie": {
      "id": 1,
      "title": "Avatar",
      "posterImageUrl": "https://...",
      "genre": "Science Fiction"
    },
    "theater": {
      "id": 3,
      "name": "Lotte Cinema",
      "location": "780 Tran Hung Dao, District 1, HCMC"
    }
  }
]
```

**Test Steps on Postman:**
1. Create new request: GET http://localhost:8080/api/v1/showtimes?movieId=1&showDate=2026-04-13
2. Set Headers:
   - Authorization: Bearer {{jwt_token}}
3. Click Send
4. Verify showtimes for that movie on that exact date are returned

**Test Case B: By Theater and Date**

**URL:** `GET /api/v1/showtimes?theaterId=1&showDate=2026-04-13`

**Purpose:** Lấy tất cả suất chiếu tại rạp 1 **vào ngày 2026-04-13 chính xác**

**Success Criteria:**
- ✅ Status code: 200
- ✅ Response is array of showtimes
- ✅ All showtimes have showDate = 2026-04-13 (exact date match)
- ✅ Each showtime has id, showDate, showTime, price, movie, theater

**Test Case C: Only by Movie (No Date)**

**URL:** `GET /api/v1/showtimes?movieId=1`

**Purpose:** Lấy suất chiếu của phim 1 **vào ngày hôm nay (2026-04-17)**

**Expected:** Empty array [] (vì không có showtimes vào ngày 2026-04-17)

**Test Case D: Only by ShowDate (No Movie/Theater)**

**URL:** `GET /api/v1/showtimes?showDate=2026-04-13`

**Purpose:** Lấy tất cả suất chiếu **vào ngày 2026-04-13 chính xác** (mọi phim, mọi rạp)

**Error Test Case (No Parameters):**
- URL: GET http://localhost:8080/api/v1/showtimes
- Expected: 200 OK + Empty array [] (hoặc logic có thể yêu cầu ít nhất 1 parameter)

---

### Scenario 2.6: Get Showtime Details (Lấy chi tiết suất chiếu)

**Endpoint:** `GET /api/v1/showtimes/{id}`

**Purpose:** Lấy thông tin chi tiết một suất chiếu theo ID

**Request Headers:**
```
Authorization: Bearer {{jwt_token}}
```

**URL Parameter:**
- id = 1

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "showDate": "2026-04-13",
  "showTime": "10:00:00",
  "price": 120000,
  "totalSeats": 150,
  "availableSeats": 45,
  "isFlashSale": false,
  "movie": {
    "id": 1,
    "title": "Avatar",
    "posterImageUrl": "https://...",
    "genre": "Science Fiction"
  },
  "theater": {
    "id": 1,
    "name": "CGV Landmark 81",
    "location": "106 Nguyen Hue, District 1, HCMC"
  }
}
```

**Test Steps on Postman:**
1. Create new request: GET http://localhost:8080/api/v1/showtimes/1
2. Set Headers:
   - Authorization: Bearer {{jwt_token}}
3. Click Send

**Success Criteria:**
- ✅ Status code: 200
- ✅ Response contains showtime details with all fields

**Error Test Case (Invalid ID):**
- URL: GET http://localhost:8080/api/v1/showtimes/9999
- Expected: 404 Not Found

---

## 💺 NGÀY 7: Admin Seat Matrix Generation API

### Scenario 3.1: Generate Seat Matrix (Tự động sinh sơ đồ ghế)

**Endpoint:** `POST /api/v1/admin/seats/matrix/generate`

**Purpose:** Tự động sinh sơ đồ ghế cho suất chiếu

**Request Headers:**
```
Authorization: Bearer {{jwt_token}}
Content-Type: application/json
```

**Test Case A: Standard Theater (10×15)**

**Request Body:**
```json
{
  "showtimeId": 1,
  "rows": 10,
  "cols": 15
}
```

**Expected Response (200 OK):**
```json
{
  "showtimeId": 1,
  "totalSeatsGenerated": 150,
  "rows": 10,
  "cols": 15,
  "message": "Đã tạo thành công 150 ghế cho suất chiếu (Hàng: 10, Cột: 15)"
}
```

**Test Steps on Postman:**
1. Create new request: POST http://localhost:8080/api/v1/admin/seats/matrix/generate
2. Set Headers:
   - Authorization: Bearer {{jwt_token}}
   - Content-Type: application/json
3. Paste request body
4. Click Send
5. Verify 150 seats generated

**Success Criteria:**
- ✅ Status code: 200
- ✅ totalSeatsGenerated = 150
- ✅ Message confirms success
- ✅ 150 seat records in database

**Database Verification:**
```sql
SELECT COUNT(*) FROM seats WHERE showtime_id = 1;
-- Result: 150

SELECT seat_number FROM seats WHERE showtime_id = 1
ORDER BY seat_number LIMIT 20;
-- Result: A1, A2, A3, ..., A15, B1, B2, ...
```

**Test Case B: Large Theater (20×25)**

**Request Body:**
```json
{
  "showtimeId": 2,
  "rows": 20,
  "cols": 25
}
```

**Expected:** 500 seats generated (20 × 25)

**Test Case C: Small Screen (5×8)**

**Request Body:**
```json
{
  "showtimeId": 3,
  "rows": 5,
  "cols": 8
}
```

**Expected:** 40 seats generated (5 × 8)

**Error Test Case 1: Missing Field**

**Request Body:**
```json
{
  "rows": 10,
  "cols": 15
}
```

**Expected:** 400 Bad Request (showtimeId is required)

**Error Test Case 2: Invalid ShowtimeId**

**Request Body:**
```json
{
  "showtimeId": 9999,
  "rows": 10,
  "cols": 15
}
```

**Expected Response (500 Internal Error):**
```json
{
  "statusCode": 500,
  "timestamp": "2026-04-17T12:35:00.123Z",
  "message": "Suất chiếu không tìm thấy với ID: 9999",
  "description": "uri=/api/v1/admin/seats/matrix/generate"
}
```

---

**Error Test Case 3: Seats Already Exist**

**Steps:**
1. Generate seats for showtimeId=1 (succeed)
2. Try again with same showtimeId
3. Expected: 400 Bad Request (seats already exist)

**Request Body (second attempt):**
```json
{
  "showtimeId": 1,
  "rows": 10,
  "cols": 15
}
```

**Expected Response (400 Bad Request):**
```json
{
  "statusCode": 400,
  "timestamp": "2026-04-17T12:35:15.456Z",
  "message": "Suất chiếu này đã có sơ đồ ghế. Vui lòng xóa ghế cũ trước khi tạo mới.",
  "description": "uri=/api/v1/admin/seats/matrix/generate"
}
```

**How to Fix:**
If you get this error and want to regenerate seats:
```sql
DELETE FROM seats WHERE showtime_id = 1;
```
Then retry the generation request.

---

**Error Test Case 4: Invalid Rows Value**

**Request Body:**
```json
{
  "showtimeId": 1,
  "rows": 0,
  "cols": 15
}
```

**Expected Response (400 Bad Request):**
```json
{
  "statusCode": 400,
  "timestamp": "2026-04-17T12:35:30.789Z",
  "message": "Số hàng phải lớn hơn 0",
  "description": "uri=/api/v1/admin/seats/matrix/generate"
}
```

**Note:** Validation error from @Min(1) annotation on rows field

---

## 📊 POSTMAN COLLECTION SETUP

### Step 1: Create Environment Variable

1. Click "Environments" (top left)
2. Create new environment: "MovieTicket-Dev"
3. Add variables:
   ```
   Variable Name: jwt_token
   Initial Value: (leave empty)
   Current Value: (leave empty)

   Variable Name: base_url
   Initial Value: http://localhost:8080/api
   Current Value: http://localhost:8080/api
   ```

### Step 2: Setup Base Request Headers

For each request, add common headers:
```
Content-Type: application/json
Authorization: Bearer {{jwt_token}}
```

### Step 3: Create Request Collections

**Folder Structure:**
```
MovieTicket API Tests
├── 1. Authentication
│   ├── Register User
│   ├── Login User
│   └── Get Current User
├── 2. Master Data (Movies/Theaters/Showtimes)
│   ├── Get All Movies
│   ├── Get Movie by ID
│   ├── Get All Theaters
│   ├── Get Theater by ID
│   ├── Get Showtimes
│   └── Get Showtime by ID
└── 3. Admin Operations
    └── Generate Seat Matrix
```

### Step 4: Test Execution Order

**Recommended Flow:**
1. **First:** Register User (Scenario 1.1)
2. **Second:** Login User (Scenario 1.2) → Copy JWT token
3. **Then:** Get Current User (Scenario 1.3) → Verify token works
4. **Then:** All Master Data APIs (Scenarios 2.1-2.6)
5. **Finally:** Seat Generation (Scenario 3.1)

---

## 🔍 COMMON TESTING ISSUES & SOLUTIONS

### Issue 1: "401 Unauthorized"
**Cause:** Missing or invalid JWT token
**Solution:**
1. Run Login test (Scenario 1.2)
2. Copy token from response
3. Set Authorization header with: `Bearer {token}`

### Issue 2: "400 Bad Request"
**Cause:** Invalid request format
**Solution:**
1. Check JSON syntax (use online JSON validator)
2. Verify all required fields present
3. Check field types (string, number, date format)

### Issue 3: "404 Not Found"
**Cause:** Invalid ID or resource doesn't exist
**Solution:**
1. Verify ID exists in database
2. Use GET /api/v1/movies first to see valid IDs

### Issue 4: Seats Already Exist Error
**Cause:** Trying to generate seats for showtime with existing seats
**Solution:**
1. Delete existing seats: `DELETE FROM seats WHERE showtime_id = {id}`
2. Then retry generation

### Issue 5: Connection Refused
**Cause:** Backend not running
**Solution:**
1. Start backend: `mvn spring-boot:run`
2. Verify on http://localhost:8080/api/swagger-ui.html

---

## 📈 PERFORMANCE TESTING

### Test: Seat Generation Performance

**Objective:** Verify seat generation is fast (batch insert)

**Test Case:**
```json
{
  "showtimeId": 1,
  "rows": 25,
  "cols": 30
}
```

**Expected:**
- Response time: < 500ms
- 750 seats generated
- 1 batch SQL insert (not 750 individual inserts)

**Postman Timing:**
1. Click "Tests" tab
2. Add script:
```javascript
console.log("Response time: " + pm.response.responseTime + "ms");
var jsonData = pm.response.json();
console.log("Seats generated: " + jsonData.totalSeatsGenerated);
```

---

## ✅ COMPLETION CHECKLIST

**After testing all scenarios, verify:**
- [ ] All authentication tests pass
- [ ] All master data API tests pass
- [ ] All seat generation tests pass
- [ ] Error handling tests pass
- [ ] Performance acceptable (< 500ms for seat generation)
- [ ] Database records created correctly
- [ ] No authentication/authorization issues
- [ ] JWT token management works correctly

---

**Date:** 2026-04-17
**Status:** Complete Test Scenarios
**Next:** Run all tests on local environment

