--liquibase formatted sql

--changeset jeff:1
CREATE TABLE IF NOT EXISTS server_info (
    id SERIAL PRIMARY KEY,
    key VARCHAR(255) NOT NULL UNIQUE,
    value VARCHAR(255) NOT NULL
);

INSERT INTO server_info (key, value) VALUES ('db_status', 'R2DBC Connection Established!')
ON CONFLICT (key) DO UPDATE SET value = 'R2DBC Connection Established!';

CREATE TABLE IF NOT EXISTS server_settings (
    id SERIAL PRIMARY KEY,
    server_name VARCHAR(255) NOT NULL DEFAULT 'AI Mud',
    allow_new_user BOOLEAN NOT NULL DEFAULT true,
    maintenance BOOLEAN NOT NULL DEFAULT false,
    maintenance_text VARCHAR(255) NOT NULL DEFAULT 'Undergoing Maintenance'
);

INSERT INTO server_settings (id, server_name, allow_new_user, maintenance, maintenance_text)
VALUES (1, 'AI Mud', true, false, 'Undergoing Maintenance')
ON CONFLICT (id) DO NOTHING;

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'MUD_USER'
);

--changeset jeff:2
ALTER TABLE users ADD COLUMN locked BOOLEAN NOT NULL DEFAULT false;

--changeset jeff:3
CREATE TABLE IF NOT EXISTS characters (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    strength INT NOT NULL DEFAULT 0,
    dexterity INT NOT NULL DEFAULT 0,
    constitution INT NOT NULL DEFAULT 0,
    intelligence INT NOT NULL DEFAULT 0,
    wisdom INT NOT NULL DEFAULT 0,
    charisma INT NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

--changeset jeff:4
CREATE TABLE IF NOT EXISTS races (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(300) NOT NULL,
    strength_mod INT NOT NULL DEFAULT 0,
    intelligence_mod INT NOT NULL DEFAULT 0,
    wisdom_mod INT NOT NULL DEFAULT 0,
    charisma_mod INT NOT NULL DEFAULT 0,
    dexterity_mod INT NOT NULL DEFAULT 0,
    constitution_mod INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS character_classes (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(300) NOT NULL,
    strength_mod INT NOT NULL DEFAULT 0,
    intelligence_mod INT NOT NULL DEFAULT 0,
    wisdom_mod INT NOT NULL DEFAULT 0,
    charisma_mod INT NOT NULL DEFAULT 0,
    dexterity_mod INT NOT NULL DEFAULT 0,
    constitution_mod INT NOT NULL DEFAULT 0
);

INSERT INTO character_classes (id, name, description, strength_mod, intelligence_mod, wisdom_mod, charisma_mod, dexterity_mod, constitution_mod) VALUES
(1,'Cleric', 'A priestly champion who wields divine magic in service of a higher power.', 0, 0, 2, 0, 0, 0),
(2,'Druid', 'A priest of the Old Faith, wielding the powers of nature and adopting animal forms.', 0, 0, 2, 0, 0, 0),
(3,'Fighter', 'A master of martial combat, skilled with a variety of weapons and armor.', 2, 0, 0, 0, 0, 1),
(4,'Rogue', 'A scoundrel who uses stealth and trickery to overcome obstacles and enemies.', 0, 0, 0, 0, 2, 0),
(5,'Paladin', 'A holy warrior bound to a sacred oath.', 2, 0, 0, 1, 0, 0),
(6,'Ranger', 'A fighter that specializes in ranged combant.', 1, 0, 0, 1, 1, 0),
(7,'Wizard', 'A scholarly magic-user capable of manipulating the structures of reality.', 0, 2, 0, 0, 0, 0);

--changeset jeff:5
ALTER TABLE races ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE character_classes ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT false;

--changeset jeff:6
INSERT INTO races (name, description, strength_mod, intelligence_mod, wisdom_mod, charisma_mod, dexterity_mod, constitution_mod) VALUES
('Elf', 'A magical people of otherworldly grace, living in the world but not entirely part of it.', 0, 1, 0, 0, 2, 0),
('Halfling', 'The diminutive halflings survive in a world full of larger creatures by avoiding notice or, barring that, avoiding offense.', 0, 0, 0, 0, 2, 0),
('Human', 'Humans are the most adaptable and ambitious people among the common races.', 1, 1, 1, 1, 1, 1),
('Gnome', 'A constant hum of busy activity permeates the warrens and neighborhoods where gnomes form their close-knit communities.', 0, 2, 0, 0, 0, 0),
('Dwarf', 'Bold and hardy, dwarves are known as skilled warriors, miners, and workers of stone and metal.', 0, 0, 0, 0, 0, 2),
('Orc', 'Orcs are savage raiders and pillagers with stooped postures, low foreheads, and piggish faces.', 2, 0, 0, 0, 0, 1);

--changeset jeff:7
ALTER TABLE characters ADD COLUMN race_id BIGINT;
ALTER TABLE characters ADD COLUMN class_id BIGINT;
ALTER TABLE characters ADD CONSTRAINT fk_character_race FOREIGN KEY (race_id) REFERENCES races(id);
ALTER TABLE characters ADD CONSTRAINT fk_character_class FOREIGN KEY (class_id) REFERENCES character_classes(id);

--changeset jeff:8
CREATE TABLE IF NOT EXISTS rooms (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    room_type VARCHAR(50) NOT NULL,
    north_id BIGINT,
    south_id BIGINT,
    east_id BIGINT,
    west_id BIGINT,
    up_id BIGINT,
    down_id BIGINT,
    north_door BOOLEAN DEFAULT false,
    south_door BOOLEAN DEFAULT false,
    east_door BOOLEAN DEFAULT false,
    west_door BOOLEAN DEFAULT false,
    up_door BOOLEAN DEFAULT false,
    down_door BOOLEAN DEFAULT false,
    north_door_open BOOLEAN DEFAULT false,
    south_door_open BOOLEAN DEFAULT false,
    east_door_open BOOLEAN DEFAULT false,
    west_door_open BOOLEAN DEFAULT false,
    up_door_open BOOLEAN DEFAULT false,
    down_door_open BOOLEAN DEFAULT false
);

--changeset jeff:9
INSERT INTO rooms (id, name, description, room_type) VALUES
(1, 'Mud Entrance', 'You stand at the grand entrance of the AI MUD. This is the primary gathering place for adventurers starting their journey.', 'CITY')
ON CONFLICT (id) DO NOTHING;

--changeset jeff:10
ALTER TABLE characters ADD COLUMN current_room_id BIGINT DEFAULT 1;
ALTER TABLE characters ADD CONSTRAINT fk_character_room FOREIGN KEY (current_room_id) REFERENCES rooms(id);

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

--changeset jeff:12
CREATE TABLE IF NOT EXISTS character_inventory (
    character_id BIGINT NOT NULL REFERENCES characters(id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    PRIMARY KEY (character_id, item_id)
);

--changeset jeff:add-ai-system-prompt
ALTER TABLE server_settings ADD COLUMN ai_system_prompt TEXT;

UPDATE server_settings SET ai_system_prompt = 'You are an Expert Multi-User Dungeon World Builder. You have access to MCP tools for creating rooms, items, and effects for items. Use these tools to help the user build their world.

When creating rooms, use the following RoomTypes: INDOORS, CITY, FIELD, FOREST, HILLS, MOUNTAIN, DESERT, ARCTIC, SWAMP, WATER_SURFACE, UNDERWATER, AIR, UNDERGROUND_CAVE, UNDERGROUND_DUNGEON.

When creating items, use the following ItemTypes: WEAPON, TWO_HANDED_WEAPON, ARMOR, FOOD, DRINK, POTION, SCROLL, MONEY, WAND, QUEST, KEY, LIGHT, CONTAINER, TRASH, MISC.
For WearLocations, use: HEAD, CHEST, LEGS, FEET, ARMS, HANDS, RIGHT_FINGER, LEFT_FINGER, RIGHT_WRIST, LEFT_WRIST, NECK, LEFT_EAR, RIGHT_EAR, FACE, WAIST, PRIMARY, OFFHAND, NONE.

When creating effects, use the following EffectTypes:
- Damage: SLASHING_DAMAGE, BASHING_DAMAGE, PIERCING_DAMAGE, FIRE_DAMAGE, COLD_DAMAGE, SONIC_DAMAGE, POISON_DAMAGE, ELECTRICAL_DAMAGE (Modifiers: Number of Dice, Size of Dice)
- Stats: STRENGTH, DEXTERITY, CONSTITUTION, INTELLIGENCE, WISDOM, CHARISMA (Modifier: Amount)
- Combat: PHYSICAL_ATTACK, MAGIC_ATTACK, MAGIC_RESIST, DODGE, CRITICAL_HIT, ARMOR (Modifier: Amount)
- Regen: HP_REGEN, MANA_REGEN (Modifier: Amount)
- Status: FLY, WATER_BREATHING, INVISIBLE (No modifiers)';

--changeset jeff:14 validCheckSum:ANY
ALTER TABLE users ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users ADD COLUMN modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users ADD COLUMN created_by VARCHAR(255) DEFAULT 'system';
ALTER TABLE users ADD COLUMN modified_by VARCHAR(255) DEFAULT 'system';

ALTER TABLE characters ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE characters ADD COLUMN modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE characters ADD COLUMN created_by VARCHAR(255) DEFAULT 'system';
ALTER TABLE characters ADD COLUMN modified_by VARCHAR(255) DEFAULT 'system';

ALTER TABLE races ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE races ADD COLUMN modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE races ADD COLUMN created_by VARCHAR(255) DEFAULT 'system';
ALTER TABLE races ADD COLUMN modified_by VARCHAR(255) DEFAULT 'system';

ALTER TABLE character_classes ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE character_classes ADD COLUMN modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE character_classes ADD COLUMN created_by VARCHAR(255) DEFAULT 'system';
ALTER TABLE character_classes ADD COLUMN modified_by VARCHAR(255) DEFAULT 'system';

ALTER TABLE rooms ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE rooms ADD COLUMN modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE rooms ADD COLUMN created_by VARCHAR(255) DEFAULT 'system';
ALTER TABLE rooms ADD COLUMN modified_by VARCHAR(255) DEFAULT 'system';

ALTER TABLE items ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE items ADD COLUMN modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE items ADD COLUMN created_by VARCHAR(255) DEFAULT 'system';
ALTER TABLE items ADD COLUMN modified_by VARCHAR(255) DEFAULT 'system';

ALTER TABLE effects ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE effects ADD COLUMN modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE effects ADD COLUMN created_by VARCHAR(255) DEFAULT 'system';
ALTER TABLE effects ADD COLUMN modified_by VARCHAR(255) DEFAULT 'system';

ALTER TABLE server_settings ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE server_settings ADD COLUMN modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE server_settings ADD COLUMN created_by VARCHAR(255) DEFAULT 'system';
ALTER TABLE server_settings ADD COLUMN modified_by VARCHAR(255) DEFAULT 'system';

ALTER TABLE server_info ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE server_info ADD COLUMN modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE server_info ADD COLUMN created_by VARCHAR(255) DEFAULT 'system';
ALTER TABLE server_info ADD COLUMN modified_by VARCHAR(255) DEFAULT 'system';

--changeset jeff:15
ALTER TABLE users ALTER COLUMN created_at TYPE TIMESTAMP;
ALTER TABLE users ALTER COLUMN modified_at TYPE TIMESTAMP;

ALTER TABLE characters ALTER COLUMN created_at TYPE TIMESTAMP;
ALTER TABLE characters ALTER COLUMN modified_at TYPE TIMESTAMP;

ALTER TABLE races ALTER COLUMN created_at TYPE TIMESTAMP;
ALTER TABLE races ALTER COLUMN modified_at TYPE TIMESTAMP;

ALTER TABLE character_classes ALTER COLUMN created_at TYPE TIMESTAMP;
ALTER TABLE character_classes ALTER COLUMN modified_at TYPE TIMESTAMP;

ALTER TABLE rooms ALTER COLUMN created_at TYPE TIMESTAMP;
ALTER TABLE rooms ALTER COLUMN modified_at TYPE TIMESTAMP;

ALTER TABLE items ALTER COLUMN created_at TYPE TIMESTAMP;
ALTER TABLE items ALTER COLUMN modified_at TYPE TIMESTAMP;

ALTER TABLE effects ALTER COLUMN created_at TYPE TIMESTAMP;
ALTER TABLE effects ALTER COLUMN modified_at TYPE TIMESTAMP;

ALTER TABLE server_settings ALTER COLUMN created_at TYPE TIMESTAMP;
ALTER TABLE server_settings ALTER COLUMN modified_at TYPE TIMESTAMP;

ALTER TABLE server_info ALTER COLUMN created_at TYPE TIMESTAMP;
ALTER TABLE server_info ALTER COLUMN modified_at TYPE TIMESTAMP;

-- changeset jeff:16
UPDATE server_settings
SET ai_system_prompt = 'You are an Expert Multi-User Dungeon World Builder.\n\nGUIDELINES:\n1. When a user asks to create something, use the appropriate MCP tools.\n2. To associate an effect with an item, first create the item using createItem to obtain its ID, then use createEffect setting the itemId field.\n3. Rooms have a name, description, and type (e.g., CITY, FIELD, FOREST, WATER, etc.).\n4. Items have a name, description, type (e.g., WEAPON, ARMOR, LIGHT, POTION), and wear location (e.g., HEAD, TORSO, ARMS, LEGS, etc.).\n5. Weapons MUST have damage effects. Use EffectTypes like SLASHING_DAMAGE, PIERCING_DAMAGE, or BASHING_DAMAGE. Set modifier1 to the number of dice and modifier2 to the size of the dice (e.g., 2d6 means modifier1=2, modifier2=6).\n6. Items can have stat modifiers. Use EffectTypes like STRENGTH, DEXTERITY, ARMOR, etc., and set modifier1 to the bonus amount.\n7. If you need more information to create an object, ask the user for clarification.\n8. Always check existing content if the user refers to it, using the retrieval tools.\n\nYou have access to the following tool categories:\n- Room Management: createRoom, updateRoom, getRoom, getAllRooms\n- Item Management: createItem, updateItem, getItem, getAllItems\n- Effect Management: createEffect, updateEffect, getEffect, getEffectsByItem\n\nBe creative but consistent with MUD conventions.'
WHERE id = 1;

--changeset jeff:17
CREATE TABLE item_effects (
    item_id BIGINT REFERENCES items(id),
    effect_id BIGINT REFERENCES effects(id),
    PRIMARY KEY (item_id, effect_id)
);

INSERT INTO item_effects (item_id, effect_id)
SELECT item_id, id FROM effects WHERE item_id IS NOT NULL;

ALTER TABLE effects DROP COLUMN item_id;

--changeset jeff:18
ALTER TABLE characters ADD COLUMN current_hp INTEGER DEFAULT 0;
ALTER TABLE characters ADD COLUMN current_mana INTEGER DEFAULT 0;

-- Initialize current values (e.g., set to some default or calculate later)
-- For now we just default to 0, but ideally we might want to set them to maxHp if we had that stored.
-- Since maxHp is calculated dynamically, we can't set it here easily in SQL without duplicating logic.
-- However, we can set a reasonable default or leave at 0 and let the app handle it on load.
UPDATE characters SET current_hp = 100, current_mana = 50;

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

-- changeset jeff:20
ALTER TABLE effects ADD COLUMN name VARCHAR(255);

--changeset jeff:21
-- Insert default rooms
INSERT INTO rooms (id, name, description, room_type, north_id, south_id, east_id, west_id) VALUES
(2, 'The Great Hall', 'A vast, echoing hall with marble floors and high vaulted ceilings.', 'INDOORS', 3, 4, 5, 1),
(3, 'Library of Ancient Wisdom', 'Rows of dusty bookshelves line the walls of this quiet chamber.', 'INDOORS', NULL, 2, NULL, NULL),
(4, 'The Alchemist''s Workshop', 'The air is thick with the smell of strange chemicals and bubbling potions.', 'INDOORS', 2, NULL, NULL, NULL),
(5, 'The Great Hall', 'A portal to the east shows a city on the other side.', 'INDOORS', NULL, NULL, NULL, 2)
ON CONFLICT (id) DO NOTHING;

-- Update Mud Entrance to connect to The Great Hall
UPDATE rooms SET north_id = 2 WHERE id = 1;

-- Insert default effects
INSERT INTO effects (id, name, effect_type, modifier1, modifier2, modifier3, modifier4) VALUES
(1, 'Strength +1', 'STRENGTH', 1, 0, 0, 0),
(2, 'Strength +2', 'STRENGTH', 2, 0, 0, 0),
(3, 'Strength +3', 'STRENGTH', 3, 0, 0, 0),
(4, 'Strength +4', 'STRENGTH', 4, 0, 0, 0),
(5, 'Strength +5', 'STRENGTH', 5, 0, 0, 0),
(6, 'Strength +6', 'STRENGTH', 6, 0, 0, 0),
(7, 'Strength +7', 'STRENGTH', 7, 0, 0, 0),
(8, 'Strength +8', 'STRENGTH', 8, 0, 0, 0),
(9, 'Strength +9', 'STRENGTH', 9, 0, 0, 0),
(10, 'Strength +10', 'STRENGTH', 10, 0, 0, 0),
(11, 'Strength +15', 'STRENGTH', 15, 0, 0, 0),
(12, 'Strength +20', 'STRENGTH', 20, 0, 0, 0),
(13, 'Strength +25', 'STRENGTH', 25, 0, 0, 0),
(14, 'Dexterity +1', 'DEXTERITY', 1, 0, 0, 0),
(15, 'Dexterity +2', 'DEXTERITY', 2, 0, 0, 0),
(16, 'Dexterity +3', 'DEXTERITY', 3, 0, 0, 0),
(17, 'Dexterity +4', 'DEXTERITY', 4, 0, 0, 0),
(18, 'Dexterity +5', 'DEXTERITY', 5, 0, 0, 0),
(19, 'Dexterity +6', 'DEXTERITY', 6, 0, 0, 0),
(20, 'Dexterity +7', 'DEXTERITY', 7, 0, 0, 0),
(21, 'Dexterity +8', 'DEXTERITY', 8, 0, 0, 0),
(22, 'Dexterity +9', 'DEXTERITY', 9, 0, 0, 0),
(23, 'Dexterity +10', 'DEXTERITY', 10, 0, 0, 0),
(24, 'Dexterity +15', 'DEXTERITY', 15, 0, 0, 0),
(25, 'Dexterity +20', 'DEXTERITY', 20, 0, 0, 0),
(26, 'Dexterity +25', 'DEXTERITY', 25, 0, 0, 0),
(27, 'Intelligence +1', 'INTELLIGENCE', 1, 0, 0, 0),
(28, 'Intelligence +2', 'INTELLIGENCE', 2, 0, 0, 0),
(29, 'Intelligence +3', 'INTELLIGENCE', 3, 0, 0, 0),
(30, 'Intelligence +4', 'INTELLIGENCE', 4, 0, 0, 0),
(31, 'Intelligence +5', 'INTELLIGENCE', 5, 0, 0, 0),
(32, 'Intelligence +6', 'INTELLIGENCE', 6, 0, 0, 0),
(33, 'Intelligence +7', 'INTELLIGENCE', 7, 0, 0, 0),
(34, 'Intelligence +8', 'INTELLIGENCE', 8, 0, 0, 0),
(35, 'Intelligence +9', 'INTELLIGENCE', 9, 0, 0, 0),
(36, 'Intelligence +10', 'INTELLIGENCE', 10, 0, 0, 0),
(37, 'Intelligence +15', 'INTELLIGENCE', 15, 0, 0, 0),
(38, 'Intelligence +20', 'INTELLIGENCE', 20, 0, 0, 0),
(39, 'Intelligence +25', 'INTELLIGENCE', 25, 0, 0, 0),
(40, 'Wisdom +1', 'WISDOM', 1, 0, 0, 0),
(41, 'Wisdom +2', 'WISDOM', 2, 0, 0, 0),
(42, 'Wisdom +3', 'WISDOM', 3, 0, 0, 0),
(43, 'Wisdom +4', 'WISDOM', 4, 0, 0, 0),
(44, 'Wisdom +5', 'WISDOM', 5, 0, 0, 0),
(45, 'Wisdom +6', 'WISDOM', 6, 0, 0, 0),
(46, 'Wisdom +7', 'WISDOM', 7, 0, 0, 0),
(47, 'Wisdom +8', 'WISDOM', 8, 0, 0, 0),
(48, 'Wisdom +9', 'WISDOM', 9, 0, 0, 0),
(49, 'Wisdom +10', 'WISDOM', 10, 0, 0, 0),
(50, 'Wisdom +15', 'WISDOM', 15, 0, 0, 0),
(51, 'Wisdom +20', 'WISDOM', 20, 0, 0, 0),
(52, 'Wisdom +25', 'WISDOM', 25, 0, 0, 0),
(53, 'Charisma +1', 'CHARISMA', 1, 0, 0, 0),
(54, 'Charisma +2', 'CHARISMA', 2, 0, 0, 0),
(55, 'Charisma +3', 'CHARISMA', 3, 0, 0, 0),
(56, 'Charisma +4', 'CHARISMA', 4, 0, 0, 0),
(57, 'Charisma +5', 'CHARISMA', 5, 0, 0, 0),
(58, 'Charisma +6', 'CHARISMA', 6, 0, 0, 0),
(59, 'Charisma +7', 'CHARISMA', 7, 0, 0, 0),
(60, 'Charisma +8', 'CHARISMA', 8, 0, 0, 0),
(61, 'Charisma +9', 'CHARISMA', 9, 0, 0, 0),
(62, 'Charisma +10', 'CHARISMA', 10, 0, 0, 0),
(63, 'Charisma +15', 'CHARISMA', 15, 0, 0, 0),
(64, 'Charisma +20', 'CHARISMA', 20, 0, 0, 0),
(65, 'Charisma +25', 'CHARISMA', 25, 0, 0, 0),
(66, 'Constitution +1', 'CONSTITUTION', 1, 0, 0, 0),
(67, 'Constitution +2', 'CONSTITUTION', 2, 0, 0, 0),
(68, 'Constitution +3', 'CONSTITUTION', 3, 0, 0, 0),
(69, 'Constitution +4', 'CONSTITUTION', 4, 0, 0, 0),
(70, 'Constitution +5', 'CONSTITUTION', 5, 0, 0, 0),
(71, 'Constitution +6', 'CONSTITUTION', 6, 0, 0, 0),
(72, 'Constitution +7', 'CONSTITUTION', 7, 0, 0, 0),
(73, 'Constitution +8', 'CONSTITUTION', 8, 0, 0, 0),
(74, 'Constitution +9', 'CONSTITUTION', 9, 0, 0, 0),
(75, 'Constitution +10', 'CONSTITUTION', 10, 0, 0, 0),
(76, 'Constitution +15', 'CONSTITUTION', 15, 0, 0, 0),
(77, 'Constitution +20', 'CONSTITUTION', 20, 0, 0, 0),
(78, 'Constitution +25', 'CONSTITUTION', 25, 0, 0, 0),
(79, 'Armor +1', 'ARMOR', 1, 0, 0, 0),
(80, 'Armor +2', 'ARMOR', 2, 0, 0, 0),
(81, 'Armor +3', 'ARMOR', 3, 0, 0, 0),
(82, 'Armor +4', 'ARMOR', 4, 0, 0, 0),
(83, 'Armor +5', 'ARMOR', 5, 0, 0, 0),
(84, 'Armor +6', 'ARMOR', 6, 0, 0, 0),
(85, 'Armor +7', 'ARMOR', 7, 0, 0, 0),
(86, 'Armor +8', 'ARMOR', 8, 0, 0, 0),
(87, 'Armor +9', 'ARMOR', 9, 0, 0, 0),
(88, 'Armor +10', 'ARMOR', 10, 0, 0, 0),
(89, 'Armor +15', 'ARMOR', 15, 0, 0, 0),
(90, 'Armor +20', 'ARMOR', 20, 0, 0, 0),
(91, 'Armor +25', 'ARMOR', 25, 0, 0, 0),
(92, 'HP Regen +1', 'HP_REGEN', 1, 0, 0, 0),
(93, 'HP Regen +2', 'HP_REGEN', 2, 0, 0, 0),
(94, 'HP Regen +3', 'HP_REGEN', 3, 0, 0, 0),
(95, 'HP Regen +4', 'HP_REGEN', 4, 0, 0, 0),
(96, 'HP Regen +5', 'HP_REGEN', 5, 0, 0, 0),
(97, 'HP Regen +6', 'HP_REGEN', 6, 0, 0, 0),
(98, 'HP Regen +7', 'HP_REGEN', 7, 0, 0, 0),
(99, 'HP Regen +8', 'HP_REGEN', 8, 0, 0, 0),
(100, 'HP Regen +9', 'HP_REGEN', 9, 0, 0, 0),
(101, 'HP Regen +10', 'HP_REGEN', 10, 0, 0, 0),
(102, 'HP Regen +15', 'HP_REGEN', 15, 0, 0, 0),
(103, 'HP Regen +20', 'HP_REGEN', 20, 0, 0, 0),
(104, 'HP Regen +25', 'HP_REGEN', 25, 0, 0, 0),
(105, 'Mana Regen +1', 'MANA_REGEN', 1, 0, 0, 0),
(106, 'Mana Regen +2', 'MANA_REGEN', 2, 0, 0, 0),
(107, 'Mana Regen +3', 'MANA_REGEN', 3, 0, 0, 0),
(108, 'Mana Regen +4', 'MANA_REGEN', 4, 0, 0, 0),
(109, 'Mana Regen +5', 'MANA_REGEN', 5, 0, 0, 0),
(110, 'Mana Regen +6', 'MANA_REGEN', 6, 0, 0, 0),
(111, 'Mana Regen +7', 'MANA_REGEN', 7, 0, 0, 0),
(112, 'Mana Regen +8', 'MANA_REGEN', 8, 0, 0, 0),
(113, 'Mana Regen +9', 'MANA_REGEN', 9, 0, 0, 0),
(114, 'Mana Regen +10', 'MANA_REGEN', 10, 0, 0, 0),
(115, 'Mana Regen +15', 'MANA_REGEN', 15, 0, 0, 0),
(116, 'Mana Regen +20', 'MANA_REGEN', 20, 0, 0, 0),
(117, 'Mana Regen +25', 'MANA_REGEN', 25, 0, 0, 0),
( 118, 'Physical Attack +1', 'PHYSICAL_ATTACK', 1, 0, 0, 0),
( 119, 'Physical Attack +2', 'PHYSICAL_ATTACK', 2, 0, 0, 0),
( 120, 'Physical Attack +3', 'PHYSICAL_ATTACK', 3, 0, 0, 0),
( 121, 'Physical Attack +4', 'PHYSICAL_ATTACK', 4, 0, 0, 0),
( 122, 'Physical Attack +5', 'PHYSICAL_ATTACK', 5, 0, 0, 0),
( 123, 'Physical Attack +6', 'PHYSICAL_ATTACK', 6, 0, 0, 0),
( 124, 'Physical Attack +7', 'PHYSICAL_ATTACK', 7, 0, 0, 0),
( 125, 'Physical Attack +8', 'PHYSICAL_ATTACK', 8, 0, 0, 0),
( 126, 'Physical Attack +9', 'PHYSICAL_ATTACK', 9, 0, 0, 0),
( 127, 'Physical Attack +10', 'PHYSICAL_ATTACK', 10, 0, 0, 0),
( 128, 'Physical Attack +15', 'PHYSICAL_ATTACK', 15, 0, 0, 0),
( 129, 'Physical Attack +20', 'PHYSICAL_ATTACK', 20, 0, 0, 0),
( 130, 'Physical Attack +25', 'PHYSICAL_ATTACK', 25, 0, 0, 0),
( 131, 'Magical Attack +1', 'MAGIC_ATTACK', 1, 0, 0, 0),
( 132, 'Magical Attack +2', 'MAGIC_ATTACK', 2, 0, 0, 0),
( 133, 'Magical Attack +3', 'MAGIC_ATTACK', 3, 0, 0, 0),
( 134, 'Magical Attack +4', 'MAGIC_ATTACK', 4, 0, 0, 0),
( 135, 'Magical Attack +5', 'MAGIC_ATTACK', 5, 0, 0, 0),
( 136, 'Magical Attack +6', 'MAGIC_ATTACK', 6, 0, 0, 0),
( 137, 'Magical Attack +7', 'MAGIC_ATTACK', 7, 0, 0, 0),
( 138, 'Magical Attack +8', 'MAGIC_ATTACK', 8, 0, 0, 0),
( 139, 'Magical Attack +9', 'MAGIC_ATTACK', 9, 0, 0, 0),
( 140, 'Magical Attack +10', 'MAGIC_ATTACK', 10, 0, 0, 0),
( 141, 'Magical Attack +15', 'MAGIC_ATTACK', 15, 0, 0, 0),
( 142, 'Magical Attack +20', 'MAGIC_ATTACK', 20, 0, 0, 0),
( 143, 'Magical Attack +25', 'MAGIC_ATTACK', 25, 0, 0, 0),
( 144, 'Magic Resist +1', 'MAGIC_RESIST', 1, 0, 0, 0),
( 145, 'Magic Resist +5', 'MAGIC_RESIST', 5, 0, 0, 0),
( 146, 'Magic Resist +10', 'MAGIC_RESIST', 10, 0, 0, 0),
( 147, 'Dodge +1', 'DODGE', 1, 0, 0, 0),
( 148, 'Dodge +5', 'DODGE', 5, 0, 0, 0),
( 149, 'Dodge +10', 'DODGE', 10, 0, 0, 0),
( 150, 'Critical Hit +1', 'CRITICAL_HIT', 1, 0, 0, 0),
( 151, 'Critical Hit +5', 'CRITICAL_HIT', 5, 0, 0, 0),
( 152, 'Critical Hit +10', 'CRITICAL_HIT', 10, 0, 0, 0),
( 153, 'Bashing Damage 1d6', 'BASHING_DAMAGE', 1, 6, 0, 0),
( 154, 'Piercing Damage 1d6', 'PIERCING_DAMAGE', 1, 6, 0, 0),
( 155, 'Slashing Damage 1d6', 'SLASHING_DAMAGE', 1, 6, 0, 0),
( 156, 'Fire Damage 1d6', 'FIRE_DAMAGE', 1, 6, 0, 0),
( 157, 'Cold Damage 1d6', 'COLD_DAMAGE', 1, 6, 0, 0),
( 158, 'Sonic Damage 1d6', 'SONIC_DAMAGE', 1, 6, 0, 0),
( 159, 'Poison Damage 1d6', 'POISON_DAMAGE', 1, 6, 0, 0),
( 160, 'Electrical Damage 1d6', 'ELECTRICAL_DAMAGE', 1, 6, 0, 0),
( 161, 'Fly', 'FLY', 0, 0, 0, 0),
( 162, 'Water Breathing', 'WATER_BREATHING', 0, 0, 0, 0),
( 163, 'Invisible', 'INVISIBLE', 0, 0, 0, 0),
( 164, 'Bashing Damage 2d6', 'BASHING_DAMAGE', 2, 6, 0, 0),
( 165, 'Piercing Damage 2d6', 'PIERCING_DAMAGE', 2, 6, 0, 0),
( 166, 'Slashing Damage 2d6', 'SLASHING_DAMAGE', 2, 6, 0, 0),
( 167, 'Fire Damage 2d6', 'FIRE_DAMAGE', 2, 6, 0, 0),
( 168, 'Cold Damage 2d6', 'COLD_DAMAGE', 2, 6, 0, 0),
( 169, 'Sonic Damage 2d6', 'SONIC_DAMAGE', 2, 6, 0, 0),
( 170, 'Poison Damage 2d6', 'POISON_DAMAGE', 2, 6, 0, 0),
( 171, 'Electrical Damage 2d6', 'ELECTRICAL_DAMAGE', 2, 6, 0, 0),
( 172, 'Bashing Damage 1d8', 'BASHING_DAMAGE', 1, 8, 0, 0),
( 173, 'Piercing Damage 1d8', 'PIERCING_DAMAGE', 1, 8, 0, 0),
( 174, 'Slashing Damage 1d8', 'SLASHING_DAMAGE', 1, 8, 0, 0),
( 175, 'Fire Damage 1d8', 'FIRE_DAMAGE', 1, 8, 0, 0),
( 176, 'Cold Damage 1d8', 'COLD_DAMAGE', 1, 8, 0, 0),
( 177, 'Sonic Damage 1d8', 'SONIC_DAMAGE', 1, 8, 0, 0),
( 178, 'Poison Damage 1d8', 'POISON_DAMAGE', 1, 8, 0, 0),
( 179, 'Electrical Damage 1d8', 'ELECTRICAL_DAMAGE', 1, 8, 0, 0),
( 180, 'Bashing Damage 2d8', 'BASHING_DAMAGE', 2, 8, 0, 0),
( 181, 'Piercing Damage 2d8', 'PIERCING_DAMAGE', 2, 8, 0, 0),
( 182, 'Slashing Damage 2d8', 'SLASHING_DAMAGE', 2, 8, 0, 0),
( 183, 'Fire Damage 2d8', 'FIRE_DAMAGE', 2, 8, 0, 0),
( 184, 'Cold Damage 2d8', 'COLD_DAMAGE', 2, 8, 0, 0),
( 185, 'Sonic Damage 2d8', 'SONIC_DAMAGE', 2, 8, 0, 0),
( 186, 'Poison Damage 2d8', 'POISON_DAMAGE', 2, 8, 0, 0),
( 187, 'Electrical Damage 2d8', 'ELECTRICAL_DAMAGE', 2, 8, 0, 0),
( 188, 'Bashing Damage 1d10', 'BASHING_DAMAGE', 1, 10, 0, 0),
( 189, 'Piercing Damage 1d10', 'PIERCING_DAMAGE', 1, 10, 0, 0),
( 190, 'Slashing Damage 1d10', 'SLASHING_DAMAGE', 1, 10, 0, 0),
( 191, 'Fire Damage 1d10', 'FIRE_DAMAGE', 1, 10, 0, 0),
( 192, 'Cold Damage 1d10', 'COLD_DAMAGE', 1, 10, 0, 0),
( 193, 'Sonic Damage 1d10', 'SONIC_DAMAGE', 1, 10, 0, 0),
( 194, 'Poison Damage 1d10', 'POISON_DAMAGE', 1, 10, 0, 0),
( 195, 'Electrical Damage 1d10', 'ELECTRICAL_DAMAGE', 1, 10, 0, 0),
( 196, 'Bashing Damage 2d10', 'BASHING_DAMAGE', 2, 10, 0, 0),
( 197, 'Piercing Damage 2d10', 'PIERCING_DAMAGE', 2, 10, 0, 0),
( 198, 'Slashing Damage 2d10', 'SLASHING_DAMAGE', 2, 10, 0, 0),
( 199, 'Fire Damage 2d10', 'FIRE_DAMAGE', 2, 10, 0, 0),
( 200, 'Cold Damage 2d10', 'COLD_DAMAGE', 2, 10, 0, 0),
( 201, 'Sonic Damage 2d10', 'SONIC_DAMAGE', 2, 10, 0, 0),
( 202, 'Poison Damage 2d10', 'POISON_DAMAGE', 2, 10, 0, 0),
( 203, 'Electrical Damage 2d10', 'ELECTRICAL_DAMAGE', 2, 10, 0, 0),
( 204, 'Bashing Damage 1d12', 'BASHING_DAMAGE', 1, 12, 0, 0),
( 205, 'Piercing Damage 1d12', 'PIERCING_DAMAGE', 1, 12, 0, 0),
( 206, 'Slashing Damage 1d12', 'SLASHING_DAMAGE', 1, 12, 0, 0),
( 207, 'Fire Damage 1d12', 'FIRE_DAMAGE', 1, 12, 0, 0),
( 208, 'Cold Damage 1d12', 'COLD_DAMAGE', 1, 12, 0, 0),
( 209, 'Sonic Damage 1d12', 'SONIC_DAMAGE', 1, 12, 0, 0),
( 210, 'Poison Damage 1d12', 'POISON_DAMAGE', 1, 12, 0, 0),
( 211, 'Electrical Damage 1d12', 'ELECTRICAL_DAMAGE', 1, 12, 0, 0),
( 212, 'Bashing Damage 2d12', 'BASHING_DAMAGE', 2, 12, 0, 0),
( 213, 'Piercing Damage 2d12', 'PIERCING_DAMAGE', 2, 12, 0, 0),
( 214, 'Slashing Damage 2d12', 'SLASHING_DAMAGE', 2, 12, 0, 0),
( 215, 'Fire Damage 2d12', 'FIRE_DAMAGE', 2, 12, 0, 0),
( 216, 'Cold Damage 2d12', 'COLD_DAMAGE', 2, 12, 0, 0),
( 217, 'Sonic Damage 2d12', 'SONIC_DAMAGE', 2, 12, 0, 0),
( 218, 'Poison Damage 2d12', 'POISON_DAMAGE', 2, 12, 0, 0),
( 219, 'Electrical Damage 2d12', 'ELECTRICAL_DAMAGE', 2, 12, 0, 0),
( 220, 'Bashing Damage 3d6', 'BASHING_DAMAGE', 3, 6, 0, 0),
( 221, 'Piercing Damage 3d6', 'PIERCING_DAMAGE', 3, 6, 0, 0),
( 222, 'Slashing Damage 3d6', 'SLASHING_DAMAGE', 3, 6, 0, 0),
( 223, 'Fire Damage 3d6', 'FIRE_DAMAGE', 3, 6, 0, 0),
( 224, 'Cold Damage 3d6', 'COLD_DAMAGE', 3, 6, 0, 0),
( 225, 'Sonic Damage 3d6', 'SONIC_DAMAGE', 3, 6, 0, 0),
( 226, 'Poison Damage 3d6', 'POISON_DAMAGE', 3, 6, 0, 0),
( 227, 'Electrical Damage 3d6', 'ELECTRICAL_DAMAGE', 3, 6, 0, 0),
( 228, 'Bashing Damage 3d8', 'BASHING_DAMAGE', 3, 8, 0, 0),
( 229, 'Piercing Damage 3d8', 'PIERCING_DAMAGE', 3, 8, 0, 0),
( 230, 'Slashing Damage 3d8', 'SLASHING_DAMAGE', 3, 8, 0, 0),
( 231, 'Fire Damage 3d8', 'FIRE_DAMAGE', 3, 8, 0, 0),
( 232, 'Cold Damage 3d8', 'COLD_DAMAGE', 3, 8, 0, 0),
( 233, 'Sonic Damage 3d8', 'SONIC_DAMAGE', 3, 8, 0, 0),
( 234, 'Poison Damage 3d8', 'POISON_DAMAGE', 3, 8, 0, 0),
( 235, 'Electrical Damage 3d8', 'ELECTRICAL_DAMAGE', 3, 8, 0, 0),
( 236, 'Bashing Damage 3d10', 'BASHING_DAMAGE', 3, 10, 0, 0),
( 237, 'Piercing Damage 3d10', 'PIERCING_DAMAGE', 3, 10, 0, 0),
( 238, 'Slashing Damage 3d10', 'SLASHING_DAMAGE', 3, 10, 0, 0),
( 239, 'Fire Damage 3d10', 'FIRE_DAMAGE', 3, 10, 0, 0),
( 240, 'Cold Damage 3d10', 'COLD_DAMAGE', 3, 10, 0, 0),
( 241, 'Sonic Damage 3d10', 'SONIC_DAMAGE', 3, 10, 0, 0),
( 242, 'Poison Damage 3d10', 'POISON_DAMAGE', 3, 10, 0, 0),
( 243, 'Electrical Damage 3d10', 'ELECTRICAL_DAMAGE', 3, 10, 0, 0),
( 244, 'Bashing Damage 3d12', 'BASHING_DAMAGE', 3, 12, 0, 0),
( 245, 'Piercing Damage 3d12', 'PIERCING_DAMAGE', 3, 12, 0, 0),
( 246, 'Slashing Damage 3d12', 'SLASHING_DAMAGE', 3, 12, 0, 0),
( 247, 'Fire Damage 3d12', 'FIRE_DAMAGE', 3, 12, 0, 0),
( 248, 'Cold Damage 3d12', 'COLD_DAMAGE', 3, 12, 0, 0),
( 249, 'Sonic Damage 3d12', 'SONIC_DAMAGE', 3, 12, 0, 0),
( 250, 'Poison Damage 3d12', 'POISON_DAMAGE', 3, 12, 0, 0),
( 251, 'Electrical Damage 3d12', 'ELECTRICAL_DAMAGE', 3, 12, 0, 0),
( 252, 'Bashing Damage 3d20', 'BASHING_DAMAGE', 3, 20, 0, 0),
( 253, 'Piercing Damage 3d20', 'PIERCING_DAMAGE', 3, 20, 0, 0),
( 254, 'Slashing Damage 3d20', 'SLASHING_DAMAGE', 3, 20, 0, 0),
( 255, 'Fire Damage 3d20', 'FIRE_DAMAGE', 3, 20, 0, 0),
( 256, 'Cold Damage 3d20', 'COLD_DAMAGE', 3, 20, 0, 0),
( 257, 'Sonic Damage 3d20', 'SONIC_DAMAGE', 3, 20, 0, 0),
( 258, 'Poison Damage 3d20', 'POISON_DAMAGE', 3, 20, 0, 0),
( 259, 'Electrical Damage 3d20', 'ELECTRICAL_DAMAGE', 3, 20, 0, 0),
( 260, 'Bashing Damage 4d6', 'BASHING_DAMAGE', 4, 6, 0, 0),
( 261, 'Piercing Damage 4d6', 'PIERCING_DAMAGE', 4, 6, 0, 0),
( 262, 'Slashing Damage 4d6', 'SLASHING_DAMAGE', 4, 6, 0, 0),
( 263, 'Fire Damage 4d6', 'FIRE_DAMAGE', 4, 6, 0, 0),
( 264, 'Cold Damage 4d6', 'COLD_DAMAGE', 4, 6, 0, 0),
( 265, 'Sonic Damage 4d6', 'SONIC_DAMAGE', 4, 6, 0, 0),
( 266, 'Poison Damage 4d6', 'POISON_DAMAGE', 4, 6, 0, 0),
( 267, 'Electrical Damage 4d6', 'ELECTRICAL_DAMAGE', 4, 6, 0, 0),
( 268, 'Bashing Damage 4d8', 'BASHING_DAMAGE', 4, 8, 0, 0),
( 269, 'Piercing Damage 4d8', 'PIERCING_DAMAGE', 4, 8, 0, 0),
( 270, 'Slashing Damage 4d8', 'SLASHING_DAMAGE', 4, 8, 0, 0),
( 271, 'Fire Damage 4d8', 'FIRE_DAMAGE', 4, 8, 0, 0),
( 272, 'Cold Damage 4d8', 'COLD_DAMAGE', 4, 8, 0, 0),
( 273, 'Sonic Damage 4d8', 'SONIC_DAMAGE', 4, 8, 0, 0),
( 274, 'Poison Damage 4d8', 'POISON_DAMAGE', 4, 8, 0, 0),
( 275, 'Electrical Damage 4d8', 'ELECTRICAL_DAMAGE', 4, 8, 0, 0),
( 276, 'Bashing Damage 4d10', 'BASHING_DAMAGE', 4, 10, 0, 0),
( 277, 'Piercing Damage 4d10', 'PIERCING_DAMAGE', 4, 10, 0, 0),
( 278, 'Slashing Damage 4d10', 'SLASHING_DAMAGE', 4, 10, 0, 0),
( 279, 'Fire Damage 4d10', 'FIRE_DAMAGE', 4, 10, 0, 0),
( 280, 'Cold Damage 4d10', 'COLD_DAMAGE', 4, 10, 0, 0),
( 281, 'Sonic Damage 4d10', 'SONIC_DAMAGE', 4, 10, 0, 0),
( 282, 'Poison Damage 4d10', 'POISON_DAMAGE', 4, 10, 0, 0),
( 283, 'Electrical Damage 4d10', 'ELECTRICAL_DAMAGE', 4, 10, 0, 0),
( 284, 'Bashing Damage 4d12', 'BASHING_DAMAGE', 4, 12, 0, 0),
( 285, 'Piercing Damage 4d12', 'PIERCING_DAMAGE', 4, 12, 0, 0),
( 286, 'Slashing Damage 4d12', 'SLASHING_DAMAGE', 4, 12, 0, 0),
( 287, 'Fire Damage 4d12', 'FIRE_DAMAGE', 4, 12, 0, 0),
( 288, 'Cold Damage 4d12', 'COLD_DAMAGE', 4, 12, 0, 0),
( 289, 'Sonic Damage 4d12', 'SONIC_DAMAGE', 4, 12, 0, 0),
( 290, 'Poison Damage 4d12', 'POISON_DAMAGE', 4, 12, 0, 0),
( 291, 'Electrical Damage 4d12', 'ELECTRICAL_DAMAGE', 4, 12, 0, 0),
( 292, 'Bashing Damage 4d20', 'BASHING_DAMAGE', 4, 20, 0, 0),
( 293, 'Piercing Damage 4d20', 'PIERCING_DAMAGE', 4, 20, 0, 0),
( 294, 'Slashing Damage 4d20', 'SLASHING_DAMAGE', 4, 20, 0, 0),
( 295, 'Fire Damage 4d20', 'FIRE_DAMAGE', 4, 20, 0, 0),
( 296, 'Cold Damage 4d20', 'COLD_DAMAGE', 4, 20, 0, 0),
( 297, 'Sonic Damage 4d20', 'SONIC_DAMAGE', 4, 20, 0, 0),
( 298, 'Poison Damage 4d20', 'POISON_DAMAGE', 4, 20, 0, 0),
( 299, 'Electrical Damage 4d20', 'ELECTRICAL_DAMAGE', 4, 20, 0, 0),
( 300, 'Bashing Damage 5d6', 'BASHING_DAMAGE', 5, 6, 0, 0),
( 301, 'Piercing Damage 5d6', 'PIERCING_DAMAGE', 5, 6, 0, 0),
( 302, 'Slashing Damage 5d6', 'SLASHING_DAMAGE', 5, 6, 0, 0),
( 303, 'Fire Damage 5d6', 'FIRE_DAMAGE', 5, 6, 0, 0),
( 304, 'Cold Damage 5d6', 'COLD_DAMAGE', 5, 6, 0, 0),
( 305, 'Sonic Damage 5d6', 'SONIC_DAMAGE', 5, 6, 0, 0),
( 306, 'Poison Damage 5d6', 'POISON_DAMAGE', 5, 6, 0, 0),
( 307, 'Electrical Damage 5d6', 'ELECTRICAL_DAMAGE', 5, 6, 0, 0),
( 308, 'Bashing Damage 5d8', 'BASHING_DAMAGE', 5, 8, 0, 0),
( 309, 'Piercing Damage 5d8', 'PIERCING_DAMAGE', 5, 8, 0, 0),
( 310, 'Slashing Damage 5d8', 'SLASHING_DAMAGE', 5, 8, 0, 0),
( 311, 'Fire Damage 5d8', 'FIRE_DAMAGE', 5, 8, 0, 0),
( 312, 'Cold Damage 5d8', 'COLD_DAMAGE', 5, 8, 0, 0),
( 313, 'Sonic Damage 5d8', 'SONIC_DAMAGE', 5, 8, 0, 0),
( 314, 'Poison Damage 5d8', 'POISON_DAMAGE', 5, 8, 0, 0),
( 315, 'Electrical Damage 5d8', 'ELECTRICAL_DAMAGE', 5, 8, 0, 0),
( 316, 'Bashing Damage 5d10', 'BASHING_DAMAGE', 5, 10, 0, 0),
( 317, 'Piercing Damage 5d10', 'PIERCING_DAMAGE', 5, 10, 0, 0),
( 318, 'Slashing Damage 5d10', 'SLASHING_DAMAGE', 5, 10, 0, 0),
( 319, 'Fire Damage 5d10', 'FIRE_DAMAGE', 5, 10, 0, 0),
( 320, 'Cold Damage 5d10', 'COLD_DAMAGE', 5, 10, 0, 0),
( 321, 'Sonic Damage 5d10', 'SONIC_DAMAGE', 5, 10, 0, 0),
( 322, 'Poison Damage 5d10', 'POISON_DAMAGE', 5, 10, 0, 0),
( 323, 'Electrical Damage 5d10', 'ELECTRICAL_DAMAGE', 5, 10, 0, 0),
( 324, 'Bashing Damage 5d12', 'BASHING_DAMAGE', 5, 12, 0, 0),
( 325, 'Piercing Damage 5d12', 'PIERCING_DAMAGE', 5, 12, 0, 0),
( 326, 'Slashing Damage 5d12', 'SLASHING_DAMAGE', 5, 12, 0, 0),
( 327, 'Fire Damage 5d12', 'FIRE_DAMAGE', 5, 12, 0, 0),
( 328, 'Cold Damage 5d12', 'COLD_DAMAGE', 5, 12, 0, 0),
( 329, 'Sonic Damage 5d12', 'SONIC_DAMAGE', 5, 12, 0, 0),
( 330, 'Poison Damage 5d12', 'POISON_DAMAGE', 5, 12, 0, 0),
( 331, 'Electrical Damage 5d12', 'ELECTRICAL_DAMAGE', 5, 12, 0, 0),
( 332, 'Bashing Damage 5d20', 'BASHING_DAMAGE', 5, 20, 0, 0),
( 333, 'Piercing Damage 5d20', 'PIERCING_DAMAGE', 5, 20, 0, 0),
( 334, 'Slashing Damage 5d20', 'SLASHING_DAMAGE', 5, 20, 0, 0),
( 335, 'Fire Damage 5d20', 'FIRE_DAMAGE', 5, 20, 0, 0),
( 336, 'Cold Damage 5d20', 'COLD_DAMAGE', 5, 20, 0, 0),
( 337, 'Sonic Damage 5d20', 'SONIC_DAMAGE', 5, 20, 0, 0),
( 338, 'Poison Damage 5d20', 'POISON_DAMAGE', 5, 20, 0, 0),
( 339, 'Electrical Damage 5d20', 'ELECTRICAL_DAMAGE', 5, 20, 0, 0),
( 340, 'Bashing Damage 6d6', 'BASHING_DAMAGE', 6, 6, 0, 0),
( 341, 'Piercing Damage 6d6', 'PIERCING_DAMAGE', 6, 6, 0, 0),
( 342, 'Slashing Damage 6d6', 'SLASHING_DAMAGE', 6, 6, 0, 0),
( 343, 'Fire Damage 6d6', 'FIRE_DAMAGE', 6, 6, 0, 0),
( 344, 'Cold Damage 6d6', 'COLD_DAMAGE', 6, 6, 0, 0),
( 345, 'Sonic Damage 6d6', 'SONIC_DAMAGE', 6, 6, 0, 0),
( 346, 'Poison Damage 6d6', 'POISON_DAMAGE', 6, 6, 0, 0),
( 347, 'Electrical Damage 6d6', 'ELECTRICAL_DAMAGE', 6, 6, 0, 0),
( 348, 'Bashing Damage 6d8', 'BASHING_DAMAGE', 6, 8, 0, 0),
( 349, 'Piercing Damage 6d8', 'PIERCING_DAMAGE', 6, 8, 0, 0),
( 350, 'Slashing Damage 6d8', 'SLASHING_DAMAGE', 6, 8, 0, 0),
( 351, 'Fire Damage 6d8', 'FIRE_DAMAGE', 6, 8, 0, 0),
( 352, 'Cold Damage 6d8', 'COLD_DAMAGE', 6, 8, 0, 0),
( 353, 'Sonic Damage 6d8', 'SONIC_DAMAGE', 6, 8, 0, 0),
( 354, 'Poison Damage 6d8', 'POISON_DAMAGE', 6, 8, 0, 0),
( 355, 'Electrical Damage 6d8', 'ELECTRICAL_DAMAGE', 6, 8, 0, 0),
( 356, 'Bashing Damage 6d10', 'BASHING_DAMAGE', 6, 10, 0, 0),
( 357, 'Piercing Damage 6d10', 'PIERCING_DAMAGE', 6, 10, 0, 0),
( 358, 'Slashing Damage 6d10', 'SLASHING_DAMAGE', 6, 10, 0, 0),
( 359, 'Fire Damage 6d10', 'FIRE_DAMAGE', 6, 10, 0, 0),
( 360, 'Cold Damage 6d10', 'COLD_DAMAGE', 6, 10, 0, 0),
( 361, 'Sonic Damage 6d10', 'SONIC_DAMAGE', 6, 10, 0, 0),
( 362, 'Poison Damage 6d10', 'POISON_DAMAGE', 6, 10, 0, 0),
( 363, 'Electrical Damage 6d10', 'ELECTRICAL_DAMAGE', 6, 10, 0, 0),
( 364, 'Bashing Damage 6d12', 'BASHING_DAMAGE', 6, 12, 0, 0),
( 365, 'Piercing Damage 6d12', 'PIERCING_DAMAGE', 6, 12, 0, 0),
( 366, 'Slashing Damage 6d12', 'SLASHING_DAMAGE', 6, 12, 0, 0),
( 367, 'Fire Damage 6d12', 'FIRE_DAMAGE', 6, 12, 0, 0),
( 368, 'Cold Damage 6d12', 'COLD_DAMAGE', 6, 12, 0, 0),
( 369, 'Sonic Damage 6d12', 'SONIC_DAMAGE', 6, 12, 0, 0),
( 370, 'Poison Damage 6d12', 'POISON_DAMAGE', 6, 12, 0, 0),
( 371, 'Electrical Damage 6d12', 'ELECTRICAL_DAMAGE', 6, 12, 0, 0),
( 372, 'Bashing Damage 6d20', 'BASHING_DAMAGE', 6, 20, 0, 0),
( 373, 'Piercing Damage 6d20', 'PIERCING_DAMAGE', 6, 20, 0, 0),
( 374, 'Slashing Damage 6d20', 'SLASHING_DAMAGE', 6, 20, 0, 0),
( 375, 'Fire Damage 6d20', 'FIRE_DAMAGE', 6, 20, 0, 0),
( 376, 'Cold Damage 6d20', 'COLD_DAMAGE', 6, 20, 0, 0),
( 377, 'Sonic Damage 6d20', 'SONIC_DAMAGE', 6, 20, 0, 0),
( 378, 'Poison Damage 6d20', 'POISON_DAMAGE', 6, 20, 0, 0),
( 379, 'Electrical Damage 6d20', 'ELECTRICAL_DAMAGE', 6, 20, 0, 0),
( 380, 'Bashing Damage 7d6', 'BASHING_DAMAGE', 7, 6, 0, 0),
( 381, 'Piercing Damage 7d6', 'PIERCING_DAMAGE', 7, 6, 0, 0),
( 382, 'Slashing Damage 7d6', 'SLASHING_DAMAGE', 7, 6, 0, 0),
( 383, 'Fire Damage 7d6', 'FIRE_DAMAGE', 7, 6, 0, 0),
( 384, 'Cold Damage 7d6', 'COLD_DAMAGE', 7, 6, 0, 0),
( 385, 'Sonic Damage 7d6', 'SONIC_DAMAGE', 7, 6, 0, 0),
( 386, 'Poison Damage 7d6', 'POISON_DAMAGE', 7, 6, 0, 0),
( 387, 'Electrical Damage 7d6', 'ELECTRICAL_DAMAGE', 7, 6, 0, 0),
( 388, 'Bashing Damage 7d8', 'BASHING_DAMAGE', 7, 8, 0, 0),
( 389, 'Piercing Damage 7d8', 'PIERCING_DAMAGE', 7, 8, 0, 0),
( 390, 'Slashing Damage 7d8', 'SLASHING_DAMAGE', 7, 8, 0, 0),
( 391, 'Fire Damage 7d8', 'FIRE_DAMAGE', 7, 8, 0, 0),
( 392, 'Cold Damage 7d8', 'COLD_DAMAGE', 7, 8, 0, 0),
( 393, 'Sonic Damage 7d8', 'SONIC_DAMAGE', 7, 8, 0, 0),
( 394, 'Poison Damage 7d8', 'POISON_DAMAGE', 7, 8, 0, 0),
( 395, 'Electrical Damage 7d8', 'ELECTRICAL_DAMAGE', 7, 8, 0, 0),
( 396, 'Bashing Damage 7d10', 'BASHING_DAMAGE', 7, 10, 0, 0),
( 397, 'Piercing Damage 7d10', 'PIERCING_DAMAGE', 7, 10, 0, 0),
( 398, 'Slashing Damage 7d10', 'SLASHING_DAMAGE', 7, 10, 0, 0),
( 399, 'Fire Damage 7d10', 'FIRE_DAMAGE', 7, 10, 0, 0),
( 400, 'Cold Damage 7d10', 'COLD_DAMAGE', 7, 10, 0, 0),
( 401, 'Sonic Damage 7d10', 'SONIC_DAMAGE', 7, 10, 0, 0),
( 402, 'Poison Damage 7d10', 'POISON_DAMAGE', 7, 10, 0, 0),
( 403, 'Electrical Damage 7d10', 'ELECTRICAL_DAMAGE', 7, 10, 0, 0),
( 404, 'Bashing Damage 7d12', 'BASHING_DAMAGE', 7, 12, 0, 0),
( 405, 'Piercing Damage 7d12', 'PIERCING_DAMAGE', 7, 12, 0, 0),
( 406, 'Slashing Damage 7d12', 'SLASHING_DAMAGE', 7, 12, 0, 0),
( 407, 'Fire Damage 7d12', 'FIRE_DAMAGE', 7, 12, 0, 0),
( 408, 'Cold Damage 7d12', 'COLD_DAMAGE', 7, 12, 0, 0),
( 409, 'Sonic Damage 7d12', 'SONIC_DAMAGE', 7, 12, 0, 0),
( 410, 'Poison Damage 7d12', 'POISON_DAMAGE', 7, 12, 0, 0),
( 411, 'Electrical Damage 7d12', 'ELECTRICAL_DAMAGE', 7, 12, 0, 0),
( 412, 'Bashing Damage 7d20', 'BASHING_DAMAGE', 7, 20, 0, 0),
( 413, 'Piercing Damage 7d20', 'PIERCING_DAMAGE', 7, 20, 0, 0),
( 414, 'Slashing Damage 7d20', 'SLASHING_DAMAGE', 7, 20, 0, 0),
( 415, 'Fire Damage 7d20', 'FIRE_DAMAGE', 7, 20, 0, 0),
( 416, 'Cold Damage 7d20', 'COLD_DAMAGE', 7, 20, 0, 0),
( 417, 'Sonic Damage 7d20', 'SONIC_DAMAGE', 7, 20, 0, 0),
( 418, 'Poison Damage 7d20', 'POISON_DAMAGE', 7, 20, 0, 0),
( 419, 'Electrical Damage 7d20', 'ELECTRICAL_DAMAGE', 7, 20, 0, 0),
( 420, 'Bashing Damage 8d6', 'BASHING_DAMAGE', 8, 6, 0, 0),
( 421, 'Piercing Damage 8d6', 'PIERCING_DAMAGE', 8, 6, 0, 0),
( 422, 'Slashing Damage 8d6', 'SLASHING_DAMAGE', 8, 6, 0, 0),
( 423, 'Fire Damage 8d6', 'FIRE_DAMAGE', 8, 6, 0, 0),
( 424, 'Cold Damage 8d6', 'COLD_DAMAGE', 8, 6, 0, 0),
( 425, 'Sonic Damage 8d6', 'SONIC_DAMAGE', 8, 6, 0, 0),
( 426, 'Poison Damage 8d6', 'POISON_DAMAGE', 8, 6, 0, 0),
( 427, 'Electrical Damage 8d6', 'ELECTRICAL_DAMAGE', 8, 6, 0, 0),
( 428, 'Bashing Damage 8d8', 'BASHING_DAMAGE', 8, 8, 0, 0),
( 429, 'Piercing Damage 8d8', 'PIERCING_DAMAGE', 8, 8, 0, 0),
( 430, 'Slashing Damage 8d8', 'SLASHING_DAMAGE', 8, 8, 0, 0),
( 431, 'Fire Damage 8d8', 'FIRE_DAMAGE', 8, 8, 0, 0),
( 432, 'Cold Damage 8d8', 'COLD_DAMAGE', 8, 8, 0, 0),
( 433, 'Sonic Damage 8d8', 'SONIC_DAMAGE', 8, 8, 0, 0),
( 434, 'Poison Damage 8d8', 'POISON_DAMAGE', 8, 8, 0, 0),
( 435, 'Electrical Damage 8d8', 'ELECTRICAL_DAMAGE', 8, 8, 0, 0),
( 436, 'Bashing Damage 8d10', 'BASHING_DAMAGE', 8, 10, 0, 0),
( 437, 'Piercing Damage 8d10', 'PIERCING_DAMAGE', 8, 10, 0, 0),
( 438, 'Slashing Damage 8d10', 'SLASHING_DAMAGE', 8, 10, 0, 0),
( 439, 'Fire Damage 8d10', 'FIRE_DAMAGE', 8, 10, 0, 0),
( 440, 'Cold Damage 8d10', 'COLD_DAMAGE', 8, 10, 0, 0),
( 441, 'Sonic Damage 8d10', 'SONIC_DAMAGE', 8, 10, 0, 0),
( 442, 'Poison Damage 8d10', 'POISON_DAMAGE', 8, 10, 0, 0),
( 443, 'Electrical Damage 8d10', 'ELECTRICAL_DAMAGE', 8, 10, 0, 0),
( 444, 'Bashing Damage 8d12', 'BASHING_DAMAGE', 8, 12, 0, 0),
( 445, 'Piercing Damage 8d12', 'PIERCING_DAMAGE', 8, 12, 0, 0),
( 446, 'Slashing Damage 8d12', 'SLASHING_DAMAGE', 8, 12, 0, 0),
( 447, 'Fire Damage 8d12', 'FIRE_DAMAGE', 8, 12, 0, 0),
( 448, 'Cold Damage 8d12', 'COLD_DAMAGE', 8, 12, 0, 0),
( 449, 'Sonic Damage 8d12', 'SONIC_DAMAGE', 8, 12, 0, 0),
( 450, 'Poison Damage 8d12', 'POISON_DAMAGE', 8, 12, 0, 0),
( 451, 'Electrical Damage 8d12', 'ELECTRICAL_DAMAGE', 8, 12, 0, 0),
( 452, 'Bashing Damage 8d20', 'BASHING_DAMAGE', 8, 20, 0, 0),
( 453, 'Piercing Damage 8d20', 'PIERCING_DAMAGE', 8, 20, 0, 0),
( 454, 'Slashing Damage 8d20', 'SLASHING_DAMAGE', 8, 20, 0, 0),
( 455, 'Fire Damage 8d20', 'FIRE_DAMAGE', 8, 20, 0, 0),
( 456, 'Cold Damage 8d20', 'COLD_DAMAGE', 8, 20, 0, 0),
( 457, 'Sonic Damage 8d20', 'SONIC_DAMAGE', 8, 20, 0, 0),
( 458, 'Poison Damage 8d20', 'POISON_DAMAGE', 8, 20, 0, 0),
( 459, 'Electrical Damage 8d20', 'ELECTRICAL_DAMAGE', 8, 20, 0, 0),
( 460, 'Bashing Damage 9d6', 'BASHING_DAMAGE', 9, 6, 0, 0),
( 461, 'Piercing Damage 9d6', 'PIERCING_DAMAGE', 9, 6, 0, 0),
( 462, 'Slashing Damage 9d6', 'SLASHING_DAMAGE', 9, 6, 0, 0),
( 463, 'Fire Damage 9d6', 'FIRE_DAMAGE', 9, 6, 0, 0),
( 464, 'Cold Damage 9d6', 'COLD_DAMAGE', 9, 6, 0, 0),
( 465, 'Sonic Damage 9d6', 'SONIC_DAMAGE', 9, 6, 0, 0),
( 466, 'Poison Damage 9d6', 'POISON_DAMAGE', 9, 6, 0, 0),
( 467, 'Electrical Damage 9d6', 'ELECTRICAL_DAMAGE', 9, 6, 0, 0),
( 468, 'Bashing Damage 9d8', 'BASHING_DAMAGE', 9, 8, 0, 0),
( 469, 'Piercing Damage 9d8', 'PIERCING_DAMAGE', 9, 8, 0, 0),
( 470, 'Slashing Damage 9d8', 'SLASHING_DAMAGE', 9, 8, 0, 0),
( 471, 'Fire Damage 9d8', 'FIRE_DAMAGE', 9, 8, 0, 0),
( 472, 'Cold Damage 9d8', 'COLD_DAMAGE', 9, 8, 0, 0),
( 473, 'Sonic Damage 9d8', 'SONIC_DAMAGE', 9, 8, 0, 0),
( 474, 'Poison Damage 9d8', 'POISON_DAMAGE', 9, 8, 0, 0),
( 475, 'Electrical Damage 9d8', 'ELECTRICAL_DAMAGE', 9, 8, 0, 0),
( 476, 'Bashing Damage 9d10', 'BASHING_DAMAGE', 9, 10, 0, 0),
( 477, 'Piercing Damage 9d10', 'PIERCING_DAMAGE', 9, 10, 0, 0),
( 478, 'Slashing Damage 9d10', 'SLASHING_DAMAGE', 9, 10, 0, 0),
( 479, 'Fire Damage 9d10', 'FIRE_DAMAGE', 9, 10, 0, 0),
( 480, 'Cold Damage 9d10', 'COLD_DAMAGE', 9, 10, 0, 0),
( 481, 'Sonic Damage 9d10', 'SONIC_DAMAGE', 9, 10, 0, 0),
( 482, 'Poison Damage 9d10', 'POISON_DAMAGE', 9, 10, 0, 0),
( 483, 'Electrical Damage 9d10', 'ELECTRICAL_DAMAGE', 9, 10, 0, 0),
( 484, 'Bashing Damage 9d12', 'BASHING_DAMAGE', 9, 12, 0, 0),
( 485, 'Piercing Damage 9d12', 'PIERCING_DAMAGE', 9, 12, 0, 0),
( 486, 'Slashing Damage 9d12', 'SLASHING_DAMAGE', 9, 12, 0, 0),
( 487, 'Fire Damage 9d12', 'FIRE_DAMAGE', 9, 12, 0, 0),
( 488, 'Cold Damage 9d12', 'COLD_DAMAGE', 9, 12, 0, 0),
( 489, 'Sonic Damage 9d12', 'SONIC_DAMAGE', 9, 12, 0, 0),
( 490, 'Poison Damage 9d12', 'POISON_DAMAGE', 9, 12, 0, 0),
( 491, 'Electrical Damage 9d12', 'ELECTRICAL_DAMAGE', 9, 12, 0, 0),
( 492, 'Bashing Damage 9d20', 'BASHING_DAMAGE', 9, 20, 0, 0),
( 493, 'Piercing Damage 9d20', 'PIERCING_DAMAGE', 9, 20, 0, 0),
( 494, 'Slashing Damage 9d20', 'SLASHING_DAMAGE', 9, 20, 0, 0),
( 495, 'Fire Damage 9d20', 'FIRE_DAMAGE', 9, 20, 0, 0),
( 496, 'Cold Damage 9d20', 'COLD_DAMAGE', 9, 20, 0, 0),
( 497, 'Sonic Damage 9d20', 'SONIC_DAMAGE', 9, 20, 0, 0),
( 498, 'Poison Damage 9d20', 'POISON_DAMAGE', 9, 20, 0, 0),
( 499, 'Electrical Damage 9d20', 'ELECTRICAL_DAMAGE', 9, 20, 0, 0),
( 500, 'Bashing Damage 10d6', 'BASHING_DAMAGE', 10, 6, 0, 0),
( 501, 'Piercing Damage 10d6', 'PIERCING_DAMAGE', 10, 6, 0, 0),
( 502, 'Slashing Damage 10d6', 'SLASHING_DAMAGE', 10, 6, 0, 0),
( 503, 'Fire Damage 10d6', 'FIRE_DAMAGE', 10, 6, 0, 0),
( 504, 'Cold Damage 10d6', 'COLD_DAMAGE', 10, 6, 0, 0),
( 505, 'Sonic Damage 10d6', 'SONIC_DAMAGE', 10, 6, 0, 0),
( 506, 'Poison Damage 10d6', 'POISON_DAMAGE', 10, 6, 0, 0),
( 507, 'Electrical Damage 10d6', 'ELECTRICAL_DAMAGE', 10, 6, 0, 0),
( 508, 'Bashing Damage 10d8', 'BASHING_DAMAGE', 10, 8, 0, 0),
( 509, 'Piercing Damage 10d8', 'PIERCING_DAMAGE', 10, 8, 0, 0),
( 510, 'Slashing Damage 10d8', 'SLASHING_DAMAGE', 10, 8, 0, 0),
( 511, 'Fire Damage 10d8', 'FIRE_DAMAGE', 10, 8, 0, 0),
( 512, 'Cold Damage 10d8', 'COLD_DAMAGE', 10, 8, 0, 0),
( 513, 'Sonic Damage 10d8', 'SONIC_DAMAGE', 10, 8, 0, 0),
( 514, 'Poison Damage 10d8', 'POISON_DAMAGE', 10, 8, 0, 0),
( 515, 'Electrical Damage 10d8', 'ELECTRICAL_DAMAGE', 10, 8, 0, 0),
( 516, 'Bashing Damage 10d10', 'BASHING_DAMAGE', 10, 10, 0, 0),
( 517, 'Piercing Damage 10d10', 'PIERCING_DAMAGE', 10, 10, 0, 0),
( 518, 'Slashing Damage 10d10', 'SLASHING_DAMAGE', 10, 10, 0, 0),
( 519, 'Fire Damage 10d10', 'FIRE_DAMAGE', 10, 10, 0, 0),
( 520, 'Cold Damage 10d10', 'COLD_DAMAGE', 10, 10, 0, 0),
( 521, 'Sonic Damage 10d10', 'SONIC_DAMAGE', 10, 10, 0, 0),
( 522, 'Poison Damage 10d10', 'POISON_DAMAGE', 10, 10, 0, 0),
( 523, 'Electrical Damage 10d10', 'ELECTRICAL_DAMAGE', 10, 10, 0, 0),
( 524, 'Bashing Damage 10d12', 'BASHING_DAMAGE', 10, 12, 0, 0),
( 525, 'Piercing Damage 10d12', 'PIERCING_DAMAGE', 10, 12, 0, 0),
( 526, 'Slashing Damage 10d12', 'SLASHING_DAMAGE', 10, 12, 0, 0),
( 527, 'Fire Damage 10d12', 'FIRE_DAMAGE', 10, 12, 0, 0),
( 528, 'Cold Damage 10d12', 'COLD_DAMAGE', 10, 12, 0, 0),
( 529, 'Sonic Damage 10d12', 'SONIC_DAMAGE', 10, 12, 0, 0),
( 530, 'Poison Damage 10d12', 'POISON_DAMAGE', 10, 12, 0, 0),
( 531, 'Electrical Damage 10d12', 'ELECTRICAL_DAMAGE', 10, 12, 0, 0),
( 532, 'Bashing Damage 10d20', 'BASHING_DAMAGE', 10, 20, 0, 0),
( 533, 'Piercing Damage 10d20', 'PIERCING_DAMAGE', 10, 20, 0, 0),
( 534, 'Slashing Damage 10d20', 'SLASHING_DAMAGE', 10, 20, 0, 0),
( 535, 'Fire Damage 10d20', 'FIRE_DAMAGE', 10, 20, 0, 0),
( 536, 'Cold Damage 10d20', 'COLD_DAMAGE', 10, 20, 0, 0),
( 537, 'Sonic Damage 10d20', 'SONIC_DAMAGE', 10, 20, 0, 0),
( 538, 'Poison Damage 10d20', 'POISON_DAMAGE', 10, 20, 0, 0),
( 539, 'Electrical Damage 10d20', 'ELECTRICAL_DAMAGE', 10, 20, 0, 0)
ON CONFLICT (id) DO NOTHING;

--changeset jeff:47
CREATE TABLE IF NOT EXISTS skills (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    rank INT NOT NULL DEFAULT 1,
    character_id BIGINT NOT NULL,
    FOREIGN KEY (character_id) REFERENCES characters(id),
    UNIQUE (character_id, name)
);

--changeset jeff:48
ALTER TABLE character_classes ADD COLUMN starting_items TEXT;
UPDATE character_classes SET starting_items = '' WHERE starting_items IS NULL;
