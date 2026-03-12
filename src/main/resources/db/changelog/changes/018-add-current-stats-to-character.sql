--liquibase formatted sql

--changeset jeff:18
ALTER TABLE characters ADD COLUMN current_hp INTEGER DEFAULT 0;
ALTER TABLE characters ADD COLUMN current_mana INTEGER DEFAULT 0;

-- Initialize current values (e.g., set to some default or calculate later)
-- For now we just default to 0, but ideally we might want to set them to maxHp if we had that stored.
-- Since maxHp is calculated dynamically, we can't set it here easily in SQL without duplicating logic.
-- However, we can set a reasonable default or leave at 0 and let the app handle it on load.
UPDATE characters SET current_hp = 100, current_mana = 50;
