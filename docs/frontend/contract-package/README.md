# Frontend integration contract package

Энэ package нь frontend integration эхлүүлэхэд шаардлагатай backend contract материалыг нэг дор цуглуулсан багц юм.

## Баталгаажуулсан гол зүйлс

- Auth response дээр `refreshToken` **байгаа**.
- Booking request нь nested биш, **flat field** бүтэцтэй.
- Notification response field нь `isRead` биш, **`read`** байна.

## Багц доторх файлууд

1. `openapi.yaml` — frontend багт ашиглах OpenAPI 3.0 contract.
2. `examples.md` — auth, booking, wishlist, notification, payment, error response жишээнүүд.
3. `role-ownership-matrix.md` — role ба ownership access matrix.
4. `env-matrix.md` — dev/staging/prod environment matrix.
5. `hotelback-postman-collection.json` — Postman collection.

## Integration note

- Auth endpoint-ууд public боловч logout дээр access token-ийг `Authorization: Bearer <token>` header-аар явуулахыг зөвлөж байна.
- Booking create дээр `userId` optional; өгөгдөөгүй бол backend current auth user-ийг resolve хийнэ.
- Booking update request дээр `roomId` болон `userId` байхгүй.
- Wishlist/notification endpoint-ууд ownership check-тэй.
- Payment endpoint-ууд authenticated боловч controller түвшинд ownership check одоогоор тусдаа хэрэгжээгүй; frontend дээр booking owner flow-оор хязгаарлаж ашиглах нь зөв.
