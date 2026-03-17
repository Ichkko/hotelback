const API_BASE = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export type BookingStatus = "PENDING" | "CONFIRMED" | "CANCELLED";

export interface BookingPayload {
  user: { id: number };
  room: { id: number };
  checkInDate: string;
  checkOutDate: string;
  totalPrice: number;
  status?: BookingStatus;
}

export interface WishlistItem {
  id: number;
  userId: number;
  roomId: number;
  createdAt?: string;
}

export interface NotificationPayload {
  userId: number;
  title: string;
  message: string;
  type?: string;
}

export interface NotificationItem {
  id: number;
  userId: number;
  title: string;
  message: string;
  type: string;
  isRead: boolean;
  createdAt: string;
}

async function request<T>(input: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${input}`, {
    headers: { "Content-Type": "application/json", ...(init?.headers ?? {}) },
    ...init,
  });

  if (!res.ok) {
    throw new Error(await res.text());
  }

  return res.json() as Promise<T>;
}

export const api = {
  bookings: {
    create: async (payload: BookingPayload) => {
      return request<BookingPayload & { id: number }>("/api/bookings", {
        method: "POST",
        body: JSON.stringify(payload),
      });
    },
  },

  wishlist: {
    add: async (userId: number, roomId: number) => {
      return request<WishlistItem>("/api/wishlist", {
        method: "POST",
        body: JSON.stringify({ userId, roomId }),
      });
    },
    listByUser: async (userId: number) => {
      return request<WishlistItem[]>(`/api/wishlist/user/${userId}`);
    },
    exists: async (userId: number, roomId: number) => {
      return request<{ exists: boolean }>(`/api/wishlist/user/${userId}/room/${roomId}/exists`);
    },
  },

  notifications: {
    create: async (payload: NotificationPayload) => {
      return request<NotificationItem>("/api/notifications", {
        method: "POST",
        body: JSON.stringify(payload),
      });
    },
  },
};
