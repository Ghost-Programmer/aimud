--liquibase formatted sql

--changeset jeff:7
ALTER TABLE characters ADD COLUMN race_id BIGINT;
ALTER TABLE characters ADD COLUMN class_id BIGINT;
ALTER TABLE characters ADD CONSTRAINT fk_character_race FOREIGN KEY (race_id) REFERENCES races(id);
ALTER TABLE characters ADD CONSTRAINT fk_character_class FOREIGN KEY (class_id) REFERENCES character_classes(id);
