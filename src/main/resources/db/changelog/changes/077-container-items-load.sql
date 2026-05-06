-- liquibase formatted sql
-- changeset jeff:077

-- Add container_items_load table for load-time nesting mapping
CREATE TABLE container_items_load (
    container_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    PRIMARY KEY (container_id, item_id)
);

-- Add 1 Million Gold (668) to Admin Chest (667)
INSERT INTO container_items_load (container_id, item_id) VALUES (667, 668);
