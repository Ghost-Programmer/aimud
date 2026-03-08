--liquibase formatted sql

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

INSERT INTO character_classes (name, description, strength_mod, intelligence_mod, wisdom_mod, charisma_mod, dexterity_mod, constitution_mod) VALUES
('Cleric', 'A priestly champion who wields divine magic in service of a higher power.', 0, 0, 2, 0, 0, 0),
('Druid', 'A priest of the Old Faith, wielding the powers of nature and adopting animal forms.', 0, 0, 2, 0, 0, 0),
('Fighter', 'A master of martial combat, skilled with a variety of weapons and armor.', 2, 0, 0, 0, 0, 1),
('Rogue', 'A scoundrel who uses stealth and trickery to overcome obstacles and enemies.', 0, 0, 0, 0, 2, 0),
('Paladin', 'A holy warrior bound to a sacred oath.', 2, 0, 0, 1, 0, 0),
('Wizard', 'A scholarly magic-user capable of manipulating the structures of reality.', 0, 2, 0, 0, 0, 0);
