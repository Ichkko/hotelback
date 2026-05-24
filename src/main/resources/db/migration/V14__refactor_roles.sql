-- ══════════════════════════════════════════════════════════════════════
-- V14: Role architecture refactor
--
-- 1. Remove duplicate hotel_user_roles rows (keep oldest per hotel+user)
-- 2. Add new unique constraint (hotel_id, user_id) FIRST
--    so the FK on hotel_id still has an index to use
-- 3. Drop old constraint (hotel_id, user_id, role)
-- 4. Clean users.role column: remove hotel-specific values
-- ══════════════════════════════════════════════════════════════════════

-- Step 1: Remove duplicate rows — keep the one with the lowest id
DELETE FROM hotel_user_roles
WHERE id NOT IN (
    SELECT min_id FROM (
        SELECT MIN(id) AS min_id
        FROM hotel_user_roles
        GROUP BY hotel_id, user_id
    ) AS keep
);

-- Step 2: Add the new unique constraint FIRST
--   MySQL FK (fk_hur_hotel on hotel_id) needs a leftmost-column index.
--   uq_hotel_user satisfies that, so the old index can be dropped next.
ALTER TABLE hotel_user_roles ADD UNIQUE KEY uq_hotel_user (hotel_id, user_id);

-- Step 3: Now drop the old composite index (FK is covered by step 2)
ALTER TABLE hotel_user_roles DROP INDEX uq_hotel_user_role;

-- Step 4: Clean users.role — replace any hotel-specific value with USER
UPDATE users
SET role = 'USER'
WHERE role NOT IN ('ADMIN', 'USER')
   OR role IS NULL;
