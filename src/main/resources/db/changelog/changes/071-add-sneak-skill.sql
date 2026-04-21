--changeset jeff:71
INSERT INTO skills_registry (name) VALUES ('Sneak') ON CONFLICT DO NOTHING;

-- Add Sneak to rogue and ranger starting skills
UPDATE character_classes
SET starting_skills = starting_skills || ',Sneak'
WHERE name IN ('Rogue', 'Ranger') AND starting_skills NOT LIKE '%Sneak%';
