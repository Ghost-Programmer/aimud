export enum ItemType {
  WEAPON = 'WEAPON',
  TWO_HANDED_WEAPON = 'TWO_HANDED_WEAPON',
  ARMOR = 'ARMOR',
  FOOD = 'FOOD',
  DRINK = 'DRINK',
  POTION = 'POTION',
  SCROLL = 'SCROLL',
  MONEY = 'MONEY',
  WAND = 'WAND',
  QUEST = 'QUEST',
  KEY = 'KEY',
  LIGHT = 'LIGHT',
  CONTAINER = 'CONTAINER',
  TRASH = 'TRASH',
  MISCELLANEOUS = 'MISCELLANEOUS'
}

export enum WearLocation {
  HEAD = 'HEAD',
  CHEST = 'CHEST',
  LEGS = 'LEGS',
  FEET = 'FEET',
  ARMS = 'ARMS',
  HANDS = 'HANDS',
  RIGHT_FINGER = 'RIGHT_FINGER',
  LEFT_FINGER = 'LEFT_FINGER',
  RIGHT_WRIST = 'RIGHT_WRIST',
  LEFT_WRIST = 'LEFT_WRIST',
  NECK = 'NECK',
  LEFT_EAR = 'LEFT_EAR',
  RIGHT_EAR = 'RIGHT_EAR',
  FACE = 'FACE',
  WAIST = 'WAIST',
  PRIMARY = 'PRIMARY',
  OFFHAND = 'OFFHAND'
}

export enum EffectType {
  SLASHING_DAMAGE = 'SLASHING_DAMAGE',
  BASHING_DAMAGE = 'BASHING_DAMAGE',
  PIERCING_DAMAGE = 'PIERCING_DAMAGE',
  STRENGTH = 'STRENGTH',
  DEXTERITY = 'DEXTERITY',
  CONSTITUTION = 'CONSTITUTION',
  INTELLIGENCE = 'INTELLIGENCE',
  WISDOM = 'WISDOM',
  CHARISMA = 'CHARISMA',
  ARMOR = 'ARMOR',
  HP_REGEN = 'HP_REGEN',
  MANA_REGEN = 'MANA_REGEN',
  PHYSICAL_ATTACK = 'PHYSICAL_ATTACK',
  MAGIC_ATTACK = 'MAGIC_ATTACK',
  MAGIC_RESIST = 'MAGIC_RESIST',
  DODGE = 'DODGE',
  CRITICAL_HIT = 'CRITICAL_HIT',
  FIRE_DAMAGE = 'FIRE_DAMAGE',
  COLD_DAMAGE = 'COLD_DAMAGE',
  SONIC_DAMAGE = 'SONIC_DAMAGE',
  POISON_DAMAGE = 'POISON_DAMAGE',
  ELECTRICAL_DAMAGE = 'ELECTRICAL_DAMAGE',
  FLY = 'FLY',
  WATER_BREATHING = 'WATER_BREATHING',
  INVISIBLE = 'INVISIBLE'
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
