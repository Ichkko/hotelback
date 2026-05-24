ALTER TABLE users
    ADD COLUMN global_role VARCHAR(20) NULL AFTER role;

UPDATE users
SET global_role = CASE
    WHEN role = 'ADMIN' THEN 'ADMIN'
    ELSE 'USER'
END
WHERE global_role IS NULL;

ALTER TABLE users
    MODIFY COLUMN global_role VARCHAR(20) NOT NULL;
