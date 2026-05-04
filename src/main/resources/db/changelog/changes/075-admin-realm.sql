-- liquibase formatted sql
-- changeset jeff:075

-- 1. Create the Admin Store
INSERT INTO
    stores (
        id,
        name,
        description,
        created_by
    )
VALUES (
        666,
        'Admin Store',
        'A cosmic storefront for the administrators of the realm.',
        'system'
    )
ON CONFLICT (id) DO NOTHING;

-- 2. Create the Admin Vendor NPC
INSERT INTO
    mobiles (
        id,
        name,
        current_room_id,
        store_id,
        strength,
        dexterity,
        constitution,
        intelligence,
        wisdom,
        charisma,
        created_by,
        non_combat
    )
VALUES (
        666,
        'Admin Vendor',
        666,
        666,
        50,
        50,
        50,
        50,
        50,
        50,
        'system',
        true
    )
ON CONFLICT (id) DO NOTHING;

-- 3. Create the Items
-- Statue
INSERT INTO
    items (
        id,
        name,
        description,
        item_type,
        wear_location,
        no_pickup,
        created_by
    )
VALUES (
        666,
        'Statue of Strager Darkheart',
        'An imposing, immortalized visage of Strager Darkheart. It hums with immense power.',
        'MISC',
        'NONE',
        true,
        'system'
    )
ON CONFLICT (id) DO NOTHING;

-- Admin Chest
INSERT INTO
    items (
        id,
        name,
        description,
        item_type,
        wear_location,
        no_pickup,
        property_1,
        property_2,
        created_by
    )
VALUES (
        667,
        'Admin Chest',
        'A sturdy, otherworldly chest holding equipment of profound power.',
        'CONTAINER',
        'NONE',
        true,
        1000,
        1000,
        'system'
    )
ON CONFLICT (id) DO NOTHING;

-- 1 Million Gold
INSERT INTO
    items (
        id,
        name,
        description,
        item_type,
        wear_location,
        no_pickup,
        property_1,
        created_by
    )
VALUES (
        668,
        'Pile of 1 Million Gold',
        'An enormous pile of glowing gold coins.',
        'MONEY',
        'NONE',
        false,
        1000000,
        'system'
    )
ON CONFLICT (id) DO NOTHING;

-- 4. Create the Room
INSERT INTO
    rooms (
        id,
        name,
        description,
        room_type,
        items,
        day_light_value,
        night_light_value,
        created_by
    )
VALUES (
        666,
        'Realm of the Admins',
        'An otherworldly place where reality bends to the will of the creators. Swirling nebulae substitute for a sky, and the ground is an expanse of crystalline energy.',
        'INDOORS',
        '666,667,668',
        10,
        10,
        'system'
    )
ON CONFLICT (id) DO NOTHING;

-- 1. Insert Items with Hard-coded IDs
INSERT INTO
    items (
        id,
        item_type,
        wear_location,
        name,
        description,
        property_1,
        property_2,
        property_3,
        property_4
    )
VALUES
    -- Melee Weapons (One-Handed)
    (
        100,
        'WEAPON',
        'PRIMARY',
        'The Alpha Stroke',
        'A sword of pure light that marks the beginning of all things.',
        10,
        20,
        25,
        0
    ), -- Slashing
    (
        101,
        'WEAPON',
        'PRIMARY',
        'The Final Period',
        'A heavy mace used to end arguments and existences alike.',
        10,
        20,
        25,
        0
    ), -- Bashing
    (
        102,
        'WEAPON',
        'PRIMARY',
        'Logic''s Needle',
        'A thin rapier that pierces through the fabric of the weave.',
        10,
        20,
        25,
        0
    ), -- Piercing
    (
        103,
        'WEAPON',
        'PRIMARY',
        'Cinder of Creation',
        'A blade that burns with the heat of a thousand dying stars.',
        10,
        20,
        25,
        0
    ), -- Fire
    (
        104,
        'WEAPON',
        'PRIMARY',
        'Shard of the Void',
        'A frozen edge that saps the heat from the universe.',
        10,
        20,
        25,
        0
    ), -- Cold
    (
        105,
        'WEAPON',
        'PRIMARY',
        'Resonance of Truth',
        'A hammer that vibrates with the frequency of reality.',
        10,
        20,
        25,
        0
    ), -- Sonic
    (
        106,
        'WEAPON',
        'PRIMARY',
        'Toxin of Non-Existence',
        'A dagger coated in a poison that deletes the soul.',
        10,
        20,
        25,
        0
    ), -- Poison
    (
        107,
        'WEAPON',
        'PRIMARY',
        'Static of the Source',
        'A spear crackling with the raw electricity of the server core.',
        10,
        20,
        25,
        0
    ), -- Electrical
    (
        108,
        'WEAPON',
        'PRIMARY',
        'Staff of the Weaver',
        'A gnarled staff of ancient wood that pulses with the heartbeat of the world.',
        10,
        20,
        25,
        0
    ), -- Bashing/Magic

-- Two-Handed and Ranged
(
    109,
    'TWO_HANDED_WEAPON',
    'PRIMARY',
    'Reality''s Edge',
    'A massive claymore that splits dimensions.',
    10,
    20,
    25,
    0
),
(
    110,
    'RANGED_WEAPON',
    'PRIMARY',
    'System Pulse',
    'A bow that fires bolts of pure raw data.',
    10,
    20,
    25,
    0
),

-- Heavy Armor Set (The Celestial)
(
    111,
    'HEAVY_ARMOR',
    'CHEST',
    'Breastplate of the Celestial',
    'The core protection worn by the world-builders.',
    25,
    0,
    0,
    0
),
(
    112,
    'HEAVY_ARMOR',
    'HEAD',
    'Great-Helm of the Celestial',
    'A massive crown of iron and light.',
    25,
    0,
    0,
    0
),
(
    113,
    'HEAVY_ARMOR',
    'LEGS',
    'Greaves of the Celestial',
    'Leg protection that grounds the wearer in existence.',
    25,
    0,
    0,
    0
),
(
    114,
    'HEAVY_ARMOR',
    'FEET',
    'Sabatons of the Celestial',
    'Boots that leave footprints of gold in the data.',
    25,
    0,
    0,
    0
),
(
    115,
    'HEAVY_ARMOR',
    'HANDS',
    'Gauntlets of the Celestial',
    'Gloves used to grab and move the sun.',
    25,
    0,
    0,
    0
),

-- Medium Armor Set (The Celestial)
(
    116,
    'MEDIUM_ARMOR',
    'CHEST',
    'Hauberk of the Celestial',
    'Reinforced mail that balances protection with perfect mobility.',
    25,
    0,
    0,
    0
),
(
    117,
    'MEDIUM_ARMOR',
    'HEAD',
    'Circlet of the Celestial',
    'A silver band that sharpens the wearer''s focus.',
    25,
    0,
    0,
    0
),
(
    118,
    'MEDIUM_ARMOR',
    'LEGS',
    'Chausses of the Celestial',
    'Leggings designed for the tireless observation of all things.',
    25,
    0,
    0,
    0
),
(
    119,
    'MEDIUM_ARMOR',
    'HANDS',
    'Gloves of the Celestial',
    'Supple leather reinforced with silvered chain.',
    25,
    0,
    0,
    0
),
(
    120,
    'MEDIUM_ARMOR',
    'FEET',
    'Boots of the Celestial',
    'Sturdy boots that carry the observer through every layer of reality.',
    25,
    0,
    0,
    0
),

-- Light Armor Set (The Celestial)
(
    121,
    'LIGHT_ARMOR',
    'CHEST',
    'Vestment of the Celestial',
    'A gossamer tunic woven from the very fabric of the dreamscape.',
    25,
    0,
    0,
    0
),
(
    122,
    'LIGHT_ARMOR',
    'HEAD',
    'Cowl of the Celestial',
    'A hood that obscures the face with drifting data particles.',
    25,
    0,
    0,
    0
),
(
    123,
    'LIGHT_ARMOR',
    'LEGS',
    'Pantaloons of the Celestial',
    'Silk trousers that feel as light as air itself.',
    25,
    0,
    0,
    0
),
(
    124,
    'LIGHT_ARMOR',
    'HANDS',
    'Wraps of the Celestial',
    'Fine silk bandages that guide the hands of the weaver.',
    25,
    0,
    0,
    0
),
(
    125,
    'LIGHT_ARMOR',
    'FEET',
    'Steps of the Celestial',
    'Footwraps that make no sound upon the weave.',
    25,
    0,
    0,
    0
),
(
    126,
    'LIGHT_ARMOR',
    'FINGER',
    'Signet of the Celestial',
    'A ring of shifting light that leaves a trail in the air.',
    25,
    0,
    0,
    0
),
(
    127,
    'LIGHT_ARMOR',
    'WRIST',
    'Bracers of the Celestial',
    'Shimmering bands that pulse with the server''s heartbeat.',
    25,
    0,
    0,
    0
),
(
    128,
    'LIGHT_ARMOR',
    'NECK',
    'Collar of the Celestial',
    'A choker of pure energy that protects the wearer''s essence.',
    25,
    0,
    0,
    0
),
(
    129,
    'LIGHT_ARMOR',
    'EAR',
    'Stud of the Celestial',
    'A tiny crystal that whispers the secrets of the code.',
    25,
    0,
    0,
    0
),
(
    130,
    'LIGHT_ARMOR',
    'FACE',
    'Veil of the Celestial',
    'A translucent mask that filters out the lies of reality.',
    25,
    0,
    0,
    0
),
(
    131,
    'LIGHT_ARMOR',
    'WAIST',
    'Sash of the Celestial',
    'A belt made of woven light that holds the universe together.',
    25,
    0,
    0,
    0
),

-- Special Admin Artifact
(
    132,
    'LIGHT_ARMOR',
    'FINGER',
    'Master''s Ring',
    'A ring that grants total dominion over the physical and ethereal planes.',
    25,
    0,
    0,
    0
),

-- Consumables & Utility
(
    133,
    'POTION',
    'NONE',
    'Elixir of Root Access',
    'A vial of shimmering liquid that restores all faculties.',
    0,
    0,
    0,
    0
),
(
    134,
    'FOOD',
    'NONE',
    'Manna of the Gods',
    'Sustenance that removes the need for mortal food.',
    100,
    100,
    0,
    0
),
(
    135,
    'DRINK',
    'NONE',
    'Nectar of Eternity',
    'A liquid that quenches the thirst of the divine.',
    100,
    100,
    0,
    0
),
(
    136,
    'CONTAINER',
    'NONE',
    'The Infinite Repository',
    'A bag that holds more than the world itself.',
    10000,
    10000,
    0,
    0
),
(
    137,
    'LIGHT',
    'NONE',
    'The Sun Fragment',
    'A light that never fades.',
    99999,
    0,
    0,
    0
),
(
    138,
    'KEY',
    'NONE',
    'Master Override Key',
    'A key that fits any lock in existence.',
    0,
    0,
    0,
    0
),
(
    139,
    'BANDAGE',
    'NONE',
    'Miracle Wrap',
    'A legendary wrap glowing with overwhelming restorative energy.',
    10,
    10,
    1,
    1
),
(
    140,
    'BOOK',
    'NONE',
    'The Developer Log',
    'A book containing the secrets of the world''s creation.',
    0,
    0,
    0,
    0
),
(
    141,
    'SCROLL',
    'NONE',
    'Scroll of Absolute Deletion',
    'A command on parchment to erase an enemy.',
    0,
    0,
    0,
    0
);

-- 2. Link Effects using hard-coded IDs

-- 2.1 Weapons: 10d20 Damage (532-539) + Physical Attack +25 (130)
INSERT INTO
    item_effects (item_id, effect_id)
VALUES (100, 534),
    (100, 130), -- Alpha Stroke: Slash
    (101, 532),
    (101, 130), -- Final Period: Bash
    (102, 533),
    (102, 130), -- Logic Needle: Pierce
    (103, 535),
    (103, 130), -- Cinder: Fire
    (104, 536),
    (104, 130), -- Shard: Cold
    (105, 537),
    (105, 130), -- Resonance: Sonic
    (106, 538),
    (106, 130), -- Toxin: Poison
    (107, 539),
    (107, 130), -- Static: Electric
    (108, 532),
    (108, 130), -- Staff: Bash
    (109, 534),
    (109, 130), -- Edge: Slash 2H
    (110, 533),
    (110, 130);
-- Pulse: Pierce Ranged

-- 2.2 Celestial Set & Master's Ring: +25 STR(13), DEX(26), INT(39), WIS(52), CHA(65), CON(78) and ARMOR(91)
INSERT INTO
    item_effects (item_id, effect_id)
SELECT i.id, e.eid
FROM items i
    CROSS JOIN (
        SELECT unnest(
                ARRAY[13, 26, 39, 52, 65, 78, 91]
            ) as eid
    ) e
WHERE
    i.id BETWEEN 111 AND 132;

-- 2.3 Special Effects (Mana Regen, Darkvision, Fly, etc.)
INSERT INTO
    item_effects (item_id, effect_id)
VALUES
    -- Master's Ring: Fly(161), WaterBreath(162), Invis(163), Darkvision(3209)
    (132, 161),
    (132, 162),
    (132, 163),
    (132, 3209),
    -- Staff: ManaRegen(117), Darkvision(3209)
    (108, 117),
    (108, 3209),
    -- Signet: ManaRegen(117), Darkvision(3209), Atk(130), Crit(152)
    (126, 117),
    (126, 3209),
    (126, 130),
    (126, 152),
    -- Bracers: Dodge(149)
    (127, 149),
    -- Collar: ManaRegen(117), Darkvision(3209), HPRegen(104), MagicResist(146)
    (128, 117),
    (128, 3209),
    (128, 104),
    (128, 146),
    -- Stud: ManaRegen(117), Darkvision(3209), MagicAtk(143)
    (129, 117),
    (129, 3209),
    (129, 143),
    -- Veil: Invis(163)
    (130, 163),
    -- Sabatons: HPRegen(104)
    (114, 104),
    -- Gauntlets: Crit(152)
    (115, 152),
    -- Pantaloons: ManaRegen(117)
    (123, 117),
    -- Sash: ManaRegen(117)
    (131, 117);

-- Add all divine items (100-141) to the Admin Store
INSERT INTO
    store_items (
        store_id,
        item_id,
        created_by,
        modified_by
    )
SELECT 666, id, 'system', 'system'
FROM items
WHERE
    id BETWEEN 100 AND 141
ON CONFLICT (store_id, item_id) DO NOTHING;

-- 4. Synchronize Sequences
SELECT setval(
        pg_get_serial_sequence('items', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL
    )
FROM items;

SELECT setval(
        pg_get_serial_sequence('stores', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL
    )
FROM stores;
