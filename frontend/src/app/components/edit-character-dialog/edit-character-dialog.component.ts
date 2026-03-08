import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CharacterService } from '../../services/character.service';

@Component({
  selector: 'app-edit-character-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './edit-character-dialog.component.html',
  styleUrl: './edit-character-dialog.component.css'
})
export class EditCharacterDialogComponent {
  @Input() character: any;
  @Output() closeDialog = new EventEmitter<void>();
  @Output() characterUpdated = new EventEmitter<any>();

  characterForm: FormGroup;

  constructor(private fb: FormBuilder, private characterService: CharacterService) {
    this.characterForm = this.fb.group({
      name: ['', Validators.required],
      strength: [0, Validators.required],
      dexterity: [0, Validators.required],
      constitution: [0, Validators.required],
      intelligence: [0, Validators.required],
      wisdom: [0, Validators.required],
      charisma: [0, Validators.required]
    });
  }

  ngOnInit() {
    if (this.character) {
      this.characterForm.patchValue(this.character);
    }
  }

  onSubmit() {
    if (this.characterForm.valid) {
      const updatedCharacter = { ...this.character, ...this.characterForm.value };
      this.characterService.updateCharacter(this.character.id, updatedCharacter).subscribe({
        next: (response) => {
          console.log('Character updated', response);
          this.characterUpdated.emit(response);
          this.closeDialog.emit();
        },
        error: (error) => {
          console.error('Error updating character', error);
          alert('Failed to update character');
        }
      });
    }
  }

  onCancel() {
    this.closeDialog.emit();
  }
}
