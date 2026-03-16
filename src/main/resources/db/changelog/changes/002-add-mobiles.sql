-- liquibase formatted sql

-- changeset jeff:002-add-mobiles
CREATE TABLE IF NOT EXISTS mobiles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    strength INT NOT NULL DEFAULT 0,
    dexterity INT NOT NULL DEFAULT 0,
    constitution INT NOT NULL DEFAULT 0,
    intelligence INT NOT NULL DEFAULT 0,
    wisdom INT NOT NULL DEFAULT 0,
    charisma INT NOT NULL DEFAULT 0,
    race_id BIGINT,
    class_id BIGINT,
    current_room_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'system',
    modified_by VARCHAR(255) DEFAULT 'system',
    current_hp INTEGER DEFAULT 0,
    current_mana INTEGER DEFAULT 0,
    head_id BIGINT REFERENCES items(id),
    chest_id BIGINT REFERENCES items(id),
    legs_id BIGINT REFERENCES items(id),
    feet_id BIGINT REFERENCES items(id),
    arms_id BIGINT REFERENCES items(id),
    hands_id BIGINT REFERENCES items(id),
    right_finger_id BIGINT REFERENCES items(id),
    left_finger_id BIGINT REFERENCES items(id),
    right_wrist_id BIGINT REFERENCES items(id),
    left_wrist_id BIGINT REFERENCES items(id),
    neck_id BIGINT REFERENCES items(id),
    left_ear_id BIGINT REFERENCES items(id),
    right_ear_id BIGINT REFERENCES items(id),
    face_id BIGINT REFERENCES items(id),
    waist_id BIGINT REFERENCES items(id),
    primary_id BIGINT REFERENCES items(id),
    offhand_id BIGINT REFERENCES items(id)
);

ALTER TABLE rooms ADD COLUMN mobiles TEXT;
