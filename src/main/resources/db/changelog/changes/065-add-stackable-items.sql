-- liquibase formatted sql
-- changeset jeff:065

ALTER TABLE items ADD COLUMN stackable BOOLEAN DEFAULT FALSE;
ALTER TABLE items ADD COLUMN count INT DEFAULT 1 NOT NULL;

ALTER TABLE character_inventory ADD COLUMN item_count INT DEFAULT 1 NOT NULL;
ALTER TABLE mobile_inventory ADD COLUMN item_count INT DEFAULT 1 NOT NULL;

UPDATE items SET stackable = TRUE WHERE item_type = 'BANDAGE';
