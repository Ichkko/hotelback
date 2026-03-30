ALTER TABLE hotels
    ADD COLUMN owner_id BIGINT NULL;

ALTER TABLE hotels
    ADD CONSTRAINT fk_hotels_owner
        FOREIGN KEY (owner_id) REFERENCES users (id);
