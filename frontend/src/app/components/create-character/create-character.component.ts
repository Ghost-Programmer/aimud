import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {CharacterService} from '../../services/character.service';
import {ConfigService} from '../../services/config.service';
import {SearchableDropdownComponent} from '../searchable-dropdown/searchable-dropdown.component';
import {forkJoin} from 'rxjs';

@Component({
  selector: 'app-create-character',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, SearchableDropdownComponent],
  templateUrl: './create-character.component.html',
  styleUrl: './create-character.component.css'
})
export class CreateCharacterComponent implements OnInit {
  characterForm: FormGroup;
  baseStats: any = {
    strength: 0,
    dexterity: 0,
    constitution: 0,
    intelligence: 0,
    wisdom: 0,
    charisma: 0
  };
  currentStats: any = {
    currentStrength: 0,
    currentDexterity: 0,
    currentConstitution: 0,
    currentIntelligence: 0,
    currentWisdom: 0,
    currentCharisma: 0
  };
  derivedStats: any = {};
  races: any[] = [];
  classes: any[] = [];
  currentRoomName: string = 'Mud Entrance';

  constructor(
    private fb: FormBuilder,
    private characterService: CharacterService,
    private configService: ConfigService
  ) {
    this.characterForm = this.fb.group({
      name: ['', Validators.required],
      raceId: [null, Validators.required],
      classId: [null, Validators.required]
    });
  }

  ngOnInit() {
    this.loadData();

    this.characterForm.get('raceId')?.valueChanges.subscribe(() => {
      if (this.baseStats.strength > 0) {
        this.updateCurrentStats();
      }
    });

    this.characterForm.get('classId')?.valueChanges.subscribe(() => {
      if (this.baseStats.strength > 0) {
        this.updateCurrentStats();
      }
    });
  }

  loadData() {
    forkJoin({
      races: this.configService.getPlayableRaces(),
      classes: this.configService.getPlayableCharacterClasses()
    }).subscribe(({races, classes}) => {
      this.races = [...races].sort((a, b) => (a.name ?? '').localeCompare(b.name ?? ''));
      this.classes = [...classes].sort((a, b) => (a.name ?? '').localeCompare(b.name ?? ''));

      // Default to Human Cleric
      const human = this.races.find(r => r.name === 'Human');
      const cleric = this.classes.find(c => c.name === 'Cleric');

      if (human && cleric) {
        this.characterForm.patchValue({
          raceId: human.id,
          classId: cleric.id
        });
        this.rollStats(); // Generate initial stats
      }
    });
  }

  rollStats() {
    if (this.characterForm.get('raceId')?.invalid || this.characterForm.get('classId')?.invalid) {
      alert('Please select a Race and Class first.');
      return;
    }

    const character = {
      ...this.characterForm.value,
      strength: this.roll(),
      dexterity: this.roll(),
      constitution: this.roll(),
      intelligence: this.roll(),
      wisdom: this.roll(),
      charisma: this.roll()
    };

    this.characterService.generateCharacter(character).subscribe({
      next: (generatedCharacter) => {
        this.baseStats = {
          strength: generatedCharacter.strength,
          dexterity: generatedCharacter.dexterity,
          constitution: generatedCharacter.constitution,
          intelligence: generatedCharacter.intelligence,
          wisdom: generatedCharacter.wisdom,
          charisma: generatedCharacter.charisma
        };
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
        console.error('Error generating character', error);
      }
    });
  }

  updateCurrentStats() {
    const character = {
      ...this.characterForm.value,
      ...this.baseStats
    };

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
      },
      error: (error) => {
        console.error('Error updating stats', error);
      }
    });
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
      magicResist: character.magicResist
    };
  }

  roll(): number {
    return Math.floor(Math.random() * 4) + 1;
  }

  saveCharacter() {
    if (this.characterForm.valid && this.baseStats.strength > 0) {
      const character = {
        ...this.characterForm.value,
        ...this.baseStats
      };
      this.characterService.createCharacter(character).subscribe({
        next: (response) => {
          console.log('Character created', response);
          alert('Character created successfully!');
          this.characterForm.reset();
          // Reset to defaults after save
          const human = this.races.find(r => r.name === 'Human');
          const cleric = this.classes.find(c => c.name === 'Cleric');
          if (human && cleric) {
            this.characterForm.patchValue({
              raceId: human.id,
              classId: cleric.id
            });
            this.rollStats();
          } else {
            this.baseStats = {strength: 0, dexterity: 0, constitution: 0, intelligence: 0, wisdom: 0, charisma: 0};
            this.currentStats = {
              currentStrength: 0,
              currentDexterity: 0,
              currentConstitution: 0,
              currentIntelligence: 0,
              currentWisdom: 0,
              currentCharisma: 0
            };
            this.derivedStats = {};
          }
        },
        error: (error) => {
          console.error('Error creating character', error);
          alert('Failed to create character');
        }
      });
    } else if (this.baseStats.strength === 0) {
      alert('Please roll stats before saving.');
    }
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
