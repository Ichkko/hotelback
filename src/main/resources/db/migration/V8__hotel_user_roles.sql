CREATE TABLE hotel_user_roles (
    id       BIGINT      NOT NULL AUTO_INCREMENT,
    hotel_id BIGINT      NOT NULL,
    user_id  BIGINT      NOT NULL,
    role     VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_hotel_user_role (hotel_id, user_id, role),
    CONSTRAINT fk_hur_hotel FOREIGN KEY (hotel_id) REFERENCES hotels (id) ON DELETE CASCADE,
    CONSTRAINT fk_hur_user  FOREIGN KEY (user_id)  REFERENCES users (id)  ON DELETE CASCADE
);

-- Одоогийн owner_id өгөгдлийг hotel_user_roles-д шилжүүлэх
INSERT INTO hotel_user_roles (hotel_id, user_id, role)
SELECT id, owner_id, 'OWNER'
FROM hotels
WHERE owner_id IS NOT NULL;
