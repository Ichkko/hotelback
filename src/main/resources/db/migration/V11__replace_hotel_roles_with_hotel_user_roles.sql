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

-- hotel_owners → OWNER role
INSERT INTO hotel_user_roles (hotel_id, user_id, role)
SELECT hotel_id, user_id, 'OWNER'
FROM hotel_owners;

-- hotel_receptionists → RECEPTION role
INSERT INTO hotel_user_roles (hotel_id, user_id, role)
SELECT hotel_id, user_id, 'RECEPTION'
FROM hotel_receptionists;

DROP TABLE hotel_receptionists;
DROP TABLE hotel_owners;
