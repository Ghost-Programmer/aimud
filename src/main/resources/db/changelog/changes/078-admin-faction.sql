--changeset jeff:078-admin-faction
-- Add the ADMIN faction
INSERT INTO factions (name, description) VALUES ('ADMIN', 'The supreme rulers of the realm.');

-- Update the Admin Vendor to belong to the ADMIN faction
UPDATE mobiles 
SET faction_id = (SELECT id FROM factions WHERE name = 'ADMIN') 
WHERE name = 'Admin Vendor';
