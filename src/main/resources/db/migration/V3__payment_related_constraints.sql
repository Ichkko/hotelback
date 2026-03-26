UPDATE payments
SET amount = 0.01
WHERE amount IS NULL;

UPDATE payments
SET payment_method = 'UNKNOWN'
WHERE payment_method IS NULL;

UPDATE payments
SET status = 'PENDING'
WHERE status IS NULL;

UPDATE bookings
SET status = 'NEW'
WHERE status IS NULL;

ALTER TABLE payments
    MODIFY COLUMN amount DECIMAL(10, 2) NOT NULL;

ALTER TABLE payments
    MODIFY COLUMN payment_method VARCHAR(50) NOT NULL;

ALTER TABLE payments
    MODIFY COLUMN status VARCHAR(50) NOT NULL;

ALTER TABLE bookings
    MODIFY COLUMN status VARCHAR(50) NOT NULL;

ALTER TABLE payments
    ADD CONSTRAINT chk_payments_amount_positive
    CHECK (amount > 0);

ALTER TABLE payments
    ADD CONSTRAINT chk_payments_status_valid
    CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED'));

ALTER TABLE bookings
    ADD CONSTRAINT chk_bookings_status_valid
    CHECK (status IN ('NEW', 'CONFIRMED', 'PAID', 'CANCELLED'));
