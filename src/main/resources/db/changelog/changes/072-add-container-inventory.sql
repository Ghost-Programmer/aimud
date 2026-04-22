-- liquibase formatted sql
-- changeset jeff:072

ALTER TABLE character_inventory ADD COLUMN container_item_id BIGINT DEFAULT 0 NOT NULL;
ALTER TABLE character_inventory DROP CONSTRAINT character_inventory_pkey;
ALTER TABLE character_inventory ADD PRIMARY KEY (character_id, item_id, container_item_id);

ALTER TABLE mobile_inventory ADD COLUMN container_item_id BIGINT DEFAULT 0 NOT NULL;
ALTER TABLE mobile_inventory DROP CONSTRAINT mobile_inventory_pkey;
ALTER TABLE mobile_inventory ADD PRIMARY KEY (mobile_id, item_id, container_item_id);
