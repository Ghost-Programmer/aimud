--liquibase formatted sql
--changeset jeff:091-weapons-weapons-store

-- Clean up any potential existing conflicting mappings
DELETE FROM item_effects WHERE item_id BETWEEN 674 AND 695;
DELETE FROM store_items WHERE item_id BETWEEN 674 AND 695;

-- Insert weapons
INSERT INTO items (id, item_type, wear_location, name, description, stackable, count)
VALUES
  (674, 'WEAPON', 'PRIMARY', 'Rusty Shortsword', 'A rusty iron shortsword with a nicked blade.', FALSE, 1),
  (675, 'WEAPON', 'PRIMARY', 'Iron Shortsword', 'A sturdy iron shortsword with a clean, sharp edge.', FALSE, 1),
  (676, 'WEAPON', 'PRIMARY', 'Rusty Longsword', 'A heavy longsword showing signs of rust and wear.', FALSE, 1),
  (677, 'WEAPON', 'PRIMARY', 'Iron Longsword', 'A finely balanced iron longsword, polished and sharp.', FALSE, 1),
  (678, 'TWO_HANDED_WEAPON', 'PRIMARY', 'Rusty Two handed sword', 'A massive two-handed sword with a rusted blade and worn hilt.', FALSE, 1),
  (679, 'TWO_HANDED_WEAPON', 'PRIMARY', 'Iron Two handed sword', 'A formidable two-handed iron sword, crafted for devastating sweeps.', FALSE, 1),
  (680, 'WEAPON', 'PRIMARY', 'Rusty Rapier', 'A rusted rapier, thin blade slightly bent but still pointy.', FALSE, 1),
  (681, 'WEAPON', 'PRIMARY', 'Iron Rapier', 'A slender, flexible iron rapier designed for precise thrusts.', FALSE, 1),
  (682, 'WEAPON', 'PRIMARY', 'Rusty Mace', 'A rusted iron mace with dull flanges.', FALSE, 1),
  (683, 'WEAPON', 'PRIMARY', 'Iron Mace', 'A heavy iron mace with sharp flanges, perfect for crushing armor.', FALSE, 1),
  (684, 'WEAPON', 'PRIMARY', 'Wooden Club', 'A simple club fashioned from a sturdy tree branch.', FALSE, 1),
  (685, 'WEAPON', 'PRIMARY', 'Oak Club', 'A solid club carved from seasoned oak wood, heavy and durable.', FALSE, 1),
  (686, 'WEAPON', 'PRIMARY', 'Rusty Broadsword', 'A wide-bladed broadsword, covered in orange rust.', FALSE, 1),
  (687, 'WEAPON', 'PRIMARY', 'Iron Broadsword', 'A broad iron blade, heavy and sharp, designed for cleaving.', FALSE, 1),
  (688, 'WEAPON', 'PRIMARY', 'Rusty War Hammer', 'A rusted war hammer, its heavy head showing pits of decay.', FALSE, 1),
  (689, 'WEAPON', 'PRIMARY', 'Iron War Hammer', 'A solid iron war hammer, balanced for crushing blows.', FALSE, 1),
  (690, 'WEAPON', 'PRIMARY', 'Rusty Flail', 'A rusted spiked ball attached by a worn chain to a wooden handle.', FALSE, 1),
  (691, 'WEAPON', 'PRIMARY', 'Iron Flail', 'A dangerous iron flail with a heavy spiked ball and strong chain.', FALSE, 1),
  (692, 'TWO_HANDED_WEAPON', 'PRIMARY', 'Rusty Pike', 'A rusted spear-like pike on a splintered wooden shaft.', FALSE, 1),
  (693, 'TWO_HANDED_WEAPON', 'PRIMARY', 'Iron Pike', 'A long pole weapon with a sharp, leaf-shaped iron tip.', FALSE, 1),
  (694, 'TWO_HANDED_WEAPON', 'PRIMARY', 'Rusty Quaterstaff', 'A worn, decaying wooden quarterstaff.', FALSE, 1),
  (695, 'TWO_HANDED_WEAPON', 'PRIMARY', 'Iron Quaterstaff', 'A robust wooden quarterstaff reinforced with iron bands at both ends.', FALSE, 1)
ON CONFLICT (id) DO UPDATE SET
  item_type = EXCLUDED.item_type,
  wear_location = EXCLUDED.wear_location,
  name = EXCLUDED.name,
  description = EXCLUDED.description,
  stackable = EXCLUDED.stackable,
  count = EXCLUDED.count;

-- Map items to damage effects
INSERT INTO item_effects (item_id, effect_id)
VALUES
  (674, 155), -- Rusty Shortsword: Slashing Damage 1d6
  (675, 174), -- Iron Shortsword: Slashing Damage 1d8
  (676, 155), -- Rusty Longsword: Slashing Damage 1d6
  (677, 174), -- Iron Longsword: Slashing Damage 1d8
  (678, 155), -- Rusty Two handed sword: Slashing Damage 1d6
  (679, 174), -- Iron Two handed sword: Slashing Damage 1d8
  (680, 154), -- Rusty Rapier: Piercing Damage 1d6
  (681, 173), -- Iron Rapier: Piercing Damage 1d8
  (682, 153), -- Rusty Mace: Bashing Damage 1d6
  (683, 172), -- Iron Mace: Bashing Damage 1d8
  (684, 153), -- Wooden Club: Bashing Damage 1d6
  (685, 172), -- Oak Club: Bashing Damage 1d8
  (686, 155), -- Rusty Broadsword: Slashing Damage 1d6
  (687, 174), -- Iron Broadsword: Slashing Damage 1d8
  (688, 153), -- Rusty War Hammer: Bashing Damage 1d6
  (689, 172), -- Iron War Hammer: Bashing Damage 1d8
  (690, 153), -- Rusty Flail: Bashing Damage 1d6
  (691, 172), -- Iron Flail: Bashing Damage 1d8
  (692, 154), -- Rusty Pike: Piercing Damage 1d6
  (693, 173), -- Iron Pike: Piercing Damage 1d8
  (694, 153), -- Rusty Quaterstaff: Bashing Damage 1d6
  (695, 172)  -- Iron Quaterstaff: Bashing Damage 1d8
ON CONFLICT (item_id, effect_id) DO NOTHING;

-- Link items to Mooncrest Weapons store (store_id = 2)
INSERT INTO store_items (store_id, item_id, created_by, modified_by)
VALUES
  (2, 674, 'system', 'system'),
  (2, 675, 'system', 'system'),
  (2, 676, 'system', 'system'),
  (2, 677, 'system', 'system'),
  (2, 678, 'system', 'system'),
  (2, 679, 'system', 'system'),
  (2, 680, 'system', 'system'),
  (2, 681, 'system', 'system'),
  (2, 682, 'system', 'system'),
  (2, 683, 'system', 'system'),
  (2, 684, 'system', 'system'),
  (2, 685, 'system', 'system'),
  (2, 686, 'system', 'system'),
  (2, 687, 'system', 'system'),
  (2, 688, 'system', 'system'),
  (2, 689, 'system', 'system'),
  (2, 690, 'system', 'system'),
  (2, 691, 'system', 'system'),
  (2, 692, 'system', 'system'),
  (2, 693, 'system', 'system'),
  (2, 694, 'system', 'system'),
  (2, 695, 'system', 'system')
ON CONFLICT (store_id, item_id) DO NOTHING;

-- Synchronize sequence
SELECT setval(pg_get_serial_sequence('items', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM items;
