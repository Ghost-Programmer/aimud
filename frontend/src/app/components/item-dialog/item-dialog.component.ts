import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Item, ItemType, WearLocation} from '../../models/item.model';
import {Effect} from '../../models/effect.model';
import {EffectService} from '../../services/effect.service';
import {SearchableDropdownComponent} from '../searchable-dropdown/searchable-dropdown.component';

@Component({
  selector: 'app-item-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, SearchableDropdownComponent],
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
    property1: 0,
    property2: 0,
    property3: 0,
    property4: 0,
    effects: [],
    noPickup: false
  };

  itemTypes = Object.values(ItemType);
  wearLocations = Object.values(WearLocation);

  availableEffects: Effect[] = [];
  selectedEffectId: number | null = null;

  constructor(private effectService: EffectService) {
  }

  ngOnInit(): void {
    if (this.itemData) {
      this.item = JSON.parse(JSON.stringify(this.itemData));
    }
    this.loadEffects();
  }

  effectDisplayFn = (effect: any) => this.getEffectDisplay(effect);

  loadEffects() {
    // Fetch all effects. In a real app, you might want pagination/search here.
    this.effectService.getEffects(0, 1000, {}).subscribe(pagedEffects => {
      this.availableEffects = pagedEffects.effects.sort((a, b) => (a.name || '').localeCompare(b.name || ''));
    });
  }

  addSelectedEffect() {
    if (this.selectedEffectId) {
      const effectToAdd = this.availableEffects.find(e => e.id === this.selectedEffectId);
      if (effectToAdd && !this.item.effects.some(e => e.id === effectToAdd.id)) {
        this.item.effects.push(effectToAdd);
      }
      this.selectedEffectId = null; // Reset selector
    }
  }

  removeEffect(index: number) {
    this.item.effects.splice(index, 1);
  }

  getEffectDisplay(effect: Effect): string {
    if (effect.name) {
      return effect.name;
    }
    let display: string = effect.effectType;
    if (effect.modifier1) display += ` ${effect.modifier1}`;
    if (effect.modifier2) display += `d${effect.modifier2}`;
    return display;
  }

  save() {
    this.saveItem.emit(this.item);
  }

  cancel() {
    this.cancelDialog.emit();
  }
}
