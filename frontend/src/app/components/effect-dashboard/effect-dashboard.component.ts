import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EffectService } from '../../services/effect.service';
import { Effect, PagedEffects } from '../../models/effect.model';
import { EffectType } from '../../models/item.model';
import { EffectDialogComponent } from '../effect-dialog/effect-dialog.component';

@Component({
  selector: 'app-effect-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, EffectDialogComponent],
  templateUrl: './effect-dashboard.component.html',
  styleUrl: './effect-dashboard.component.css'
})
export class EffectDashboardComponent {
  effects: Effect[] = [];
  total = 0;
  page = 0;
  size = 10;
  sort: 'id' | 'effectType' | 'name' = 'effectType';

  // filters
  nameFilter = '';
  typeFilter?: EffectType;
  effectTypes = Object.values(EffectType);

  // dialog state
  showDialog = false;
  editingEffect?: Effect;

  isLoading = false;

  constructor(private effectService: EffectService) {
    this.load();
  }

  load() {
    this.isLoading = true;
    this.effectService.getEffects(this.page, this.size, {
      name: this.nameFilter || undefined,
      type: this.typeFilter,
      sort: this.sort
    }).subscribe({
      next: (res) => {
        this.effects = res.effects;
        this.total = res.total;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load effects', err);
        this.isLoading = false;
      }
    });
  }

  refresh() { this.load(); }

  newEffect() {
    this.editingEffect = undefined;
    this.showDialog = true;
  }

  edit(effect: Effect) {
    this.editingEffect = effect;
    this.showDialog = true;
  }

  onDialogCancel() { this.showDialog = false; }

  onDialogSave(effect: Effect) {
    const op = effect.id ? this.effectService.updateEffect(effect.id, effect) : this.effectService.createEffect(effect);
    op.subscribe({
      next: () => {
        this.showDialog = false;
        this.load();
      },
      error: (err) => console.error('Failed to save effect', err)
    });
  }

  pageCount(): number { return Math.ceil(this.total / this.size) || 1; }
  canPrev(): boolean { return this.page > 0; }
  canNext(): boolean { return this.page + 1 < this.pageCount(); }
  prevPage() { if (this.canPrev()) { this.page--; this.load(); } }
  nextPage() { if (this.canNext()) { this.page++; this.load(); } }
}
