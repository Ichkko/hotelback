CREATE TABLE hotel_receptionists (
    hotel_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (hotel_id, user_id),
    CONSTRAINT fk_hotel_receptionists_hotel FOREIGN KEY (hotel_id) REFERENCES hotels (id) ON DELETE CASCADE,
    CONSTRAINT fk_hotel_receptionists_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
