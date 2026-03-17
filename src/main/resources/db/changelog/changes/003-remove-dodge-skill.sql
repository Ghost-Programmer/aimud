-- liquibase formatted sql

-- changeset jeff:003-remove-dodge-skill

-- Remove Dodge from character_classes starting_skills
UPDATE character_classes
SET starting_skills = REPLACE(REPLACE(starting_skills, 'Dodge,', ''), ',Dodge', '')
WHERE starting_skills LIKE '%Dodge%';

-- Remove Dodge from skills_registry
DELETE FROM skills_registry WHERE name = 'Dodge';

-- Remove Dodge from any characters that might already have it
DELETE FROM skills WHERE name = 'Dodge';
