--liquibase formatted sql

--changeset jeff:17
CREATE TABLE item_effects (
    item_id BIGINT REFERENCES items(id),
    effect_id BIGINT REFERENCES effects(id),
    PRIMARY KEY (item_id, effect_id)
);

INSERT INTO item_effects (item_id, effect_id)
SELECT item_id, id FROM effects WHERE item_id IS NOT NULL;

ALTER TABLE effects DROP COLUMN item_id;
