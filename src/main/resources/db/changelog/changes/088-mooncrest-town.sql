--liquibase formatted sql
--changeset jeff:088-mooncrest-town

-- Relocate any characters in the rooms we're about to delete (to avoid foreign key constraint violations)
UPDATE characters SET current_room_id = 1 WHERE current_room_id != 666 AND current_room_id < 20000;

-- Clean up visited rooms for the ones we're about to delete
DELETE FROM mobile_visited_rooms WHERE room_id != 666 AND room_id < 20000;

-- Delete old seed rooms (keeping Admin Realm 666 and Player Houses >= 20000)
DELETE FROM rooms WHERE id != 666 AND id < 20000;

-- Insert Mooncrest Town Center
INSERT INTO rooms (id, name, description, room_type, north_id, south_id, east_id, west_id, room_persist, room_house, day_light_value, night_light_value) VALUES
(1, 'Town Center', 'You stand in the bustling center of Mooncrest. A grand fountain splashes merrily in the middle of the cobblestone plaza. Roads stretch out in all four cardinal directions, leading to the various districts of the town.', 'CITY', 10, 13, 6, 2, false, false, 8, 2);

-- Insert West Main Street
INSERT INTO rooms (id, name, description, room_type, east_id, west_id, north_id, room_persist, room_house, day_light_value, night_light_value) VALUES
(2, 'West Main Street', 'The western stretch of Main Street begins here, lined with quaint shops and colorful awnings. The Town Center lies to the east.', 'CITY', 1, 3, 24, false, false, 8, 2),
(3, 'West Main Street', 'You are walking along West Main Street. The foot traffic here is steady, consisting mostly of locals running their daily errands.', 'CITY', 2, 4, 25, false, false, 8, 2),
(4, 'West Main Street', 'Further down West Main Street, the buildings start to appear a bit more residential and quiet.', 'CITY', 3, 5, 26, false, false, 8, 2),
(5, 'West Main Street', 'This is the western end of Main Street. The road abruptly ends at the high stone wall that surrounds Mooncrest. You can only head back to the east from here.', 'CITY', 4, NULL, NULL, false, false, 8, 2);

-- Insert West Main Street Shops
INSERT INTO rooms (id, name, description, room_type, south_id, room_persist, room_house, day_light_value, night_light_value) VALUES
(24, 'Housing Store', 'A large showroom containing various models of homes and building materials. The clerk stands ready to assist you with real estate.', 'INDOORS', 2, false, false, 6, 2),
(25, 'Blue Dragon Inn', 'The common room of the Blue Dragon Inn is bustling with adventurers. A warm fire crackles in the hearth.', 'INDOORS', 3, false, false, 6, 2),
(26, 'Magic Shop', 'The air here smells of ozone and strange spices. Potions bubble in glass vials and glowing trinkets line the shelves.', 'INDOORS', 4, false, false, 6, 2);

-- Insert East Main Street
INSERT INTO rooms (id, name, description, room_type, east_id, west_id, north_id, room_persist, room_house, day_light_value, night_light_value) VALUES
(6, 'East Main Street', 'The eastern stretch of Main Street opens up here. Artisans and street vendors have set up small stalls along the edges of the road.', 'CITY', 7, 1, 27, false, false, 8, 2),
(7, 'East Main Street', 'You are on East Main Street. The smell of fresh bread and roasted nuts drifts from a nearby bakery.', 'CITY', 8, 6, 28, false, false, 8, 2),
(8, 'East Main Street', 'The street continues east, growing slightly narrower as the buildings lean slightly towards each other over the cobblestones.', 'CITY', 9, 7, NULL, false, false, 8, 2),
(9, 'East Main Street', 'You have reached the eastern dead end of Main Street. A large, ornate gate stands locked here, barring exit from the town.', 'CITY', NULL, 8, NULL, false, false, 8, 2);

-- Insert East Main Street Shops
INSERT INTO rooms (id, name, description, room_type, south_id, room_persist, room_house, day_light_value, night_light_value) VALUES
(27, 'Temple of Healing', 'A serene and quiet sanctuary. Priests and clerics move softly, tending to the sick and offering prayers for the fallen.', 'INDOORS', 6, false, false, 6, 2),
(28, 'Fighters Guild', 'The sounds of clashing practice swords echo in this large, open hall. Seasoned warriors stand around exchanging tales of battle.', 'INDOORS', 7, false, false, 6, 2);

-- Insert Market Street
INSERT INTO rooms (id, name, description, room_type, north_id, south_id, east_id, west_id, room_persist, room_house, day_light_value, night_light_value) VALUES
(10, 'Market Street', 'You are on Market Street, heading north from the Town Center. The area is filled with colorful tents and the loud haggling of merchants.', 'CITY', 11, 1, 20, 21, false, false, 8, 2),
(11, 'Market Street', 'The heart of Market Street. Goods from all over the realm are displayed on wooden tables and thick rugs. The noise is almost deafening.', 'CITY', 12, 10, 22, 23, false, false, 8, 2),
(12, 'Market Street', 'The northern end of Market Street. A few quiet stalls sit here, mostly selling rare herbs and trinkets. The town wall blocks further travel north.', 'CITY', NULL, 11, NULL, NULL, false, false, 8, 2);

-- Insert Market Street Shops
INSERT INTO rooms (id, name, description, room_type, east_id, west_id, room_persist, room_house, day_light_value, night_light_value) VALUES
(20, 'Armor Shop', 'A sturdy building filled with the clanking of metal. Racks of gleaming armor line the walls.', 'INDOORS', NULL, 10, false, false, 6, 2),
(21, 'Weapon Shop', 'The sharp tang of oiled steel fills the air. Various swords, axes, and maces are displayed prominently.', 'INDOORS', 10, NULL, false, false, 6, 2),
(22, 'General Store', 'A cluttered but charming shop that seems to sell a little bit of everything.', 'INDOORS', NULL, 11, false, false, 6, 2),
(23, 'Bakery', 'The warm, sweet smell of fresh pastries and bread makes your mouth water. Display cases are filled with delicious treats.', 'INDOORS', 11, NULL, false, false, 6, 2);

-- Insert Castle Lane
INSERT INTO rooms (id, name, description, room_type, north_id, south_id, room_persist, room_house, day_light_value, night_light_value) VALUES
(13, 'Castle Lane', 'Heading south from the Town Center, you enter Castle Lane. The cobblestones here are perfectly laid, and the buildings look prestigious and well-maintained.', 'CITY', 1, 14, false, false, 8, 2),
(14, 'Castle Lane', 'You walk down Castle Lane. Tall elm trees line the street, providing a cool shade. The area is peaceful and quiet.', 'CITY', 13, 15, false, false, 8, 2),
(15, 'Castle Lane', 'Continuing south on Castle Lane. You can see the towering spires of the Mooncrest Castle looming in the distance to the south.', 'CITY', 14, 16, false, false, 8, 2),
(16, 'Castle Lane', 'The lane is flanked by high stone walls of various noble estates. Guards patrol this area regularly.', 'CITY', 15, 17, false, false, 8, 2),
(17, 'Castle Lane', 'You are on Castle Lane. The imposing presence of the castle to the south dominates the skyline.', 'CITY', 16, 18, false, false, 8, 2),
(18, 'Castle Lane', 'The lane approaches the castle grounds. Banners bearing the crest of Mooncrest flutter in the wind.', 'CITY', 17, 19, false, false, 8, 2),
(19, 'Castle Lane', 'You stand before the massive iron gates of Mooncrest Castle. The gate is drawn shut, and heavily armed guards stand watch. You cannot proceed any further south.', 'CITY', 18, NULL, false, false, 8, 2);

-- Create Stores for Shopkeepers
INSERT INTO stores (id, name, description) VALUES
(1, 'Mooncrest Armor', 'A sturdy shop providing the finest protective gear in town.'),
(2, 'Mooncrest Weapons', 'A shop selling sharp and deadly armaments for adventurers.'),
(3, 'Mooncrest General Goods', 'Your one-stop shop for everything you need on the road.'),
(4, 'Mooncrest Bakery', 'A warm bakery selling fresh, delicious goods.'),
(5, 'Mooncrest Real Estate', 'A specialized firm handling property deeds and housing.'),
(6, 'Blue Dragon Tavern', 'A lively tavern providing drinks and a place to rest.'),
(7, 'Arcane Emporium', 'A mysterious store selling magical reagents and potions.'),
(8, 'Temple Sanctuary', 'A holy place offering healing services and blessings.'),
(9, 'Fighters Guild Quartermaster', 'A guild armory supplying certified combatants.');

-- Create Shopkeeper Mobiles
-- Assuming race_id 1 (Human), 2 (Elf), 3 (Dwarf) and class_id 1 (Fighter), 2 (Cleric), 3 (Mage), 4 (Thief) exist
ALTER TABLE mobiles ADD COLUMN IF NOT EXISTS description TEXT;
INSERT INTO mobiles (id, name, description, race_id, class_id, current_room_id, current_hp, current_mana, non_combat, uses_ai, agent, store_id) VALUES
(1001, 'Grom', 'A burly dwarven smith with soot-stained hands.', 3, 1, 20, 1000, 100, true, true, 'You are Grom, a gruff but honest dwarven blacksmith who sells armor. You love talking about metalwork.', 1),
(1002, 'Kaelen', 'A sharp-eyed elven weaponsmith, constantly sharpening a blade.', 2, 1, 21, 1000, 100, true, true, 'You are Kaelen, an elven weaponsmith. You are meticulous and view weapons as art.', 2),
(1003, 'Martha', 'A cheerful middle-aged human woman bustling around the store.', 1, 4, 22, 500, 100, true, true, 'You are Martha, the friendly and gossipy owner of the General Store. You know a little bit about everything.', 3),
(1004, 'Pip', 'A jovial halfling covered in flour.', 1, 4, 23, 300, 100, true, true, 'You are Pip, a bubbly halfling baker. You constantly try to sell people on your new pastry recipes.', 4),
(1005, 'Silas', 'A well-dressed, fast-talking human merchant.', 1, 4, 24, 400, 100, true, true, 'You are Silas, a smooth-talking real estate agent. You sell Housing Writs and Expansion Permits. You love money.', 5),
(1006, 'Barnaby', 'A hearty, red-faced innkeeper wiping down the bar.', 1, 1, 25, 800, 100, true, true, 'You are Barnaby, the boisterous innkeeper of the Blue Dragon Inn. You are friendly to adventurers and have many rumors to share.', 6),
(1007, 'Elara', 'An enigmatic elven mage surrounded by floating books.', 2, 3, 26, 600, 1000, true, true, 'You are Elara, an aloof but brilliant mage who runs the Arcane Emporium. You find mundane talk tedious.', 7),
(1008, 'Brother Thomas', 'A serene human cleric wearing robes of pure white.', 1, 2, 27, 800, 800, true, true, 'You are Brother Thomas, a gentle and compassionate cleric at the Temple of Healing. You offer guidance and peace.', 8),
(1009, 'Commander Vane', 'A battle-scarred veteran with a stern gaze.', 1, 1, 28, 1500, 100, true, true, 'You are Commander Vane, the gruff and strict quartermaster of the Fighters Guild. You respect strength and discipline.', 9);

-- Make sure to sync sequence numbers so future inserts do not collide
SELECT setval(pg_get_serial_sequence('stores', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM stores;
SELECT setval(pg_get_serial_sequence('mobiles', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM mobiles;
