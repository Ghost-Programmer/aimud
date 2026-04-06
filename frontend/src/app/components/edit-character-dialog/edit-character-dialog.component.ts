import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule} from '@angular/forms';
import {CharacterService} from '../../services/character.service';
import {ConfigService} from '../../services/config.service';
import {FactionService} from '../../services/faction.service';
import {Faction} from '../../models/faction.model';
import {SearchableDropdownComponent} from '../searchable-dropdown/searchable-dropdown.component';

@Component({
  selector: 'app-edit-character-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, SearchableDropdownComponent],
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
  derivedStats: any = {};
  currentRoomName: string = '';
  factions: Faction[] = [];
  factionRatings: { [key: number]: number } = {};

  constructor(
    private fb: FormBuilder,
    private characterService: CharacterService,
    private configService: ConfigService,
    private factionService: FactionService
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
      charisma: [0, Validators.required],
      currentRoomId: [null],
      factionId: [null]
    });
  }

  ngOnInit() {
    this.loadRaces();
    this.loadClasses();
    this.factionService.getAllFactions().subscribe(f => this.factions = f);
    
    if (this.character) {
      this.characterForm.patchValue(this.character);
      this.currentRoomName = this.character.currentRoomName;
      this.updateCurrentStats();
      this.factionService.getMobileFactionRatings(this.character.id).subscribe(ratings => {
        this.factionRatings = ratings || {};
      });
    }

    this.characterForm.valueChanges.subscribe(() => {
      this.updateCurrentStats();
    });
  }

  loadRaces() {
    this.configService.getPlayableRaces().subscribe(races => {
      this.races = [...races].sort((a, b) => (a.name ?? '').localeCompare(b.name ?? ''));
    });
  }

  loadClasses() {
    this.configService.getPlayableCharacterClasses().subscribe(classes => {
      this.classes = [...classes].sort((a, b) => (a.name ?? '').localeCompare(b.name ?? ''));
    });
  }

  updateCurrentStats() {
    if (this.characterForm.valid) {
      const character = {...this.character, ...this.characterForm.value};
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
          this.updateDerivedStats(generatedCharacter);
          if (generatedCharacter.currentRoomName) {
            this.currentRoomName = generatedCharacter.currentRoomName;
          }
        },
        error: (error) => {
          console.error('Error calculating stats', error);
        }
      });
    }
  }

  updateDerivedStats(character: any) {
    this.derivedStats = {
      maxHp: character.maxHp,
      maxMana: character.maxMana,
      hpRegen: character.hpRegen,
      manaRegen: character.manaRegen,
      dodgeChance: character.dodgeChance,
      critChance: character.critChance,
      physicalAttack: character.physicalAttack,
      magicAttack: character.magicAttack,
      armor: character.armor,
      magicResist: character.magicResist,
      challengeRating: character.challengeRating
    };
  }

  onSubmit() {
    if (this.characterForm.valid) {
      const updatedCharacter = {...this.character, ...this.characterForm.value};
      this.characterService.updateCharacter(this.character.id, updatedCharacter).subscribe({
        next: (response) => {
          this.factionService.updateMobileFactionRatings(this.character.id, this.factionRatings).subscribe({
            next: () => {
              console.log('Character updated', response);
              this.characterUpdated.emit(response);
              this.closeDialog.emit();
            }
          });
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
