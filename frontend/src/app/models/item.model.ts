export enum ItemType {
  WEAPON = 'Weapon',
  TWO_HANDED_WEAPON = 'Two Handed Weapon',
  ARMOR = 'Armor',
  FOOD = 'Food',
  DRINK = 'Drink',
  POTION = 'Potion',
  SCROLL = 'Scroll',
  MONEY = 'Money',
  WAND = 'Wand',
  QUEST = 'Quest',
  KEY = 'Key',
  LIGHT = 'Light',
  CONTAINER = 'Container',
  TRASH = 'Trash',
  MISCELLANEOUS = 'Miscellaneous'
}

export enum WearLocation {
  HEAD = 'Head',
  CHEST = 'Chest',
  LEGS = 'Legs',
  FEET = 'Feet',
  ARMS = 'Arms',
  HANDS = 'Hands',
  RIGHT_FINGER = 'Right Finger',
  LEFT_FINGER = 'Left Finger',
  RIGHT_WRIST = 'Right Wrist',
  LEFT_WRIST = 'Left Wrist',
  NECK = 'Neck',
  LEFT_EAR = 'Left Ear',
  RIGHT_EAR = 'Right Ear',
  FACE = 'Face',
  WAIST = 'Waist',
  PRIMARY = 'Primary',
  OFFHAND = 'Offhand'
}

export enum EffectType {
  SLASHING_DAMAGE = 'Slashing Damage',
  BASHING_DAMAGE = 'Bashing Damage',
  PIERCING_DAMAGE = 'Piercing Damage',
  STRENGTH = 'Strength',
  DEXTERITY = 'Dexterity',
  CONSTITUTION = 'Constitution',
  INTELLIGENCE = 'Intelligence',
  WISDOM = 'Wisdom',
  CHARISMA = 'Charisma',
  ARMOR = 'Armor',
  HP_REGEN = 'HP Regen',
  MANA_REGEN = 'Mana Regen',
  PHYSICAL_ATTACK = 'Physical Attack',
  MAGIC_ATTACK = 'Magic Attack',
  MAGIC_RESIST = 'Magic Resist',
  DODGE = 'Dodge',
  CRITICAL_HIT = 'Critical Hit',
  FIRE_DAMAGE = 'Fire Damage',
  COLD_DAMAGE = 'Cold Damage',
  SONIC_DAMAGE = 'Sonic Damage',
  POISON_DAMAGE = 'Poison Damage',
  ELECTRICAL_DAMAGE = 'Electrical Damage',
  FLY = 'Fly',
  WATER_BREATHING = 'Water Breathing',
  INVISIBLE = 'Invisible'
}

export interface Effect {
  id?: number;
  itemId?: number;
  effectType: EffectType;
  modifier1: number;
  modifier2: number;
  modifier3: number;
  modifier4: number;
}

export interface Item {
  id?: number;
  itemType: ItemType;
  wearLocation: WearLocation;
  name: string;
  description: string;
  effects: Effect[];
  value?: number;
}

export interface PagedItems {
  items: Item[];
  total: number;
  page: number;
  size: number;
}
