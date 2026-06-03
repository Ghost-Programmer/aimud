--liquibase formatted sql
--changeset jeff:090-mooncrest-general-goods-items

-- Insert water
INSERT INTO items (id, item_type, wear_location, name, description, property_1, property_2, stackable)
VALUES (671, 'DRINK', 'NONE', 'Water', 'A small flask filled with clear, fresh water.', 1, 5, TRUE)
ON CONFLICT (id) DO UPDATE SET
  item_type = EXCLUDED.item_type,
  wear_location = EXCLUDED.wear_location,
  name = EXCLUDED.name,
  description = EXCLUDED.description,
  property_1 = EXCLUDED.property_1,
  property_2 = EXCLUDED.property_2,
  stackable = EXCLUDED.stackable;

-- Insert Biscuit
INSERT INTO items (id, item_type, wear_location, name, description, property_1, property_2, stackable)
VALUES (672, 'FOOD', 'NONE', 'Biscuit', 'A hard, dry biscuit that satisfies minor hunger.', 1, 5, TRUE)
ON CONFLICT (id) DO UPDATE SET
  item_type = EXCLUDED.item_type,
  wear_location = EXCLUDED.wear_location,
  name = EXCLUDED.name,
  description = EXCLUDED.description,
  property_1 = EXCLUDED.property_1,
  property_2 = EXCLUDED.property_2,
  stackable = EXCLUDED.stackable;

-- Insert Torch
INSERT INTO items (id, item_type, wear_location, name, description, property_1, stackable)
VALUES (673, 'LIGHT', 'OFFHAND', 'Torch', 'A wooden torch wrapped in oil-soaked cloth. Provides light when held in the offhand.', 2, FALSE)
ON CONFLICT (id) DO UPDATE SET
  item_type = EXCLUDED.item_type,
  wear_location = EXCLUDED.wear_location,
  name = EXCLUDED.name,
  description = EXCLUDED.description,
  property_1 = EXCLUDED.property_1,
  stackable = EXCLUDED.stackable;

-- Update existing character inventory references if any exist
UPDATE character_inventory SET item_id = 671 WHERE item_id IN (SELECT id FROM items WHERE name = 'Water' AND id != 671);
UPDATE character_inventory SET item_id = 672 WHERE item_id IN (SELECT id FROM items WHERE name = 'Biscuit' AND id != 672);
UPDATE character_inventory SET item_id = 673 WHERE item_id IN (SELECT id FROM items WHERE name = 'Torch' AND id != 673);

-- Delete old duplicated items with auto-generated IDs
DELETE FROM items WHERE name = 'Water' AND id != 671;
DELETE FROM items WHERE name = 'Biscuit' AND id != 672;
DELETE FROM items WHERE name = 'Torch' AND id != 673;

-- Link items to Mooncrest General Goods store (store_id = 3)
INSERT INTO store_items (store_id, item_id, created_by, modified_by)
VALUES 
  (3, 671, 'system', 'system'),
  (3, 672, 'system', 'system'),
  (3, 673, 'system', 'system')
ON CONFLICT (store_id, item_id) DO NOTHING;

-- Synchronize sequence
SELECT setval(pg_get_serial_sequence('items', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM items;
