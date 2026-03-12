-- changeset 19:jeff
CREATE TABLE IF NOT EXISTS character_effects (
    id BIGSERIAL PRIMARY KEY,
    character_id BIGINT NOT NULL,
    effect_id BIGINT NOT NULL,
    tick_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    modified_by VARCHAR(255),
    CONSTRAINT fk_character_effects_character FOREIGN KEY (character_id) REFERENCES characters(id) ON DELETE CASCADE,
    CONSTRAINT fk_character_effects_effect FOREIGN KEY (effect_id) REFERENCES effects(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_character_effects_character_id ON character_effects(character_id);
CREATE INDEX IF NOT EXISTS idx_character_effects_effect_id ON character_effects(effect_id);
