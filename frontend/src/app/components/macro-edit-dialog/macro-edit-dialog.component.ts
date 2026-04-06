import {Component, Inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef, MatDialogModule} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MobileMacro} from '../../models/mobile-macro.model';

@Component({
  selector: 'app-macro-edit-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  template: `
    <h2 mat-dialog-title>Edit Macro (F{{data.macroIndex + 1}})</h2>
    <mat-dialog-content>
      <div class="macro-form">
        <mat-form-field appearance="fill" style="width: 100%; margin-bottom: 10px;">
          <mat-label>Label</mat-label>
          <input matInput [(ngModel)]="data.label" placeholder="e.g. Heal Self">
        </mat-form-field>
        <mat-form-field appearance="fill" style="width: 100%;">
          <mat-label>Command</mat-label>
          <input matInput [(ngModel)]="data.command" placeholder="e.g. cast 'heal' self">
        </mat-form-field>
      </div>
    </mat-dialog-content>
    <div class="dialog-actions">
      <button class="btn-cancel" (click)="onCancel()">Cancel</button>
      <button class="btn-save" (click)="onSave()">Save</button>
    </div>
  `,
  styles: [`
    .macro-form { padding-top: 10px; }
    .dialog-actions {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      padding: 15px 24px;
      border-top: 1px solid rgba(255, 255, 255, 0.1);
    }
    .btn-cancel, .btn-save {
      padding: 8px 16px;
      border-radius: 4px;
      font-weight: 500;
      cursor: pointer;
      border: none;
    }
    .btn-cancel {
      background: rgba(255, 255, 255, 0.1);
      color: #fff;
    }
    .btn-cancel:hover { background: rgba(255, 255, 255, 0.2); }
    .btn-save {
      background: var(--primary);
      color: #fff;
    }
    .btn-save:hover { background: var(--primary-hover); }
  `]
})
export class MacroEditDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<MacroEditDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: MobileMacro
  ) {}

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    this.dialogRef.close(this.data);
  }
}
