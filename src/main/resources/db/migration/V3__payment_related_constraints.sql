ALTER TABLE payments
    ALTER COLUMN amount SET NOT NULL;

ALTER TABLE payments
    ALTER COLUMN payment_method SET NOT NULL;

ALTER TABLE payments
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE bookings
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE payments
    ADD CONSTRAINT chk_payments_amount_positive
    CHECK (amount > 0);

ALTER TABLE payments
    ADD CONSTRAINT chk_payments_status_valid
    CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED'));

ALTER TABLE bookings
    ADD CONSTRAINT chk_bookings_status_valid
    CHECK (status IN ('NEW', 'CONFIRMED', 'PAID', 'CANCELLED'));
