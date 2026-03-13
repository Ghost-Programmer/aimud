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
  name?: string;
  effectType: EffectType;
  modifier1: number;
  modifier2: number;
  modifier3: number;
  modifier4: number;
  createdAt?: Date;
  modifiedAt?: Date;
  createdBy?: string;
  modifiedBy?: string;
}

export interface PagedEffects {
  effects: Effect[];
  total: number;
  page: number;
  size: number;
}
