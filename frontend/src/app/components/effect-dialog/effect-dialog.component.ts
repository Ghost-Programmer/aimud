import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Effect, EffectType} from '../../models/effect.model';

@Component({
  selector: 'app-effect-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './effect-dialog.component.html',
  styleUrl: './effect-dialog.component.css'
})
export class EffectDialogComponent {
  @Input() effectData?: Effect;
  @Output() saveEffect = new EventEmitter<Effect>();
  @Output() cancelDialog = new EventEmitter<void>();

  effect: Effect = {
    name: '',
    effectType: EffectType.STRENGTH,
    modifier1: 0,
    modifier2: 0,
    modifier3: 0,
    modifier4: 0
  };

  effectTypes = Object.values(EffectType);

  constructor() {
  }

  ngOnInit(): void {
    if (this.effectData) {
      this.effect = JSON.parse(JSON.stringify(this.effectData));
    }
  }

  save() {
    this.saveEffect.emit(this.effect);
  }

  cancel() {
    this.cancelDialog.emit();
  }
}
