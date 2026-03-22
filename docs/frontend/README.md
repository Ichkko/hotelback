# Front-end API холбох загвар

`docs/frontend/api.ts` файл нь энэ backend-ийн дараах endpoint-уудтай холбогдоно.

- `POST /api/bookings`
- `POST /api/wishlist`
- `GET /api/wishlist/user/{userId}`
- `GET /api/wishlist/user/{userId}/room/{roomId}/exists`
- `POST /api/notifications`

## Ашиглах алхмууд

1. Front-end (Next.js) төсөлдөө `api.ts`-г хуулна.
2. `.env.local` дотор:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

3. Component-оос дуудах жишээ:

```ts
import { api } from "@/lib/api";

await api.bookings.create({
  user: { id: 1 },
  room: { id: 10 },
  checkInDate: "2026-04-01",
  checkOutDate: "2026-04-03",
  totalPrice: 250000,
});

const exists = await api.wishlist.exists(1, 10);
if (!exists.exists) {
  await api.wishlist.add(1, 10);
}

await api.notifications.create({
  userId: 1,
  title: "Захиалга баталгаажлаа",
  message: "Таны өрөөний захиалга амжилттай баталгаажлаа.",
  type: "BOOKING",
});
```


## Нэмэлт баримт бичиг

- `docs/frontend/requirements.md`: Frontend багт өгөх албан requirement document.

- `docs/frontend/contract-package/README.md`: Frontend integration-д зориулсан contract package overview.
- `docs/frontend/contract-package/openapi.yaml`: OpenAPI contract.
- `docs/frontend/contract-package/examples.md`: Request/response example багц.
- `docs/frontend/contract-package/role-ownership-matrix.md`: Role ба ownership matrix.
- `docs/frontend/contract-package/env-matrix.md`: Dev/Staging/Prod environment matrix.
- `docs/frontend/contract-package/hotelback-postman-collection.json`: Postman collection.
