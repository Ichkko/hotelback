CREATE TABLE room_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    room_id BIGINT NOT NULL,
    category VARCHAR(120),
    label VARCHAR(255) NOT NULL,
    `value` VARCHAR(1000),
    display_order INT,
    CONSTRAINT fk_room_details_room FOREIGN KEY (room_id) REFERENCES rooms (id)
);

ALTER TABLE rooms
    DROP COLUMN room_details;
