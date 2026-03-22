# Frontend багт өгөх requirement document

Энэ баримт нь `hotelback` backend-тэй уялдуулан frontend багт өгөх албан шаардлага, integration rule, acceptance criteria, болон handoff checklist-ийг нэгтгэнэ.

## 1. Зорилго

Frontend баг дараах зорилгыг хангаж хөгжүүлэлт хийх ёстой.

- Backend API contract-ийг field name, endpoint, auth rule, error schema түвшинд яг мөрдөх.
- Auth, role, ownership дүрмийг UI, route, state management, action permission дээр бүрэн хэрэгжүүлэх.
- Public, authenticated, admin модулиудыг backend security policy-тэй нийцүүлэн салгаж хөгжүүлэх.
- Validation, loading, empty, success, error state-уудыг нэг стандарттай болгох.
- Release хийхээс өмнө integration checklist-ээр баталгаажуулалт хийх.

## 2. Scope

### 2.1 Public scope

Нэвтрэхгүй хэрэглэгч дараахийг ашиглаж чадна.

- Auth: register, login, refresh, logout endpoint-ууд public байна.
- Hotel list, hotel search, hotel detail, hotel-ийн room list.
- Room list, room detail, available room search.
- Room image, amenity, highlight GET endpoint-ууд.

### 2.2 Authenticated user scope

Нэвтэрсэн хэрэглэгч дараах модульд ажиллана.

- Booking
- Wishlist
- Notification
- Payment

### 2.3 Admin scope

`ADMIN` role-той хэрэглэгч дараах CRUD болон management screen-үүдэд хандана.

- Hotels CRUD
- Rooms CRUD
- Amenities CRUD
- Highlights CRUD
- Users management

## 3. Архитектурын шаардлага

Frontend баг дараах архитектурын суурь шаардлагыг заавал мөрдөнө.

### 3.1 API layer

Бүх API integration нь component дотроос шууд `fetch` хийхгүй, domain-аар салгасан client layer ашиглана.

Санал болгох бүтэц:

- `authApi`
- `hotelApi`
- `roomApi`
- `bookingApi`
- `wishlistApi`
- `notificationApi`
- `paymentApi`
- `adminUserApi`

### 3.2 Shared request wrapper

Нэг shared request wrapper дараахийг хариуцана.

- `Authorization: Bearer <token>` автоматаар нэмэх.
- `Content-Type: application/json` default header.
- 401 үед refresh flow эсвэл forced logout.
- 403 үед permission/ownership access denied UI state.
- 400/422 validation error-г form-level map руу хөрвүүлэх.
- `traceId` байвал log болон support panel-д харуулах.

### 3.3 Type safety

Backend DTO бүрт frontend type/interface тодорхойлно. Hardcoded shape ашиглахгүй.

Заавал бий болгох type-ууд:

- `AuthResponse`
- `RegisterRequest`
- `LoginRequest`
- `RefreshTokenRequest`
- `LogoutRequest`
- `HotelResponse`
- `RoomResponse`
- `CreateBookingRequest`
- `BookingResponse`
- `WishlistRequest`
- `WishlistResponse`
- `NotificationRequest`
- `NotificationResponse`
- `CreatePaymentRequest`
- `PaymentResponse`
- `ErrorResponse`
- `ValidationError`

## 4. Auth ба session management шаардлага

### 4.1 Хэрэгжүүлэх flow

Frontend дараах flow-ийг бүрэн хэрэгжүүлнэ.

- Register
- Login
- Session restore
- Refresh token renewal
- Logout
- Expired access token үед retry/refresh policy
- Refresh алдагдсан эсвэл expired үед forced logout

### 4.2 Backend contract

Auth endpoint-ууд:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`

Auth response нь дараах талбаруудтай байна.

- `token`
- `refreshToken`
- `userId`
- `email`
- `name`
- `role`

### 4.3 FE implementation rule

- Register form дээр role selector гаргахгүй. Backend хэрэглэгчийг `USER` role-той үүсгэнэ.
- Access token болон refresh token хадгалах бодлогыг багийн дотор тогтмол баримтална.
- Logout үед access token болон refresh token хоёрыг хоёуланг invalidate хийх request явуулна.
- App boot хийхэд session restore ажиллуулна.
- 401 response бүрийг blind retry хийхгүй; refresh нэг удаа оролдоод бүтэлгүйтвэл logout хийнэ.

## 5. Role ба ownership дүрэм

### 5.1 Role matrix

| Area | Guest | User | Admin |
|---|---|---|---|
| Hotels, rooms read | Yes | Yes | Yes |
| Bookings own data | No | Yes | Yes |
| Wishlist own data | No | Yes | Yes |
| Notifications own data | No | Yes | Yes |
| Payments | No | Yes | Yes |
| Hotel/room CRUD | No | No | Yes |
| Amenity/highlight CRUD | No | No | Yes |
| User management | No | No | Yes |

### 5.2 Ownership rule

Frontend дараах ownership logic-ийг UI дээр хаалттайгаар хэрэгжүүлнэ.

- User зөвхөн өөрийн booking дээр action хийж чадна.
- User зөвхөн өөрийн wishlist дээр action хийж чадна.
- User зөвхөн өөрийн notification дээр action хийж чадна.
- Admin бүх хэрэглэгчийн мэдээлэлд хандаж чадна.

### 5.3 UI requirement

- Route guard байна.
- Menu visibility role-оор ялгарна.
- CTA button-ууд permission-оор ялгарна.
- User өөр userId-тай хүсэлт үүсгэх UI-г энгийн хэрэглэгч дээр гаргахгүй.
- 403 алдаа гарвал generic crash биш, ойлгомжтой access denied state үзүүлнэ.

## 6. Domain module requirement

## 6.1 Auth module

Frontend багаас шаардах deliverable:

- Register page
- Login page
- Session context/store
- Token refresh interceptor
- Logout action
- Protected route
- Admin route
- User profile/session badge

Acceptance criteria:

- Амжилттай login хийгдэхэд access token, refresh token хадгалагдана.
- App reload хийсний дараа session сэргээгдэнэ эсвэл refresh оролдоно.
- Refresh амжилтгүй бол session цэвэрлэгдэж login page руу шилжинэ.

## 6.2 Hotel module

Frontend багаас шаардах deliverable:

- Hotel list page
- Hotel search by name
- Hotel detail page
- Hotel rooms section

Acceptance criteria:

- Public хэрэглэгч login хийхгүйгээр ашиглаж чадна.
- Search нь `GET /api/hotels/search?name=` endpoint ашиглана.
- Hotel detail дээр room list харагдана.

## 6.3 Room module

Frontend багаас шаардах deliverable:

- Room list page
- Room detail page
- Available room search form

Acceptance criteria:

- Availability search нь `hotelId + checkin + checkout` ашиглана.
- `checkin`, `checkout` query param нэршлийг backend-тэй яг тааруулна.
- Available room screen нь хоосон үр дүнд зориулсан empty state-тай байна.

## 6.4 Booking module

Frontend багаас шаардах deliverable:

- Create booking form
- Own bookings list
- Booking detail
- Update booking
- Confirm booking
- Cancel booking
- Delete booking
- Admin booking list

### Booking request contract

Booking create/update дээр дараах талбаруудыг backend contract-оор явуулна.

- `userId` (optional, auth user-ээс resolve хийгдэж болно)
- `roomId`
- `checkinDate`
- `checkoutDate`
- `firstName`
- `lastName`
- `email`
- `phone`
- `guestCount`
- `specialRequests` (optional)
- `roomPrice` (optional)
- `status` (optional)

### Booking UX requirement

- `checkoutDate > checkinDate` client-side validation байна.
- `guestCount >= 1` validation байна.
- Form submit үед loading state байна.
- Response дээр ирсэн `bookingNumber`, `status`, `nights`, `roomPrice`, `serviceFee`, `totalPrice`-г UI дээр харуулна.
- Booking status badge нь дор хаяж `PENDING`, `CONFIRMED`, `CANCELLED`-г ялган үзүүлнэ.

### Critical mismatch warning

Одоогийн sample helper дээрх дараах бүтэц ашиглахыг хориглоно.

- `user: { id }`
- `room: { id }`
- `checkInDate`
- `checkOutDate`
- `totalPrice`

Booking API дээр эдгээр нэршил backend contract-тэй нийцэхгүй.

## 6.5 Wishlist module

Frontend багаас шаардах deliverable:

- Add to wishlist
- Wishlist list
- Exists check
- Remove by wishlist id
- Remove by `userId + roomId`
- Optional optimistic update with rollback

Acceptance criteria:

- Logged-in user л wishlist action хийж чадна.
- Own wishlist list л харагдана.
- Heart/favorite toggle нь exists API дээр тулгуурлаж ажиллана.
- Remove action амжилттай бол UI синк шинэчлэгдэнэ.

## 6.6 Notification module

Frontend багаас шаардах deliverable:

- Notification list
- Unread count badge
- Mark as read
- Mark all as read
- Delete notification

Acceptance criteria:

- Unread badge нь realtime биш байсан ч view refresh/state refresh дээр шинэчлэгдэнэ.
- Notification item бүр `createdAt` болон read state-тай харагдана.
- `POST /api/notifications/{id}/read` action UI дээр idempotent байдлаар ажиллана.

### Critical mismatch warning

Одоогийн frontend sample type дээр `isRead` гэж байгаа боловч backend response field нь `read` байна. Frontend дээр `read` field ашиглана.

## 6.7 Payment module

Frontend багаас шаардах deliverable:

- Create payment form
- Payment list
- Payment detail
- Update payment
- Delete payment
- Booking detail дотор payment summary/block

Acceptance criteria:

- Payment create request нь `bookingId`, `amount`, `paymentMethod` явуулна.
- `amount > 0` client validation байна.
- Response дээр ирсэн `status` болон `paymentDate` UI дээр ашиглагдана.

## 6.8 Admin module

Frontend багаас шаардах deliverable:

- Hotel create/update/delete
- Room create/update/delete
- Amenity create/update/delete
- Highlight create/update/delete
- User list/detail/update/delete

Acceptance criteria:

- Эдгээр screen зөвхөн admin role дээр харагдана.
- Non-admin хэрэглэгч deep link-ээр орж ирсэн ч access denied state үзүүлнэ.

## 7. Validation requirement

Frontend дээрх validation нь backend DTO rule-тэй таарсан байна.

### 7.1 Register

- `name` required
- `email` required, email format
- `password` required, min length 6
- `phone` optional

### 7.2 Login

- `email` required, email format
- `password` required

### 7.3 Hotel

- `name` required
- `startingPrice >= 0`

### 7.4 Room

- `hotelId` required for create
- `roomType` required
- `price >= 0`
- `capacity >= 1`
- `status` required

### 7.5 Booking

- `roomId` required
- `checkinDate` required, today-or-future
- `checkoutDate` required
- `firstName` required
- `lastName` required
- `email` required, valid email
- `phone` required
- `guestCount >= 1`

### 7.6 Wishlist

- `userId` required
- `roomId` required

### 7.7 Notification

- `userId` required
- `title` required
- `message` required
- `type` optional

### 7.8 Payment

- `bookingId` required
- `amount > 0`
- `paymentMethod` required

## 8. Error handling contract

Frontend баг нэг стандарт error parser ашиглана.

Backend error response shape:

- `timestamp`
- `status`
- `error`
- `code`
- `message`
- `path`
- `traceId`
- `validationErrors[]`

`validationErrors[]` item structure:

- `field`
- `message`
- `rejectedValue`

### Error handling requirement

- 400/422 validation алдааг field-level form message рүү map хийнэ.
- 401 дээр refresh policy ажиллуулна.
- 403 дээр permission denied state үзүүлнэ.
- 404 дээр empty/not found state байна.
- 429 эсвэл rate limit төрлийн алдаанд retry messaging өгнө.
- `traceId` байвал support/debug мэдээлэлд хадгална.

## 9. Environment ба deployment requirement

Frontend баг дараах environment contract-ийг баримтална.

- `NEXT_PUBLIC_API_URL` заавал configurable байна.
- Dev, staging, production base URL тусдаа config-тай байна.
- Hardcoded localhost URL production bundle-д үлдэхгүй.
- CORS whitelist шинэ domain хэрэгтэй бол backend багт урьдчилан мэдэгдэнэ.

Анхаарах зүйл:

- Одоогийн backend CORS нь зөвхөн `http://localhost:3000`, `http://localhost:5173`, `http://localhost:4200` origin-уудыг зөвшөөрч байна.
- Staging/prod frontend domain ашиглах бол backend config шинэчлэлт шаардлагатай.

## 10. Frontend handoff deliverables

Frontend баг дараах deliverable-уудыг өгнө.

- Domain-аар салгасан API client layer
- Type definitions
- Route guard implementation
- Error parser utility
- Auth/session store
- Public/user/admin module screen-үүд
- QA checklist result
- Contract mismatch list

## 11. Definition of done

Дараах нөхцөл биелсэн үед integration-г done гэж үзнэ.

- API field name болон response shape дээр backend-тэй mismatch байхгүй.
- Auth flow register/login/refresh/logout бүрэн ажиллаж байна.
- User/Admin route separation ажиллаж байна.
- Ownership-protected action-ууд зөв UI gating-тэй байна.
- Validation message form түвшинд зөв харагдаж байна.
- Loading, empty, error, success state бүр screen-үүд дээр байна.
- Build-time env config зөв ажиллаж байна.
- QA checklist-ийг амжилттай давсан байна.

## 12. QA acceptance checklist

Release-ийн өмнө frontend баг дараахийг шалгана.

- Login success flow
- Login invalid credential flow
- Refresh token flow
- Logout flow
- Expired access token handling
- 403 access denied rendering
- Hotel search success/empty/error state
- Available room search success/empty/error state
- Booking create/update/confirm/cancel/delete flow
- Wishlist add/remove/exists sync
- Notification unread badge, mark-read, mark-all-read
- Payment create/update/delete flow
- Admin CRUD screen permission check
- Validation error field mapping
- Trace/debug info capture where available

## 13. Backend-ээс frontend багт өгөх нэмэлт шаардлагатай материал

Backend баг дараахийг хавсаргаж өгөх нь зүйтэй.

- Шинэчилсэн API contract document
- Endpoint бүрийн request/response example
- Error response example
- Role/ownership matrix
- Environment matrix (dev/staging/prod)
- Known mismatch жагсаалт

## 14. Яг одоо нэн тэргүүнд хийх ажил

1. `docs/frontend/api.ts` sample helper-ийг backend-ийн бодит contract-тэй нийцүүлэн шинэчлэх.
2. Frontend дээр auth + refresh token lifecycle-ийг албан ёсоор үүрэгжүүлэх.
3. Error parser utility-г багийн стандарт болгох.
4. Role/ownership matrix-ийг UI routing requirement дээр тусгах.
5. Энэ requirement document-ийн дагуу Jira task breakdown гаргах.
