import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-item-stats-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
  templateUrl: './item-stats-dialog.component.html',
  styleUrl: './item-stats-dialog.component.css'
})
export class ItemStatsDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ItemStatsDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {}

  onClose(): void {
    this.dialogRef.close();
  }

  getStatKeys(): string[] {
    return [
      'strength', 'dexterity', 'constitution', 'intelligence', 'wisdom', 'charisma',
      'armor', 'magicResist', 'dodgeChance', 'critChance',
      'physicalAttack', 'magicAttack', 'hpRegen', 'manaRegen'
    ];
  }

  getStatLabel(key: string): string {
    const labels: { [key: string]: string } = {
      strength: 'STR',
      dexterity: 'DEX',
      constitution: 'CON',
      intelligence: 'INT',
      wisdom: 'WIS',
      charisma: 'CHA',
      currentHp: 'HP',
      maxHp: 'Max HP',
      currentMana: 'Mana',
      maxMana: 'Max Mana',
      armor: 'Armor',
      magicResist: 'Magic Resist',
      dodgeChance: 'Dodge',
      critChance: 'Crit',
      physicalAttack: 'Phys Atk',
      magicAttack: 'Mag Atk',
      hpRegen: 'HP Regen',
      manaRegen: 'Mana Regen'
    };
    return labels[key] || key;
  }

  hasStat(key: string): boolean {
    return this.data.item[key] !== undefined && this.data.item[key] !== 0;
  }

  getEffectDescription(effect: any): string {
    const type = effect.effectType;
    const m1 = effect.modifier1;
    const m2 = effect.modifier2;

    switch (type) {
      case 'Bashing Damage':
      case 'Piercing Damage':
      case 'Slashing Damage':
      case 'Fire Damage':
      case 'Cold Damage':
      case 'Sonic Damage':
      case 'Poison Damage':
      case 'Electrical Damage':
        return `${m1}d${m2} ${type}`;
      case 'Strength':
      case 'Dexterity':
      case 'Constitution':
      case 'Intelligence':
      case 'Wisdom':
      case 'Charisma':
      case 'Armor':
      case 'HP Regen':
      case 'Mana Regen':
      case 'Physical Attack':
      case 'Magic Attack':
      case 'Magic Resist':
      case 'Dodge':
      case 'Critical Hit':
        return `${m1 > 0 ? '+' : ''}${m1} ${type}`;
      case 'Fly':
      case 'Water Breathing':
      case 'Invisible':
        return type;
      default:
        return `${type} (${m1}, ${m2}, ${effect.modifier3}, ${effect.modifier4})`;
    }
  }
}
