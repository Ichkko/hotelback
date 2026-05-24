ALTER TABLE rooms
    ADD COLUMN room_number VARCHAR(50);

ALTER TABLE rooms
    ADD COLUMN floor INT;

ALTER TABLE rooms
    ADD COLUMN wing VARCHAR(100);

ALTER TABLE rooms
    ADD COLUMN section VARCHAR(100);

ALTER TABLE rooms
    ADD COLUMN position_x DOUBLE;

ALTER TABLE rooms
    ADD COLUMN position_y DOUBLE;
