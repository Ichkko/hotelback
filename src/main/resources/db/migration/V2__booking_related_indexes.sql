CREATE INDEX idx_bookings_room_status_dates
    ON bookings (room_id, status, checkin_date, checkout_date);

CREATE INDEX idx_bookings_user_id
    ON bookings (user_id);

CREATE INDEX idx_bookings_room_id
    ON bookings (room_id);

CREATE INDEX idx_payments_booking_id
    ON payments (booking_id);
