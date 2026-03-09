--liquibase formatted sql

--changeset jeff:12
CREATE TABLE IF NOT EXISTS character_inventory (
    character_id BIGINT NOT NULL REFERENCES characters(id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    PRIMARY KEY (character_id, item_id)
);
