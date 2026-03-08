import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CharacterService } from '../../services/character.service';
import { ConfigService } from '../../services/config.service';

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
  races: any[] = [];
  classes: any[] = [];
  currentStats: any = {};

  constructor(
    private fb: FormBuilder,
    private characterService: CharacterService,
    private configService: ConfigService
  ) {
    this.characterForm = this.fb.group({
      name: ['', Validators.required],
      raceId: [null, Validators.required],
      classId: [null, Validators.required],
      strength: [0, Validators.required],
      dexterity: [0, Validators.required],
      constitution: [0, Validators.required],
      intelligence: [0, Validators.required],
      wisdom: [0, Validators.required],
      charisma: [0, Validators.required]
    });
  }

  ngOnInit() {
    this.loadRaces();
    this.loadClasses();
    if (this.character) {
      this.characterForm.patchValue(this.character);
      this.updateCurrentStats();
    }

    this.characterForm.valueChanges.subscribe(() => {
      this.updateCurrentStats();
    });
  }

  loadRaces() {
    this.configService.getAllRaces().subscribe(races => {
      this.races = races;
    });
  }

  loadClasses() {
    this.configService.getAllCharacterClasses().subscribe(classes => {
      this.classes = classes;
    });
  }

  updateCurrentStats() {
    if (this.characterForm.valid) {
      const character = { ...this.character, ...this.characterForm.value };
      this.characterService.generateCharacter(character).subscribe({
        next: (generatedCharacter) => {
          this.currentStats = {
            currentStrength: generatedCharacter.currentStrength,
            currentDexterity: generatedCharacter.currentDexterity,
            currentConstitution: generatedCharacter.currentConstitution,
            currentIntelligence: generatedCharacter.currentIntelligence,
            currentWisdom: generatedCharacter.currentWisdom,
            currentCharisma: generatedCharacter.currentCharisma
          };
        },
        error: (error) => {
          console.error('Error calculating stats', error);
        }
      });
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

  getSelectedRaceDescription(): string {
    const raceId = this.characterForm.get('raceId')?.value;
    const race = this.races.find(r => r.id === raceId);
    return race ? race.description : '';
  }

  getSelectedClassDescription(): string {
    const classId = this.characterForm.get('classId')?.value;
    const cls = this.classes.find(c => c.id === classId);
    return cls ? cls.description : '';
  }
}
