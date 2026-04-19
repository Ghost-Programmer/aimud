--changeset jeff:69

ALTER TABLE races ADD COLUMN starting_effects VARCHAR(255);

-- Append Fly (161) to flying races natively
UPDATE races SET starting_effects = '161' 
WHERE name IN ('Aarakocra', 'Fairy', 'Gargoyle', 'Harpy', 'Imp', 'Pixie', 'Pseudodragon', 'Sprite', 'Wyvern', 'Griffon', 'Pegasus', 'Dragon');

-- Append Darkvision +6 (3205) to dark-dwelling races natively
UPDATE races SET starting_effects = '3205'
WHERE name IN ('Drow', 'Duergar', 'Svirfneblin', 'Kobold', 'Goblin', 'Hobgoblin', 'Bugbear', 'Orc', 'Half-Orc', 'Tiefling', 'Vampire', 'Ghoul', 'Wraith', 'Mind Flayer', 'Drider', 'Deep Gnome', 'Troll');

-- Retroactive matching: Identify all active mobiles and inject the appropriate permanent effect binding into their arrays
INSERT INTO character_effects (character_id, effect_id, tick_count, created_at, modified_at, created_by, modified_by)
SELECT m.id, 161, -1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'
FROM mobiles m
JOIN races r ON m.race_id = r.id
WHERE r.name IN ('Aarakocra', 'Fairy', 'Gargoyle', 'Harpy', 'Imp', 'Pixie', 'Pseudodragon', 'Sprite', 'Wyvern', 'Griffon', 'Pegasus', 'Dragon')
AND NOT EXISTS (
    SELECT 1 FROM character_effects ce WHERE ce.character_id = m.id AND ce.effect_id = 161 AND ce.tick_count = -1
);

INSERT INTO character_effects (character_id, effect_id, tick_count, created_at, modified_at, created_by, modified_by)
SELECT m.id, 3205, -1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'
FROM mobiles m
JOIN races r ON m.race_id = r.id
WHERE r.name IN ('Drow', 'Duergar', 'Svirfneblin', 'Kobold', 'Goblin', 'Hobgoblin', 'Bugbear', 'Orc', 'Half-Orc', 'Tiefling', 'Vampire', 'Ghoul', 'Wraith', 'Mind Flayer', 'Drider', 'Deep Gnome', 'Troll')
AND NOT EXISTS (
    SELECT 1 FROM character_effects ce WHERE ce.character_id = m.id AND ce.effect_id = 3205 AND ce.tick_count = -1
);
