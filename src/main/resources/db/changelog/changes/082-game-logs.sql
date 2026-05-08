CREATE TABLE game_logs (
    id SERIAL PRIMARY KEY,
    mobile_id BIGINT,
    is_world_log BOOLEAN DEFAULT FALSE,
    message TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_game_logs_mobile FOREIGN KEY (mobile_id) REFERENCES mobiles(id) ON DELETE CASCADE
);

ALTER TABLE mobiles ADD COLUMN is_world_log BOOLEAN DEFAULT FALSE;
ALTER TABLE quests ADD COLUMN is_world_event BOOLEAN DEFAULT FALSE;
