--liquibase formatted sql
--changeset jeff:089-store-real-estate-items

-- Create items 669 and 670 if they do not exist
INSERT INTO items (id, item_type, wear_location, name, description, property_1)
VALUES (669, 'DOCUMENT', 'NONE', 'Housing Writ', 'An official writ granting the bearer the right to claim a plot of land for housing.', 250000)
ON CONFLICT (id) DO UPDATE SET
  item_type = EXCLUDED.item_type,
  wear_location = EXCLUDED.wear_location,
  name = EXCLUDED.name,
  description = EXCLUDED.description,
  property_1 = EXCLUDED.property_1;

INSERT INTO items (id, item_type, wear_location, name, description, property_1)
VALUES (670, 'DOCUMENT', 'NONE', 'House Expansion Permit', 'An official permit granting the bearer the right to expand an existing house they own.', 100000)
ON CONFLICT (id) DO UPDATE SET
  item_type = EXCLUDED.item_type,
  wear_location = EXCLUDED.wear_location,
  name = EXCLUDED.name,
  description = EXCLUDED.description,
  property_1 = EXCLUDED.property_1;

-- Update existing character inventory references to point to the correct IDs
UPDATE character_inventory SET item_id = 669 WHERE item_id IN (SELECT id FROM items WHERE name = 'Housing Writ' AND id != 669);
UPDATE character_inventory SET item_id = 670 WHERE item_id IN (SELECT id FROM items WHERE name = 'House Expansion Permit' AND id != 670);

-- Delete old duplicated items with auto-generated IDs
DELETE FROM items WHERE name = 'Housing Writ' AND id != 669;
DELETE FROM items WHERE name = 'House Expansion Permit' AND id != 670;

-- Link items to Mooncrest Real Estate store (store_id = 5)
INSERT INTO store_items (store_id, item_id, created_by, modified_by)
VALUES 
  (5, 669, 'system', 'system'),
  (5, 670, 'system', 'system')
ON CONFLICT (store_id, item_id) DO NOTHING;

-- Synchronize sequence
SELECT setval(pg_get_serial_sequence('items', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM items;
