import { Effect } from './effect.model';

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
