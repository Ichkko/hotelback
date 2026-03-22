# Role / ownership matrix

## 1. Role matrix

| Area | Guest | User | Admin |
|---|---|---|---|
| `POST /api/auth/register` | Yes | Yes | Yes |
| `POST /api/auth/login` | Yes | Yes | Yes |
| `POST /api/auth/refresh` | Yes | Yes | Yes |
| `POST /api/auth/logout` | Yes | Yes | Yes |
| `GET /api/hotels/**` | Yes | Yes | Yes |
| `GET /api/rooms/**` | Yes | Yes | Yes |
| `POST/PUT/DELETE /api/hotels/**` | No | No | Yes |
| `POST/PUT/DELETE /api/rooms/**` | No | No | Yes |
| `POST/PUT/DELETE /api/amenities/**` | No | No | Yes |
| `POST/PUT/DELETE /api/highlights/**` | No | No | Yes |
| `/api/users/**` | No | No | Yes |
| `/api/bookings/**` | No | Own/Admin | Yes |
| `/api/wishlist/**` | No | Own/Admin | Yes |
| `/api/notifications/**` | No | Own/Admin | Yes |
| `/api/payments/**` | No | Yes | Yes |

## 2. Ownership rules

### Bookings

- `GET /api/bookings`:
  - Admin бол бүх bookings.
  - User бол зөвхөн current user-ийн bookings.
- `GET /api/bookings/user/{userId}`: user өөрийн `userId` эсвэл admin.
- `GET /api/bookings/{id}`: booking owner эсвэл admin.
- `PUT /api/bookings/{id}`: booking owner эсвэл admin.
- `POST /api/bookings/{id}/confirm`: booking owner эсвэл admin.
- `POST /api/bookings/{id}/cancel`: booking owner эсвэл admin.
- `DELETE /api/bookings/{id}`: booking owner эсвэл admin.

### Wishlist

- `POST /api/wishlist`: request-ийн `userId` нь current user эсвэл admin.
- `GET /api/wishlist/user/{userId}`: current user эсвэл admin.
- `GET /api/wishlist/user/{userId}/room/{roomId}/exists`: current user эсвэл admin.
- `DELETE /api/wishlist/{id}`: wishlist owner эсвэл admin.
- `DELETE /api/wishlist/user/{userId}/room/{roomId}`: current user эсвэл admin.

### Notifications

- `POST /api/notifications`: request-ийн `userId` нь current user эсвэл admin.
- `GET /api/notifications/user/{userId}`: current user эсвэл admin.
- `GET /api/notifications/user/{userId}/unread-count`: current user эсвэл admin.
- `POST /api/notifications/{id}/read`: notification owner эсвэл admin.
- `POST /api/notifications/user/{userId}/read-all`: current user эсвэл admin.
- `DELETE /api/notifications/{id}`: notification owner эсвэл admin.

## 3. Frontend implementation rule

- Non-admin user дээр өөр user-ийн id input field, selector, admin switch харуулахгүй.
- UI route guard болон API-level auth interceptor хоёуланг ашиглана.
- 403 response-ийг generic error биш, access denied UX болгон үзүүлнэ.
