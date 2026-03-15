# Буудлын үнэ болон зураг мэдээлэл нэмэх заавар

Энэ төслийн одоогийн загвараар:
- **Үнэ (`price`) нь `rooms` хүснэгтэд** хадгалагддаг (өрөө тус бүрийн үнэ).
- **Зураг (`image_url`) нь `room_images` хүснэгтэд** хадгалагддаг (өрөө тус бүрийн зураг).
- Нэмэлтээр буудал дээр шууд харуулах **эхлэх үнэ (`starting_price`)** болон **cover зураг (`cover_image_url`)** талбарууд `hotels` хүснэгтэд нэмэгдсэн.

## 1) Буудал үүсгэх/засах үед starting price + cover image нэмэх

`POST /api/hotels` эсвэл `PUT /api/hotels/{id}`

```json
{
  "name": "Blue Sky Hotel",
  "address": "Ulaanbaatar",
  "aimag": "Ulaanbaatar",
  "phone": "99112233",
  "description": "Хотын төвд байрлалтай",
  "startingPrice": 180000,
  "coverImageUrl": "https://cdn.example.com/hotels/blue-sky-cover.jpg"
}
```

## 2) Өрөөний үнэ нэмэх (өрөө үүсгэх үед)

`POST /api/rooms`

```json
{
  "hotel": { "id": 1 },
  "roomType": "Deluxe",
  "price": 250000,
  "capacity": 2,
  "status": "AVAILABLE"
}
```

## 3) Өрөөний зураг нэмэх

`POST /api/room-images`

```json
{
  "room": { "id": 10 },
  "imageUrl": "https://cdn.example.com/rooms/10-main.jpg",
  "description": "Өрөөний үндсэн зураг"
}
```

## 4) SQL-ээр шууд нэмэх (сонголт)

```sql
-- Буудлын эхлэх үнэ + cover зураг
UPDATE hotels
SET starting_price = 180000,
    cover_image_url = 'https://cdn.example.com/hotels/blue-sky-cover.jpg',
    updated_at = NOW()
WHERE id = 1;

-- Өрөөний үнэ
INSERT INTO rooms (hotel_id, room_type, price, capacity, status, created_at, updated_at)
VALUES (1, 'Deluxe', 250000, 2, 'AVAILABLE', NOW(), NOW());

-- Өрөөний зураг
INSERT INTO room_images (room_id, image_url, description, created_at, updated_at)
VALUES (10, 'https://cdn.example.com/rooms/10-main.jpg', 'Өрөөний үндсэн зураг', NOW(), NOW());
```

> `spring.jpa.hibernate.ddl-auto=update` тохиргоотой тул апп эхлэхэд `hotels` хүснэгтэд шинэ баганууд автоматаар нэмэгдэнэ.
