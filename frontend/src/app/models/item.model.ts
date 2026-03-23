import { Effect } from './effect.model';

export enum ItemType {
  WEAPON = 'Weapon',
  TWO_HANDED_WEAPON = 'Two Handed Weapon',
  RANGED_WEAPON = 'Ranged Weapon',
  LIGHT_ARMOR = 'Light Armor',
  MEDIUM_ARMOR = 'Medium Armor',
  HEAVY_ARMOR = 'Heavy Armor',
  FOOD = 'Food',
  DRINK = 'Drink',
  POTION = 'Potion',
  BOOK = 'Book',
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
  FINGER = 'Finger',
  WRIST = 'Wrist',
  NECK = 'Neck',
  EAR = 'Ear',
  FACE = 'Face',
  WAIST = 'Waist',
  PRIMARY = 'Primary',
  OFFHAND = 'Offhand',
  NONE = 'None'
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
