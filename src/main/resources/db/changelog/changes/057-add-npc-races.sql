--changeset antigravity:57
INSERT INTO
    races (
        name,
        description,
        strength_mod,
        intelligence_mod,
        wisdom_mod,
        charisma_mod,
        dexterity_mod,
        constitution_mod,
        npc_only
    )
VALUES
    -- Humanoids & Goblinoids
    (
        'Goblin',
        'Small, cunning, and often malicious creatures that thrive in dark caves and forests.',
        -1,
        0,
        0,
        -1,
        2,
        0,
        true
    ),
    (
        'Hobgoblin',
        'Larger and more disciplined cousins of the goblin, highly organized and militant.',
        1,
        0,
        0,
        -1,
        1,
        1,
        true
    ),
    (
        'Bugbear',
        'Massive, hairy goblinoids who rely on stealth and brute force to hunt their prey.',
        2,
        -1,
        -1,
        -1,
        1,
        1,
        true
    ),
    (
        'Kobold',
        'Diminutive, reptilian humanoids who claim kinship with dragons and favor traps.',
        -2,
        0,
        -1,
        -1,
        2,
        0,
        true
    ),
    (
        'Troll',
        'Towering, fearsome giants with legendary regenerative abilities and voracious appetites.',
        3,
        -2,
        -2,
        -2,
        0,
        3,
        true
    ),
    (
        'Ogre',
        'Large, brutish giants characterized by their immense strength and lack of intellect.',
        3,
        -2,
        -1,
        -2,
        -1,
        2,
        true
    ),
    (
        'Gnoll',
        'Ferocious hyena-headed humanoids driven by a demonic thirst for slaughter.',
        2,
        -1,
        0,
        -1,
        1,
        1,
        true
    ),

-- Bestial & Animalistic
(
    'Minotaur',
    'Powerful monstrosities with the body of a massive human and the head of a bull.',
    2,
    -1,
    0,
    -1,
    0,
    2,
    true
),
(
    'Centaur',
    'Noble creatures with the upper body of a human and the lower body of a horse.',
    1,
    0,
    1,
    0,
    1,
    1,
    true
),
(
    'Lizardman',
    'Cold-blooded, primal reptilian humanoids dwelling in swamps and marshes.',
    1,
    -1,
    0,
    -1,
    1,
    2,
    true
),
(
    'Sahuagin',
    'Vicious aquatic humanoids who raid coastal settlements and worship deep-sea entities.',
    1,
    0,
    0,
    -1,
    1,
    1,
    true
),
(
    'Bullywug',
    'Amphibious frog-like humanoids living in squalid tribal communities.',
    0,
    -1,
    0,
    -1,
    1,
    1,
    true
),
(
    'Yuan-Ti',
    'Serpentine humanoids born from ancient, dark rituals that merged humans with snakes.',
    0,
    1,
    0,
    1,
    1,
    0,
    true
),
(
    'Kenku',
    'Avian humanoids stripped of their ability to fly and forced to mimick sounds.',
    0,
    0,
    0,
    0,
    2,
    0,
    true
),
(
    'Tabaxi',
    'Agile, feline humanoids driven by curiosity and a desire for rare artifacts.',
    0,
    0,
    0,
    1,
    2,
    0,
    true
),
(
    'Aarakocra',
    'Bird-like humanoids native to the Elemental Plane of Air.',
    0,
    0,
    1,
    0,
    2,
    0,
    true
),
(
    'Tortle',
    'Tough, nomadic reptilians carrying their massive shells on their backs.',
    1,
    0,
    1,
    0,
    -1,
    1,
    true
),
(
    'Locathah',
    'Peaceful, fish-like humanoids dwelling in the shimmering depths of the ocean.',
    1,
    0,
    0,
    0,
    1,
    0,
    true
),
(
    'Grung',
    'Small, colorful tree-frog humanoids sporting highly toxic skin.',
    -1,
    0,
    0,
    -1,
    2,
    1,
    true
),
(
    'Myconid',
    'Sentient fungus creatures communicating via telepathic spores.',
    0,
    0,
    1,
    -1,
    0,
    2,
    true
),
(
    'Thri-Kreen',
    'Mantis-like insectoids adapted to surviving harsh desert environments.',
    1,
    0,
    1,
    -1,
    2,
    0,
    true
),
(
    'Kuo-Toa',
    'Mad, fish-like humanoids living in the Underdark and worshipping imagined gods.',
    0,
    -1,
    0,
    -1,
    0,
    1,
    true
),

-- Elemental & Planar
(
    'Aasimar',
    'Mortals infused with celestial energy, often tasked as champions of the divine.',
    0,
    0,
    1,
    2,
    0,
    0,
    true
),
(
    'Tiefling',
    'Humanoids bearing infernal heritage and demonic features like horns and tails.',
    0,
    1,
    0,
    2,
    0,
    0,
    true
),
(
    'Fire Genasi',
    'Beings shaped by the elemental plane of fire, possessing burning passions.',
    0,
    1,
    0,
    0,
    0,
    2,
    true
),
(
    'Water Genasi',
    'Fluid, adaptable humanoids linked to the elemental plane of water.',
    0,
    0,
    1,
    0,
    0,
    2,
    true
),
(
    'Earth Genasi',
    'Solid, unyielding people touched by the elemental plane of earth.',
    1,
    0,
    0,
    0,
    0,
    2,
    true
),
(
    'Air Genasi',
    'Carefree and aloof beings born of the elemental plane of air.',
    0,
    0,
    0,
    0,
    1,
    2,
    true
),
(
    'Githyanki',
    'Militaristic astral travelers wielding silver swords and riding red dragons.',
    2,
    1,
    0,
    0,
    0,
    0,
    true
),
(
    'Githzerai',
    'Ascetic and disciplined philosophers dwelling in the chaotic plane of Limbo.',
    0,
    1,
    2,
    0,
    0,
    0,
    true
),
(
    'Salamander',
    'Serpentine elementals of fire and wrought iron.',
    1,
    0,
    0,
    0,
    1,
    1,
    true
),
(
    'Sylph',
    'Graceful creatures of the air, often seen as natural spirits.',
    0,
    1,
    0,
    1,
    2,
    -1,
    true
),
(
    'Undine',
    'Playful and reclusive beings at one with lakes and rivers.',
    0,
    0,
    1,
    0,
    1,
    1,
    true
),
(
    'Ifrit',
    'Proud, fiery people of efreeti descent.',
    1,
    1,
    0,
    1,
    0,
    0,
    true
),
(
    'Oread',
    'Stoic creatures infused with the immutable essence of stone.',
    2,
    0,
    1,
    -1,
    -1,
    1,
    true
),

-- Fae & Sylvan
(
    'Satyr',
    'Hedonistic fey with the horns and legs of a goat, prone to revelry.',
    0,
    0,
    0,
    2,
    1,
    0,
    true
),
(
    'Dryad',
    'Shy but fiercely protective nature spirits bound to ancient trees.',
    0,
    1,
    1,
    2,
    1,
    0,
    true
),
(
    'Nymph',
    'Breathtakingly beautiful fey beings embodying the pristine wilderness.',
    -1,
    1,
    1,
    3,
    1,
    0,
    true
),
(
    'Sprite',
    'Tiny fey warriors who aggressively defend their domains with miniature weapons.',
    -2,
    1,
    0,
    0,
    2,
    -1,
    true
),
(
    'Pixie',
    'Mischievous, magical beings known for pranks and invisibility.',
    -2,
    1,
    0,
    2,
    2,
    -1,
    true
),
(
    'Changeling',
    'Shapeshifting offspring born from fey deception.',
    0,
    0,
    0,
    2,
    1,
    0,
    true
),
(
    'Fairy',
    'Tiny, winged folk brimming with wild magic.',
    -1,
    0,
    0,
    1,
    2,
    0,
    true
),
(
    'Puck',
    'Chaotic fey tricksters favoring elaborate illusions.',
    0,
    0,
    0,
    1,
    2,
    0,
    true
),

-- Undead
(
    'Skeleton',
    'Animated bones bound by dark magic, utterly devoid of thought.',
    0,
    -2,
    -2,
    -2,
    1,
    0,
    true
),
(
    'Zombie',
    'Rotting corpses sustained by necromancy, slow but relentlessly strong.',
    2,
    -2,
    -2,
    -2,
    -2,
    3,
    true
),
(
    'Ghoul',
    'Flesh-eating undead cursed with an insatiable hunger.',
    1,
    -1,
    -1,
    -2,
    1,
    1,
    true
),
(
    'Ghast',
    'Stronger, foul-smelling relatives of ghouls.',
    2,
    -1,
    -1,
    -2,
    1,
    1,
    true
),
(
    'Wight',
    'Malicious undead hunting the living to drain their life force.',
    1,
    0,
    0,
    -1,
    0,
    2,
    true
),
(
    'Wraith',
    'Incorporeal beings of pure malice and darkness.',
    -2,
    0,
    0,
    -1,
    2,
    0,
    true
),
(
    'Vampire',
    'Undead aristocrats demanding blood to sustain their immortality.',
    2,
    1,
    1,
    2,
    1,
    1,
    true
),
(
    'Lich',
    'Powerful spellcasters who embraced undeath to live forever.',
    0,
    4,
    2,
    1,
    0,
    2,
    true
),
(
    'Mummy',
    'Embalmed undead guardians bound by ancient curses.',
    2,
    -1,
    0,
    -1,
    -2,
    3,
    true
),
(
    'Ghost',
    'Restless spirits bound to the world by unfinished business.',
    -2,
    0,
    0,
    1,
    0,
    0,
    true
),

-- Constructs & Artificial
(
    'Warforged',
    'Sentient golems originally built as soldiers for long-forgotten wars.',
    1,
    0,
    0,
    -1,
    0,
    2,
    true
),
(
    'Flesh Golem',
    'A patchwork of corpse parts animated by elemental lightning.',
    2,
    -2,
    -1,
    -2,
    -1,
    2,
    true
),
(
    'Clay Golem',
    'A heavy, divine-powered construct often used as a tomb guardian.',
    3,
    -2,
    -1,
    -2,
    -2,
    3,
    true
),
(
    'Stone Golem',
    'A massive, nearly indestructible statue brought to animated life.',
    4,
    -2,
    -1,
    -2,
    -2,
    4,
    true
),
(
    'Iron Golem',
    'The ultimate construct, wielding a massive blade and breathing poison gas.',
    5,
    -2,
    -1,
    -2,
    -1,
    5,
    true
),
(
    'Animated Armor',
    'An empty suit of platemail bound by protective magic.',
    1,
    -2,
    -2,
    -2,
    0,
    2,
    true
),
(
    'Gargoyle',
    'A malicious flying creature made of animated stone.',
    1,
    -1,
    0,
    -1,
    1,
    2,
    true
),
(
    'Automaton',
    'Clockwork entities capable of repetitive tasks.',
    0,
    0,
    -1,
    -2,
    1,
    1,
    true
),

-- Monolithic & Giant-Kin
(
    'Goliath',
    'Massive mountain-dwelling nomads with skin like jagged stone.',
    2,
    0,
    0,
    0,
    0,
    1,
    true
),
(
    'Firbolg',
    'Gentle, nature-loving giant-kin living in absolute harmony with the forest.',
    1,
    0,
    2,
    0,
    0,
    0,
    true
),
(
    'Giant (Hill)',
    'Gluttonous and primitive giants with incredibly low intellects.',
    4,
    -2,
    -1,
    -2,
    -1,
    3,
    true
),
(
    'Giant (Stone)',
    'Reclusive, artistic giants preferring the silence of vast caverns.',
    3,
    0,
    0,
    0,
    1,
    2,
    true
),
(
    'Giant (Frost)',
    'Fearsome raiders dominating icy wastes with physical supremacy.',
    4,
    0,
    0,
    0,
    -1,
    3,
    true
),
(
    'Giant (Fire)',
    'Militaristic and highly skilled smiths forging immense weapons.',
    5,
    0,
    0,
    -1,
    -1,
    4,
    true
),
(
    'Giant (Cloud)',
    'Wealthy and flamboyant lords of floating castles.',
    4,
    1,
    1,
    2,
    0,
    3,
    true
),
(
    'Giant (Storm)',
    'Prophetic, god-like giants commanding the weather from deep oceans or high peaks.',
    6,
    2,
    2,
    2,
    1,
    5,
    true
),
(
    'Cyclops',
    'One-eyed brutes known for terrible tempers and poor depth perception.',
    3,
    -1,
    -1,
    -1,
    -1,
    2,
    true
),

-- Extraplanar & Aberrations
(
    'Mind Flayer',
    'Tentacle-faced horrors that feast on brains and conquer minds.',
    -1,
    4,
    2,
    1,
    0,
    0,
    true
),
(
    'Beholder',
    'Floating orbs of flesh covered in magical eyestalks and unchecked paranoia.',
    0,
    3,
    2,
    1,
    1,
    2,
    true
),
(
    'Doppelganger',
    'Shapeshifters capable of perfectly mimicking any humanoid they observe.',
    0,
    1,
    0,
    2,
    1,
    0,
    true
),
(
    'Drider',
    'Drow cursed by their dark goddess to become half-spider abominations.',
    1,
    1,
    1,
    0,
    2,
    1,
    true
),
(
    'Hook Horror',
    'Underdark monsters communicating via clicks and wielding massive bone hooks.',
    2,
    -1,
    1,
    -2,
    2,
    2,
    true
),
(
    'Umber Hulk',
    'Massive burrowing insectoids capable of causing severe mental confusion.',
    3,
    -1,
    0,
    -2,
    -1,
    3,
    true
),
(
    'Nothic',
    'Cursed wizards reduced to creeping, cyclopean beasts hoarding secrets.',
    1,
    2,
    0,
    -1,
    1,
    1,
    true
),
(
    'Aboleth',
    'Ancient, aquatic aberrations possessing flawless memories and powerful mind magic.',
    2,
    4,
    2,
    1,
    -1,
    3,
    true
),
(
    'Gibbering Mouther',
    'A shifting mass of eyes, teeth, and mouths babbling maddening sounds.',
    1,
    -2,
    -1,
    -2,
    -2,
    3,
    true
),

-- Dark Dwellers & Deep Races
(
    'Dark Elf',
    'Sinister, subterranean elves serving a spider goddess.',
    0,
    1,
    0,
    1,
    2,
    0,
    true
),
(
    'Deep Dwarf',
    'Grumpy, gray-skinned dwarves adapted to the harsh Underdark.',
    1,
    0,
    0,
    -1,
    0,
    2,
    true
),
(
    'Deep Gnome',
    'Cautious and pragmatic gnomes specialized in hiding and mining.',
    -1,
    1,
    0,
    -1,
    2,
    0,
    true
),
(
    'Derro',
    'Mad, degenerated dwarves plotting surface raids from the deep.',
    0,
    0,
    -1,
    -1,
    2,
    1,
    true
),
(
    'Troglodyte',
    'Foul-smelling repetilian cave dwellers who despise the light.',
    1,
    -1,
    0,
    -1,
    0,
    2,
    true
),
(
    'Grimlock',
    'Blind, cannibalistic humanoids navigating entirely by sound and scent.',
    2,
    -1,
    0,
    -1,
    1,
    1,
    true
),
(
    'Choldrith',
    'Spider-like goblins forming strict matriarchal societies.',
    1,
    0,
    1,
    -1,
    1,
    1,
    true
),
(
    'Morlock',
    'A degenerated branch of humanity, pale, frail, and adapted to deep machinery or tunnels.',
    1,
    0,
    0,
    -2,
    2,
    1,
    true
),

-- Mythical & Cryptids
(
    'Yeti',
    'Fearsome, furry predators stalking blizzards and icy mountain passes.',
    3,
    -1,
    1,
    -1,
    1,
    2,
    true
),
(
    'Sasquatch',
    'Elusive ape-men leaving massive footprints in deep forests.',
    2,
    -1,
    1,
    -1,
    1,
    2,
    true
),
(
    'Rakshasa',
    'Fiendish manipulators sporting backwards hands and feline heads.',
    1,
    2,
    1,
    3,
    2,
    2,
    true
),
(
    'Wendigo',
    'Cursed spirits of starvation bringing madness and winter wherever they tread.',
    2,
    0,
    1,
    1,
    3,
    2,
    true
),
(
    'Merfolk',
    'Aquatic humanoids dwelling in shallow seas and singing to sailors.',
    0,
    0,
    0,
    1,
    1,
    1,
    true
),
(
    'Siren',
    'Bird-women or fish-women who use their voices to lure victims to their doom.',
    -1,
    1,
    0,
    3,
    1,
    0,
    true
),
(
    'Harpy',
    'Vicious vulture-women enjoying the torment of lesser creatures.',
    0,
    -1,
    0,
    1,
    2,
    0,
    true
),
(
    'Medusa',
    'Cursed beings transforming anyone who meets their gaze into pure stone.',
    0,
    1,
    1,
    2,
    2,
    1,
    true
),

-- Swarm & Parasitic
(
    'Tsochar',
    'Alien, parasitic worms that take over the bodies of their victims.',
    -1,
    3,
    2,
    -1,
    2,
    2,
    true
),
(
    'Cranium Rat Swarm',
    'A hive-mind of rats capable of exhibiting intense psionic powers.',
    -2,
    4,
    1,
    0,
    2,
    1,
    true
),
(
    'Intellect Devourer',
    'Walking brain-like creatures serving mind flayers as scouts.',
    -2,
    2,
    0,
    -1,
    2,
    1,
    true
),
(
    'Phaerimm',
    'Levitating, cone-shaped horrors absorbing magic and casting incredibly potent spells.',
    0,
    4,
    2,
    2,
    1,
    3,
    true
);