-- users.role is no longer mapped in the User entity.
-- global_role is the authoritative column since V13.
ALTER TABLE users DROP COLUMN role;
