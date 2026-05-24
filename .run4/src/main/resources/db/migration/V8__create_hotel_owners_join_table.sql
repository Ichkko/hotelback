CREATE TABLE hotel_owners (
    hotel_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (hotel_id, user_id),
    CONSTRAINT fk_hotel_owners_hotel FOREIGN KEY (hotel_id) REFERENCES hotels (id) ON DELETE CASCADE,
    CONSTRAINT fk_hotel_owners_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

INSERT INTO hotel_owners (hotel_id, user_id)
SELECT h.id, h.owner_id
FROM hotels h
WHERE h.owner_id IS NOT NULL;
