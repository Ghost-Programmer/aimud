
--changeset jeff:1001
CREATE TABLE IF NOT EXISTS factions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT
);

INSERT INTO factions (name, description) VALUES
('Adventurers', 'Brave souls exploring the realms.'),
('Guards', 'Protectors of the civilized cities.'),
('Orcs', 'A brutal and warmongering faction.'),
('Undead', 'Mindless corpses anamatized by dark magic.');

ALTER TABLE mobiles ADD COLUMN faction_id BIGINT REFERENCES factions(id);

CREATE TABLE IF NOT EXISTS mobile_factions (
    mobile_id BIGINT NOT NULL REFERENCES mobiles(id) ON DELETE CASCADE,
    faction_id BIGINT NOT NULL REFERENCES factions(id) ON DELETE CASCADE,
    rating INT NOT NULL DEFAULT 50,
    PRIMARY KEY(mobile_id, faction_id)
);
