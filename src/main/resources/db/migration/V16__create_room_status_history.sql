CREATE TABLE room_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    room_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    note VARCHAR(500),
    CONSTRAINT fk_room_status_history_room FOREIGN KEY (room_id) REFERENCES rooms (id),
    CONSTRAINT chk_room_status_history_status_valid CHECK (status IN ('AVAILABLE', 'UNAVAILABLE', 'MAINTENANCE')),
    CONSTRAINT chk_room_status_history_date_range CHECK (end_date IS NULL OR end_date > start_date)
);

CREATE INDEX idx_room_status_history_room_dates
    ON room_status_history (room_id, start_date, end_date);
