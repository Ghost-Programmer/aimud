--liquibase formatted sql

--changeset jeff:6
INSERT INTO races (name, description, strength_mod, intelligence_mod, wisdom_mod, charisma_mod, dexterity_mod, constitution_mod) VALUES
('Elf', 'A magical people of otherworldly grace, living in the world but not entirely part of it.', 0, 1, 0, 0, 2, 0),
('Halfling', 'The diminutive halflings survive in a world full of larger creatures by avoiding notice or, barring that, avoiding offense.', 0, 0, 0, 0, 2, 0),
('Human', 'Humans are the most adaptable and ambitious people among the common races.', 1, 1, 1, 1, 1, 1),
('Gnome', 'A constant hum of busy activity permeates the warrens and neighborhoods where gnomes form their close-knit communities.', 0, 2, 0, 0, 0, 0),
('Dwarf', 'Bold and hardy, dwarves are known as skilled warriors, miners, and workers of stone and metal.', 0, 0, 0, 0, 0, 2),
('Orc', 'Orcs are savage raiders and pillagers with stooped postures, low foreheads, and piggish faces.', 2, 0, 0, 0, 0, 1);
