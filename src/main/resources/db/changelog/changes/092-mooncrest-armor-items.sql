--liquibase formatted sql
--changeset jeff:092-mooncrest-armor-items

-- Clean up any potential existing conflicting mappings
DELETE FROM store_items WHERE store_id = 1 AND item_id IN (4, 5, 6, 7, 8, 9, 10, 11, 12, 17);

-- Link low level armor items to Mooncrest Armor store (store_id = 1)
INSERT INTO store_items (store_id, item_id, created_by, modified_by)
VALUES
  (1, 4, 'system', 'system'),   -- Leather Tunic (Light Chest)
  (1, 5, 'system', 'system'),   -- Chainmail (Medium Chest)
  (1, 6, 'system', 'system'),   -- Plate Mail (Heavy Chest)
  (1, 7, 'system', 'system'),   -- Leather Cap (Light Head)
  (1, 8, 'system', 'system'),   -- Iron Helm (Heavy Head)
  (1, 9, 'system', 'system'),   -- Leather Leggings (Light Legs)
  (1, 10, 'system', 'system'),  -- Iron Greaves (Heavy Legs)
  (1, 11, 'system', 'system'),  -- Leather Boots (Light Feet)
  (1, 12, 'system', 'system'),  -- Iron Boots (Heavy Feet)
  (1, 17, 'system', 'system')   -- Cloth Robe (Light Chest)
ON CONFLICT (store_id, item_id) DO NOTHING;
