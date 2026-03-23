UPDATE rooms
SET status = 'UNAVAILABLE'
WHERE UPPER(TRIM(status)) = 'BOOKED';

ALTER TABLE rooms
    ADD CONSTRAINT chk_rooms_status_valid
    CHECK (status IS NULL OR status IN ('AVAILABLE', 'UNAVAILABLE', 'MAINTENANCE'));
