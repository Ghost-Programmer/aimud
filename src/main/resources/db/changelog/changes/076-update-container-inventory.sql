-- liquibase formatted sql
-- changeset jeff:076

-- Add inventory_ids to items table
ALTER TABLE items ADD COLUMN inventory_ids TEXT;

-- Revert character_inventory container_item_id changes
ALTER TABLE character_inventory DROP CONSTRAINT character_inventory_pkey;
ALTER TABLE character_inventory DROP COLUMN container_item_id;
ALTER TABLE character_inventory ADD PRIMARY KEY (character_id, item_id);

-- Revert mobile_inventory container_item_id changes
ALTER TABLE mobile_inventory DROP CONSTRAINT mobile_inventory_pkey;
ALTER TABLE mobile_inventory DROP COLUMN container_item_id;
ALTER TABLE mobile_inventory ADD PRIMARY KEY (mobile_id, item_id);
