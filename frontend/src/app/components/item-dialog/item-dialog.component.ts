import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Item, ItemType, WearLocation, Effect, EffectType } from '../../models/item.model';

@Component({
  selector: 'app-item-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './item-dialog.component.html',
  styleUrl: './item-dialog.component.css'
})
export class ItemDialogComponent implements OnInit {
  @Input() itemData?: Item;
  @Output() saveItem = new EventEmitter<Item>();
  @Output() cancelDialog = new EventEmitter<void>();

  item: Item = {
    name: '',
    description: '',
    itemType: ItemType.MISCELLANEOUS,
    wearLocation: WearLocation.FACE,
    effects: []
  };

  itemTypes = Object.values(ItemType);
  wearLocations = Object.values(WearLocation);
  effectTypes = Object.values(EffectType);

  // Map EffectType to its modifier names
  effectModifierNames: Record<EffectType, string[]> = {
    [EffectType.SLASHING_DAMAGE]: ['Number of Dice', 'Size of the Dice', '', ''],
    [EffectType.BASHING_DAMAGE]: ['Number of Dice', 'Size of the Dice', '', ''],
    [EffectType.PIERCING_DAMAGE]: ['Number of Dice', 'Size of the Dice', '', ''],
    [EffectType.STRENGTH]: ['Amount', '', '', ''],
    [EffectType.DEXTERITY]: ['Amount', '', '', ''],
    [EffectType.CONSTITUTION]: ['Amount', '', '', ''],
    [EffectType.INTELLIGENCE]: ['Amount', '', '', ''],
    [EffectType.WISDOM]: ['Amount', '', '', ''],
    [EffectType.CHARISMA]: ['Amount', '', '', ''],
    [EffectType.ARMOR]: ['Amount', '', '', ''],
    [EffectType.HP_REGEN]: ['Amount', '', '', ''],
    [EffectType.MANA_REGEN]: ['Amount', '', '', ''],
    [EffectType.PHYSICAL_ATTACK]: ['Amount', '', '', ''],
    [EffectType.MAGIC_ATTACK]: ['Amount', '', '', ''],
    [EffectType.MAGIC_RESIST]: ['Amount', '', '', ''],
    [EffectType.DODGE]: ['Amount', '', '', ''],
    [EffectType.CRITICAL_HIT]: ['Amount', '', '', ''],
    [EffectType.FIRE_DAMAGE]: ['Number of Dice', 'Size of the Dice', '', ''],
    [EffectType.COLD_DAMAGE]: ['Number of Dice', 'Size of the Dice', '', ''],
    [EffectType.SONIC_DAMAGE]: ['Number of Dice', 'Size of the Dice', '', ''],
    [EffectType.POISON_DAMAGE]: ['Number of Dice', 'Size of the Dice', '', ''],
    [EffectType.ELECTRICAL_DAMAGE]: ['Number of Dice', 'Size of the Dice', '', ''],
    [EffectType.FLY]: ['', '', '', ''],
    [EffectType.WATER_BREATHING]: ['', '', '', ''],
    [EffectType.INVISIBLE]: ['', '', '', '']
  };

  constructor() {}

  ngOnInit(): void {
    if (this.itemData) {
      this.item = JSON.parse(JSON.stringify(this.itemData));
    }
  }

  addEffect() {
    const newEffect: Effect = {
      effectType: EffectType.STRENGTH,
      modifier1: 0,
      modifier2: 0,
      modifier3: 0,
      modifier4: 0
    };
    this.item.effects.push(newEffect);
  }

  removeEffect(index: number) {
    this.item.effects.splice(index, 1);
  }

  getModifierName(effectType: EffectType, index: number): string {
    return this.effectModifierNames[effectType][index];
  }

  save() {
    this.saveItem.emit(this.item);
  }

  cancel() {
    this.cancelDialog.emit();
  }
}
