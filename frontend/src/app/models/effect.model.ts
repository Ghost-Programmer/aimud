import { EffectType } from './item.model';

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
