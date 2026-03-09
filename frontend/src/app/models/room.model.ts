export enum RoomType {
  INDOORS = 'INDOORS',
  CITY = 'CITY',
  FIELD = 'FIELD',
  FOREST = 'FOREST',
  HILLS = 'HILLS',
  MOUNTAIN = 'MOUNTAIN',
  DESERT = 'DESERT',
  ARCTIC = 'ARCTIC',
  SWAMP = 'SWAMP',
  WATER_SURFACE = 'WATER_SURFACE',
  UNDERWATER = 'UNDERWATER',
  AIR = 'AIR',
  UNDERGROUND_CAVE = 'UNDERGROUND_CAVE',
  UNDERGROUND_DUNGEON = 'UNDERGROUND_DUNGEON'
}

export interface Room {
  id?: number;
  name: string;
  description: string;
  roomType: RoomType;
  northId?: number;
  southId?: number;
  eastId?: number;
  westId?: number;
  upId?: number;
  downId?: number;
  northDoor: boolean;
  southDoor: boolean;
  eastDoor: boolean;
  westDoor: boolean;
  upDoor: boolean;
  downDoor: boolean;
  northDoorOpen: boolean;
  southDoorOpen: boolean;
  eastDoorOpen: boolean;
  westDoorOpen: boolean;
  upDoorOpen: boolean;
  downDoorOpen: boolean;
}

export interface PagedRooms {
  rooms: Room[];
  total: number;
  page: number;
  size: number;
}
