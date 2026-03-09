--liquibase formatted sql

--changeset jeff:11
CREATE TABLE IF NOT EXISTS items (
    id SERIAL PRIMARY KEY,
    item_type VARCHAR(50) NOT NULL,
    wear_location VARCHAR(50),
    name VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS effects (
    id SERIAL PRIMARY KEY,
    item_id BIGINT REFERENCES items(id),
    effect_type VARCHAR(50) NOT NULL,
    modifier1 INTEGER NOT NULL DEFAULT 0,
    modifier2 INTEGER NOT NULL DEFAULT 0,
    modifier3 INTEGER NOT NULL DEFAULT 0,
    modifier4 INTEGER NOT NULL DEFAULT 0
);

ALTER TABLE characters ADD COLUMN head_id BIGINT REFERENCES items(id);
ALTER TABLE characters ADD COLUMN chest_id BIGINT REFERENCES items(id);
ALTER TABLE characters ADD COLUMN legs_id BIGINT REFERENCES items(id);
ALTER TABLE characters ADD COLUMN feet_id BIGINT REFERENCES items(id);
ALTER TABLE characters ADD COLUMN arms_id BIGINT REFERENCES items(id);
ALTER TABLE characters ADD COLUMN hands_id BIGINT REFERENCES items(id);
ALTER TABLE characters ADD COLUMN right_finger_id BIGINT REFERENCES items(id);
ALTER TABLE characters ADD COLUMN left_finger_id BIGINT REFERENCES items(id);
ALTER TABLE characters ADD COLUMN right_wrist_id BIGINT REFERENCES items(id);
ALTER TABLE characters ADD COLUMN left_wrist_id BIGINT REFERENCES items(id);
ALTER TABLE characters ADD COLUMN neck_id BIGINT REFERENCES items(id);
ALTER TABLE characters ADD COLUMN left_ear_id BIGINT REFERENCES items(id);
ALTER TABLE characters ADD COLUMN right_ear_id BIGINT REFERENCES items(id);
ALTER TABLE characters ADD COLUMN face_id BIGINT REFERENCES items(id);
ALTER TABLE characters ADD COLUMN waist_id BIGINT REFERENCES items(id);
ALTER TABLE characters ADD COLUMN primary_id BIGINT REFERENCES items(id);
ALTER TABLE characters ADD COLUMN offhand_id BIGINT REFERENCES items(id);
