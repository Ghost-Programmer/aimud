--liquibase formatted sql

--changeset jeff:10
ALTER TABLE characters ADD COLUMN current_room_id BIGINT DEFAULT 1;
ALTER TABLE characters ADD CONSTRAINT fk_character_room FOREIGN KEY (current_room_id) REFERENCES rooms(id);
