# Frontend integration examples

## 1. Auth examples

### 1.1 Register request

```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "Bat Erdene",
  "email": "bat@example.com",
  "password": "secret123",
  "phone": "+97699112233"
}
```

### 1.2 Register / Login / Refresh success response

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.access-token",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9.refresh-token",
  "userId": 101,
  "email": "bat@example.com",
  "name": "Bat Erdene",
  "role": "USER"
}
```

### 1.3 Login request

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "bat@example.com",
  "password": "secret123"
}
```

### 1.4 Refresh request

```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9.refresh-token"
}
```

### 1.5 Logout request

```http
POST /api/auth/logout
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.access-token
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9.refresh-token"
}
```

Logout success response:

```http
204 No Content
```

## 2. Booking examples

### 2.1 Create booking request

```http
POST /api/bookings
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.access-token
Content-Type: application/json

{
  "roomId": 10,
  "checkinDate": "2026-04-01",
  "checkoutDate": "2026-04-03",
  "firstName": "Bat",
  "lastName": "Erdene",
  "email": "bat@example.com",
  "phone": "+97699112233",
  "guestCount": 2,
  "specialRequests": "High floor if available"
}
```

### 2.2 Update booking request

```http
PUT /api/bookings/5001
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.access-token
Content-Type: application/json

{
  "checkinDate": "2026-04-02",
  "checkoutDate": "2026-04-04",
  "firstName": "Bat",
  "lastName": "Erdene",
  "email": "bat@example.com",
  "phone": "+97699112233",
  "guestCount": 2,
  "specialRequests": "Late arrival",
  "status": "PENDING"
}
```

### 2.3 Booking detail/list item response

```json
{
  "id": 5001,
  "user": {
    "id": 101,
    "name": "Bat Erdene",
    "email": "bat@example.com"
  },
  "room": {
    "id": 10,
    "hotelId": 7,
    "roomType": "DELUXE",
    "price": 180000.0,
    "capacity": 2,
    "status": "AVAILABLE"
  },
  "checkinDate": "2026-04-01",
  "checkoutDate": "2026-04-03",
  "firstName": "Bat",
  "lastName": "Erdene",
  "email": "bat@example.com",
  "phone": "+97699112233",
  "guestCount": 2,
  "specialRequests": "High floor if available",
  "nights": 2,
  "roomPrice": 180000.00,
  "serviceFee": 12000.00,
  "totalPrice": 372000.00,
  "bookingNumber": "BK-20260401-5001",
  "status": "PENDING"
}
```

### 2.4 Booking list response

```json
[
  {
    "id": 5001,
    "user": {
      "id": 101,
      "name": "Bat Erdene",
      "email": "bat@example.com"
    },
    "room": {
      "id": 10,
      "hotelId": 7,
      "roomType": "DELUXE",
      "price": 180000.0,
      "capacity": 2,
      "status": "AVAILABLE"
    },
    "checkinDate": "2026-04-01",
    "checkoutDate": "2026-04-03",
    "firstName": "Bat",
    "lastName": "Erdene",
    "email": "bat@example.com",
    "phone": "+97699112233",
    "guestCount": 2,
    "specialRequests": "High floor if available",
    "nights": 2,
    "roomPrice": 180000.00,
    "serviceFee": 12000.00,
    "totalPrice": 372000.00,
    "bookingNumber": "BK-20260401-5001",
    "status": "PENDING"
  }
]
```

## 3. Wishlist examples

### 3.1 Add to wishlist request

```http
POST /api/wishlist
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.access-token
Content-Type: application/json

{
  "userId": 101,
  "roomId": 10
}
```

### 3.2 Wishlist response

```json
{
  "id": 301,
  "userId": 101,
  "userName": "Bat Erdene",
  "roomId": 10,
  "roomType": "DELUXE",
  "roomPrice": 180000.0,
  "roomCapacity": 2,
  "createdAt": "2026-03-22T09:30:00"
}
```

### 3.3 Wishlist exists response

```json
{
  "exists": true
}
```

## 4. Notification examples

### 4.1 Create notification request

```http
POST /api/notifications
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.access-token
Content-Type: application/json

{
  "userId": 101,
  "title": "Захиалга баталгаажлаа",
  "message": "Таны захиалга амжилттай баталгаажлаа.",
  "type": "BOOKING"
}
```

### 4.2 Notification response

```json
{
  "id": 701,
  "userId": 101,
  "title": "Захиалга баталгаажлаа",
  "message": "Таны захиалга амжилттай баталгаажлаа.",
  "type": "BOOKING",
  "read": false,
  "createdAt": "2026-03-22T09:45:00"
}
```

### 4.3 Unread count response

```json
{
  "unreadCount": 3
}
```

### 4.4 Mark all read response

```json
{
  "updated": 3
}
```

## 5. Payment examples

### 5.1 Create payment request

```http
POST /api/payments
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.access-token
Content-Type: application/json

{
  "bookingId": 5001,
  "amount": 372000.00,
  "paymentMethod": "QPAY"
}
```

### 5.2 Payment response

```json
{
  "id": 801,
  "bookingId": 5001,
  "amount": 372000.00,
  "paymentMethod": "QPAY",
  "status": "PENDING",
  "paymentDate": "2026-03-22T09:50:00Z"
}
```

## 6. Error response examples

### 6.1 400 validation error

```json
{
  "timestamp": "2026-03-22T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_ERROR",
  "message": "Хүсэлтийн өгөгдөл буруу байна",
  "path": "/api/bookings",
  "traceId": "f8d7a6b9-4d47-4a3b-9bd5-0d4b8f3be222",
  "validationErrors": [
    {
      "field": "email",
      "message": "Email формат буруу байна",
      "rejectedValue": "bat-at-example.com"
    },
    {
      "field": "guestCount",
      "message": "Зочдын тоо 1-ээс багагүй байна",
      "rejectedValue": 0
    }
  ]
}
```

### 6.2 401 invalid credentials

```json
{
  "timestamp": "2026-03-22T10:01:00Z",
  "status": 401,
  "error": "Unauthorized",
  "code": "AUTH_INVALID_CREDENTIALS",
  "message": "Email эсвэл нууц үг буруу",
  "path": "/api/auth/login",
  "traceId": "2b0e25ec-f74c-4e15-a2da-7b148a0bb6ef"
}
```

### 6.3 401 invalid token

```json
{
  "timestamp": "2026-03-22T10:02:00Z",
  "status": 401,
  "error": "Unauthorized",
  "code": "AUTH_INVALID_TOKEN",
  "message": "Refresh token хүчингүй болсон байна",
  "path": "/api/auth/refresh",
  "traceId": "d950d953-7d7e-41c4-84b7-dc53a611ccf9"
}
```

### 6.4 403 forbidden

```json
{
  "timestamp": "2026-03-22T10:03:00Z",
  "status": 403,
  "error": "Forbidden",
  "code": "FORBIDDEN",
  "message": "Та зөвхөн өөрийн мэдээлэлд хандах эрхтэй",
  "path": "/api/wishlist/user/999",
  "traceId": "0f0ab262-5d99-4408-a8d4-c930d7d1494b"
}
```

### 6.5 404 not found

```json
{
  "timestamp": "2026-03-22T10:04:00Z",
  "status": 404,
  "error": "Not Found",
  "code": "RESOURCE_NOT_FOUND",
  "message": "Захиалга олдсонгүй: ID=99999",
  "path": "/api/bookings/99999",
  "traceId": "8f8e4ba8-817b-4069-8c62-ffb4d7921ef2"
}
```

### 6.6 429 rate limited

```json
{
  "timestamp": "2026-03-22T10:05:00Z",
  "status": 429,
  "error": "Too Many Requests",
  "code": "AUTH_RATE_LIMITED",
  "message": "Хэт олон удаа буруу нэвтрэх оролдлого хийлээ. Дараа дахин оролдоно уу.",
  "path": "/api/auth/login",
  "traceId": "59978a42-d858-4b65-b3a7-748d0c794714"
}
```
